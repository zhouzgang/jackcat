package cn.ecomb.jackcat.http;

import cn.ecomb.jackcat.net.NioChannel;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * 输出数据缓存区
 *
 * @author zhouzg
 * @date 2020-11-01.
 */
@Data
public class OutputBuffer implements Recyclable {

	private static final Logger logger = LoggerFactory.getLogger(OutputBuffer.class);

	public static final byte[] HTTP_1_1 = "HTTP/1.1".getBytes();
	public static final byte[] CRLF_BYTES = "\r\n".getBytes();

	private ByteBuffer byteBuffer;
	private BodyCodec bodyCodec;
	private NioChannel channel;
	private JackResponse response;

	public OutputBuffer(JackResponse response) {
		this.response = response;
	}


	/**
	 * 将响应头写到缓存区
	 */
	public void commit() throws UnsupportedEncodingException {
		response.setCommitted(true);
		int pos = byteBuffer.position();
		byteBuffer.put(HttpTag.HTTP_1_1);
		int status = response.getStatus();
		byteBuffer.put(String.valueOf(status).getBytes());
		byte[] msg = null;
		if (response.getMessage() != null) {
			msg = response.getMessage().getBytes(response.getCharacterEncoding());
		} else {
			msg = HttpStatus.getByCode(status).getMsg().getBytes();
		}

		byteBuffer.put(msg);
		byteBuffer.put(HttpTag.CRLF);

		for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
			byte[] name = header.getKey().getBytes();
			if ("Set-Cookie".equalsIgnoreCase(header.getKey())) {
				for (String cookie : header.getValue().split(";")) {
					writeHeader(name, cookie.getBytes());
				}
			} else {
				writeHeader(name, header.getValue().getBytes());
			}
		}
		byteBuffer.put(HttpTag.CRLF);
		logger.debug("将响应头部 {}B 数据写入提交到底层缓存区", byteBuffer.position() - pos + 1);

	}

	private void writeHeader(byte[] name, byte[] value) {
		byteBuffer.put(name);
		byteBuffer.put(HttpTag.COLON);
		byteBuffer.put(HttpTag.SP);
		byteBuffer.put(value);
		byteBuffer.put(HttpTag.CRLF);
	}

	/**
	 * 写入响应体数据
	 * 写之前需要确认，响应头数据已写入
	 */
	public void writeBody(ByteBuffer body) throws IOException {
		if (!response.isCommitted()) {
			response.action(ActionHook.ActionCode.COMMIT, null);
		}
		if (body.remaining() > 0) {
			logger.debug("写入响应体数据: {}", body.remaining());
			bodyCodec.doWrite(this, body);
		}
	}

	public void end() throws IOException {
		if (!response.isCommitted()) {
			response.action(ActionHook.ActionCode.COMMIT, null);
		}
		if (bodyCodec != null) {
			bodyCodec.endWrite(this);
		}
		flush();
	}

	public void write(byte[] bytes) throws IOException {
		write(ByteBuffer.wrap(bytes));
	}

	public void write(ByteBuffer byteBuffer) throws IOException {
		write(byteBuffer, false);
	}

	public void write(ByteBuffer src, boolean flip) throws IOException {
		if (flip) {
			src.flip();
		}
		while (src.hasRemaining()) {
			// 没有剩余空间
			if (byteBuffer.remaining() == 0) {
				channel.flush();
			}
			byteBuffer.put(src);
		}
		src.clear();
		channel.setLastAccessTime();
	}

	public void flush() throws IOException {
		channel.flush();
	}


	@Override
	public void recycle() {
		response.recycle();
		byteBuffer.clear();
		bodyCodec = null;
	}
}
