package cn.ecomb.jackcat.catalina.servletx;

import cn.ecomb.jackcat.http.ActionHook;
import cn.ecomb.jackcat.http.JackResponse;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * todo 这里有不少 Buffer 的操作，需要在看看
 *
 * @author zhouzg
 * @date 2020-11-06.
 */
public class AppOutputBuffer extends ServletOutputStream {

	private JackResponse response;
	private String encoding;

	private ByteBuffer bodyBytes;
	// todo 这个是什么？
	private CharBuffer bodyChars;

	private boolean suspended = false;
	private boolean isNew = true;

	private boolean isFull(Buffer buffer) {
		return buffer.limit() == buffer.capacity();
	}

	@Override
	public void write(int b) throws IOException {
		writeByte((byte) b);
	}

	public void writeByte(byte c) {
		if (isFull(bodyBytes)) {
			flushByteBuffer();
		}
		transfer(c, bodyBytes);
		isNew = false;
	}

	public void writeChar(char c) {
		if (isFull(bodyChars)) {
			flushByteBuffer();
		}
		transfer(c, bodyChars);
		isNew = false;
	}

	public void write(char[] cbuf, int off, int len) {
		if (len <= bodyChars.capacity() - bodyChars.limit()) {
			transfer(cbuf, off, len, bodyChars);
			return;
		}

		flushByteBuffer();

		realWriteChars(CharBuffer.wrap(cbuf, off, len));
		isNew = false;
	}

	@Override
	public void write(byte[] b, int off, int len) {
		if (bodyBytes.remaining() == 0) {
			appendByArray(b, off, len);
		} else {
			int n = transfer(b, off, len, bodyBytes);
			len = len - n;
			off = off + n;

			flushByteBuffer();
			if (isFull(bodyBytes)) {
				flushByteBuffer();
				appendByArray(b, off, len);
			}
		}
		isNew = true;
	}

	private void appendByArray(byte[] b, int off, int len) {
		if (len == 0) {
			return;
		}

		int limit = bodyBytes.capacity();
		while (len >= limit) {
			response.doWrite(ByteBuffer.wrap(b, off, len));
			len = len - limit;
			off = off + limit;
		}

		if (len > 0) {
			transfer(b, off, len, bodyBytes);
		}
	}

	private void realWriteChars(CharBuffer from) {
		if (encoding == null) {
			encoding = response.getCharacterEncoding();
		}

		if (from.hasRemaining()) {
			ByteBuffer bb = Charset.forName(encoding).encode(from);
			if (bb.remaining() <= bodyBytes.remaining()) {
				transfer(bb, bodyBytes);
			} else {
				flushByteBuffer();
				response.doWrite(bb.slice());
			}
		}
	}


	private void flushByteBuffer() {
		if (bodyBytes.remaining() > 0) {
			response.doWrite(bodyBytes.slice());
			clear(bodyBytes);
		}
	}

	/**
	 * todo 这里是如何 clear 的
	 * @param buffer
	 */
	private void clear(Buffer buffer) {
		buffer.rewind().limit();
	}

	@Override
	public void flush() {
		if (suspended) {
			return;
		}
		if (bodyBytes.remaining() > 0) {
			flushByteBuffer();
		}
		if (bodyChars.remaining() > 0) {
			flushByteBuffer();
		}
		response.action(ActionHook.ActionCode.FLUSH, null);
	}

	@Override
	public void close() {
		if (suspended) {
			return;
		}
		if (bodyChars.remaining() > 0) {
			flushByteBuffer();
		}
		if (!response.isCommitted()) {
			response.setContentLength(bodyBytes.remaining());
		}
		flushByteBuffer();
		response.action(ActionHook.ActionCode.CLOSE, null);
	}

	public void recycle() {
		clear(bodyBytes);
		clear(bodyChars);
		suspended = false;
		isNew = true;
	}

	public AppOutputBuffer setSuspended(boolean suspended) {
		this.suspended = suspended;
		return this;
	}

	/**
	 * 这种 Buffer 的操作在干嘛
	 * @param buffer
	 */
	private void toWriteMode(Buffer buffer) {
		buffer.mark().position(buffer.limit()).limit(buffer.capacity());
	}

	private void toReadMode(Buffer buffer) {
		buffer.limit(buffer.position()).reset();
	}

	private int transfer(byte[] buf, int off, int len, ByteBuffer to) {
		toWriteMode(to);
		int min = Math.min(len, to.remaining());
		if (min > 0) {
			to.put(buf, off, min);
		}
		toReadMode(to);
		return min;
	}

	private int transfer(char[] buf, int off, int len, CharBuffer to) {
		toWriteMode(to);
		int min = Math.min(len, to.remaining());
		if (min > 0) {
			to.put(buf, off, min);
		}
		toReadMode(to);
		return min;
	}

	private void transfer(byte b, ByteBuffer to) {
		toWriteMode(to);
		to.put(b);
		toReadMode(to);
	}

	private void transfer(char b, CharBuffer to) {
		toWriteMode(to);
		to.put(b);
		toReadMode(to);
	}

	private void transfer(ByteBuffer from, ByteBuffer to) {
		toWriteMode(to);
		int min = Math.min(from.remaining(), to.remaining());
		if (min > 0) {
			int fromLimit = from.limit();
			from.limit(from.position() + min);
			to.put(from);
			from.limit(fromLimit);
		}
		toReadMode(to);
	}

	public boolean isNew() {
		return isNew;
	}
}
