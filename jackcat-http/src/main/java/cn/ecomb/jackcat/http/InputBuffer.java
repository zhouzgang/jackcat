package cn.ecomb.jackcat.http;

import cn.ecomb.jackcat.net.NioChannel;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;

import static cn.ecomb.jackcat.http.HttpTag.*;

/**
 * 输入缓存区
 *
 * @author zhouzg
 * @date 2020-11-01.
 */
@Data
public class InputBuffer implements Recyclable, BufferHoler {

	private static final Logger logger = LoggerFactory.getLogger(InputBuffer.class);
	private byte chr;

	/**
	 * 数据解析状态
	 */
	public enum ParseStatus {
		METHOD, URI, VERSION, QUERY, HEAD_NAME, HEAD_VALUE, HEAD_END, DONE
	}

	/**
	 * 当前解析的状态
	 */
	private ParseStatus parseStatus;

	private boolean parsingHeader = true;
	private int maxHeaderSize = 8192;

	private NioChannel channel;
	private JackRequest request;
	private BodyCodec bodyCodec;
	/**
	 * 应用 Channel 内部的 readByteBuffer
	 */
	private ByteBuffer byteBuffer;

	/**
	 * 请求体开始的位置
	 */
	private int bodyOps;
	/**
	 * 最大的 post 大小
	 */
	private int maxPostSize = 1 * 1024 * 1024;
	private ByteBuffer body = ByteBuffer.allocate(maxPostSize);
	private ByteBuffer bodyView = null;

	/**
	 * 请求头信息在字节数组中结束的位置，即请求体开始位置
	 */
	private int rHead;
	private StringBuffer sb = new StringBuffer();

	public InputBuffer(JackRequest request) {
		this.request = request;
	}

	// sb 里面的内容是如何设置进去的？
	private String takeString() {
		String ret = sb.toString();
		sb.setLength(0);
		return ret;
	}

	public void setChannel(NioChannel channel) {
		this.channel = channel;
		byteBuffer = channel.getReadBuff();
		byteBuffer.position(0).limit(0);
	}

	/**
	 * 解析请求行和请求头
	 *
	 * @return
	 */
	public boolean parseRequestLineAndHeads() throws IOException {

		logger.debug("解析请求头和情趣行");
		// 这里的处理方式很有意思，通过状态流转的方式，循环读取，并解析
		// 这里是不是可以一次性循环读取完，再解析会好一点？
		String headerName = null;
		do {
			if (byteBuffer.position() >= byteBuffer.limit() && !fill(false)) {
				return false;
			}

			// 这里get做了什么事情
			chr = byteBuffer.get();
			switch (parseStatus) {
				case METHOD:
					if (chr == SP) {
						request.setMethod(takeString());
						sb.setLength(0);
						parseStatus = ParseStatus.URI;
					} else {
						sb.append((char) chr);
					}
					break;
				case URI:
					if (chr == SP || chr == QUESTION) {
						request.setUri(takeString());
						sb.setLength(0);
						if (chr == QUESTION) {
							request.setQueryBufferOps(byteBuffer.position());
							parseStatus = ParseStatus.QUERY;
						} else {
							parseStatus = ParseStatus.VERSION;
						}
					} else {
						sb.append((char) chr);
					}
					break;
				case QUERY:
					if (chr == SP) {
						int queryEndPos = byteBuffer.position();
						ByteBuffer temp = byteBuffer.duplicate();
						temp.position(request.getQueryBufferOps()).limit(queryEndPos);
						request.setQueryBuffer(temp.duplicate());
						request.getQueryBuffer().mark();

						parseStatus = ParseStatus.VERSION;
					}
					break;
				case VERSION:
					if (chr == CR) {

					} else if (chr == LF) {
						request.setProtocol(takeString());
						parseStatus = ParseStatus.HEAD_NAME;
					} else {
						sb.append((char) chr);
					}
					break;
				case HEAD_NAME:
					if (chr == COLON) {
						headerName = takeString().toLowerCase();
						parseStatus = ParseStatus.HEAD_VALUE;
					} else {
						sb.append((char) chr);
					}
					break;
				case HEAD_VALUE:
					if (chr == CR) {

					} else if (chr == LF) {
						request.addHeader(headerName, takeString().trim().toLowerCase());
						headerName = null;
						parseStatus = ParseStatus.HEAD_END;
					} else {
						sb.append((char) chr);
					}
					break;
				case HEAD_END:
					if (chr == CR) {

					} else if (chr == LF) {
						parseStatus = ParseStatus.DONE;
						parsingHeader = false;
						rHead = byteBuffer.position();
					} else {
						sb.append((char) chr);
						parseStatus = ParseStatus.HEAD_NAME;
					}
					break;
				default:
					break;
			}

		} while (parseStatus != ParseStatus.DONE);

		logger.debug("请求头部数据读取并解析完毕\r\n======Request======\r\n{}\r\n===================", request);

		return true;
	}

