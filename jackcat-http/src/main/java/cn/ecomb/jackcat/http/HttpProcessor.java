package cn.ecomb.jackcat.http;

import cn.ecomb.jackcat.http.codecs.ChunkedCodec;
import cn.ecomb.jackcat.http.codecs.IdentityCodec;
import cn.ecomb.jackcat.net.Handler;
import cn.ecomb.jackcat.net.NioChannel;
import cn.ecomb.jackcat.net.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * 使用 Http 协议处理请求数据
 * 1. 使用 Http 协议读写数据
 * 2. 封装 request，response
 * 3. 调用 service，执行具体的请求功能
 *
 * @author zhouzg
 * @date 2020-10-31.
 */
public class HttpProcessor implements Processor, ActionHook{

	private static final Logger logger = LoggerFactory.getLogger(HttpProcessor.class);

	private InputBuffer inBuffer;
	private OutputBuffer outBuffer;

	private JackRequest request;
	private JackResponse response;

	private Adapter adapter;

	private boolean keepAlive = true;
	private boolean error = false;

	/** 一个长连接最多处理多少个请求，-1 表示不限制 */
	private int maxkeepAliveRequests = -1;

	public HttpProcessor() {
		request = new JackRequest();
		inBuffer = new InputBuffer(request);
		request.setActionHook(this);

		response = new JackResponse();
		outBuffer = new OutputBuffer(response);
		response.setActionHook(this);
		maxkeepAliveRequests = 10;
	}

	public HttpProcessor setAdapter(Adapter adapter) {
		this.adapter = adapter;
		return this;
	}

	@Override
	public Handler.SocketState process(NioChannel channel) {
		inBuffer.setChannel(channel);
		outBuffer.setChannel(channel);
		int keepAliveLeft = maxkeepAliveRequests;

		// 这是一种什么写法
		while (!error && keepAlive) {
			try {
				if (inBuffer.parseRequestLineAndHeads()) {
					return Handler.SocketState.LONG;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return Handler.SocketState.CLOSED;
			}

			prepareRequest();

			if (maxkeepAliveRequests > 0 && --keepAliveLeft == 0) {
				keepAlive = false;
			}

			if (!error) {
				try {
					logger.debug("提交给容器处理，并生成响应结果");
					adapter.service(request, response);
				} catch (IOException e) {
					error = true;
					e.printStackTrace();
				}
			}

			try {
				inBuffer.end();
			} catch (IOException e) {
				logger.error("错误的饿完成请求结果:{}", e.getMessage());
				error = true;
				response.setStatus(500);
			}
			try {
				outBuffer.end();
			} catch (IOException e) {
				logger.error("错误的饿完成请求结果:{}", e.getMessage());
				error = true;
				response.setStatus(500);
			}

			inBuffer.recycle();
			outBuffer.recycle();

			if (!error && keepAlive) {
				return Handler.SocketState.OPEN;
			}
		}

		return Handler.SocketState.CLOSED;
	}

	/**
	 * 检查请求头是否合法
	 */
	private void prepareRequest() {
		String version = request.getProtocol();
		if (!"HTTP/1.1".equalsIgnoreCase(version)) {
			error = true;
		}

		String conn = request.getHeader("connection");
		if (conn == null || "CLOSE".equals(conn)) {
			keepAlive = false;
		} else if ("keep-alive".equals(conn)) {
			keepAlive = true;
		}
		// todo 检查 expect 头

		boolean contentDelimitation = false;
		String transferEncoding = request.getHeader("transfer-endcoding");
		if ("chunked".equalsIgnoreCase(transferEncoding)) {
			contentDelimitation = true;
			inBuffer.setBodyCodec(new ChunkedCodec());
		}

		int contentLength = request.getContentLength();
		if (contentLength >= 0) {
			if (contentDelimitation) {
				request.remove("content-length");
				request.setContentLength(-1);
			} else {
				inBuffer.setBodyCodec(new IdentityCodec(contentLength));
				contentDelimitation = true;
			}
		}

		String host = request.getHeader("host");
		if (host == null || host.length() <= 0) {
			error = true;
		}

	}

	@Override
	public void action(ActionCode code, Object... param) {
		switch (code) {
			case COMMIT:
				if (!response.isCommitted()) {
					try {
						prepareResponse();
						outBuffer.commit();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				break;
			case CLOSE:
				action(ActionCode.COMMIT);
				try {
					outBuffer.end();
				} catch (IOException e) {
					error = true;
					e.printStackTrace();
				}
				break;
			case ACK:
				break;
			case PARSE_PARAMS:
				inBuffer.readAndParseBody();
				break;
			case READ_BODY:
				BufferHolder holer = (BufferHolder) param[0];
				try {
					inBuffer.readBody(holer);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case WRITE_BODY:
				action(ActionCode.COMMIT);
				try {
					outBuffer.writeBody((ByteBuffer) param[0]);
				} catch (IOException e) {
					error = true;
					e.printStackTrace();
				}
				break;
			case FLUSH:
				action(ActionCode.COMMIT);
				try {
					outBuffer.flush();
				} catch (IOException e) {
					error = true;
					e.printStackTrace();
				}
				break;
			default:
				break;
		}
	}

	private void prepareResponse() {
		int statusCode = response.getStatus();
		if (statusCode == 204 || statusCode == 205 || statusCode == 304) {
			response.setContentLength(-1);
		} else {
			String contentType = response.getContentType();
			if (contentType != null) {
				response.addHeader("Content-Type", contentType);
			}
			String contentLanguage = response.getContentLanguage();
			if (contentLanguage != null) {
				response.addHeader("Content-language", contentLanguage);
			}
			int contentLength = response.getContentLength();
			if (contentLength != -1) {
				response.addHeader("Conent-Length", String.valueOf(contentLength));
				outBuffer.setBodyCodec(new IdentityCodec(contentLength));
			} else {
				response.addHeader("Transfer-Encoding", "chunked");
				outBuffer.setBodyCodec(new ChunkedCodec());
			}
		}
	}
}