	/**
	 * 从 socket 通道里读取字节
	 * todo 原版的这里设计的很乱，调用来调用去的，思路一点的不清晰，或者说代码写的不通俗易懂。
	 *
	 * @param block
	 */
	public boolean fill(boolean block) throws IOException {
		if (parsingHeader) {
			if (byteBuffer.limit() > maxHeaderSize) {
				throw new IllegalArgumentException("请求头太大");
			}
		} else {
			// todo 这里的设置有点不明白
			byteBuffer.limit(rHead).position(rHead);
		}
		// 这里的 mark，reset 是 byteBuffer 的一种操作形式，需要在熟悉一下
		byteBuffer.mark();
		byteBuffer.limit(byteBuffer.capacity());
		int n = channel.read(byteBuffer, block);
		byteBuffer.limit(byteBuffer.position()).reset();
		if (n == -1) {
			throw new EOFException("EOF, 通道被关闭");
		}
		return n > 0;
	}


	/**
	 * 模拟阻塞读取请求体
	 *
	 * @param holer
	 * @return
	 */
	public int readBody(BufferHoler holer) throws IOException {
		if (bodyCodec != null) {
			return bodyCodec.doRead(this, holer);
		} else {
			return readBodyBytes(holer);
		}
	}

	/**
	 * 读取请求体，从底层通道读取数据，并返回一个与结果对应的视图
	 * 这里的作用是什么？
	 *
	 * @param holer
	 * @return
	 */
	public int readBodyBytes(BufferHoler holer) throws IOException {
		if (byteBuffer.position() >= byteBuffer.limit()) {
			if (!fill(true)) {
				holer.setByteBuffer(null);
				return -1;
			}
		}
		int length = byteBuffer.remaining();
		if (byteBuffer != null) {
			holer.setByteBuffer(byteBuffer.duplicate());
		}
		byteBuffer.position(byteBuffer.limit());
		return length;
	}


	/**
	 * 解析 GET 和 POST 请求参数
	 */
	public void readAndParseBody() {
		request.setParamParsed(true);
		// parse GET params
		if (request.getQueryBuffer() != null) {
			request.getQueryBuffer().reset();
			byte[] queryBytes = new byte[request.getQueryBuffer().remaining()];
			request.getQueryBuffer().get(queryBytes);
			parseParam(queryBytes);
		}

		if (!"POST".contentEquals(request.getMethod()) ||
				!request.getContentType().contains("application/x-www-form-urlencoded")) {
			return;
		}

		body.clear();
		try {
			int len = request.getContentLength();
			if (len > 0) {
				if (len > maxPostSize) {
					request.setParamParseFail(true);
					return;
				}

				int n = -1;
				while (len > 0 && (n = readBody(this)) >= 0) {
					body.put(bodyView);
					len -= n;
				}
			} else if ("chunked".equalsIgnoreCase(request.getHeader("transfer-encoding"))) {
				len = 0;
				int n = 0;
				while ((n = readBody(this)) >= 0) {
					body.put(bodyView);
					len += n;
					if (len > maxPostSize) {
						request.setParamParseFail(true);
						return;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * 解析请求 URL 上的参数 a=%99
	 *
	 * @param body
	 */
	private void parseParam(byte[] body) {
		String paramStr = new String(body, request.getEncoding());
		try {
			paramStr = URLDecoder.decode(paramStr, request.getEncoding().toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String[] params = paramStr.split("&");
		if (params != null && params.length > 0) {
			for (String param : params) {
				if (param.startsWith("=")) {
					continue;
				}
				String[] nv = param.trim().split("=");
				request.getParams().put(nv[0], nv.length > 1 ? nv[1] : "");
			}
		}
	}

	public void setRequest(JackRequest request) {
		this.request = request;
	}

	public void setBodyCodec(BodyCodec codec) {
		this.bodyCodec = codec;
	}

	public void end() throws IOException {
		if (bodyCodec != null) {
			bodyCodec.endRead(this);
		}
	}

	@Override
	public void recycle() {
		request.recycle();
		parseStatus = ParseStatus.METHOD;
		parsingHeader = true;
		byteBuffer.clear();
		bodyCodec = null;
		rHead = 0;
	}

	@Override
	public void setByteBuffer(ByteBuffer byteBuffer) {
		bodyView = byteBuffer;
	}

	@Override
	public ByteBuffer getByteBuffer() {
		return bodyView;
	}
}
