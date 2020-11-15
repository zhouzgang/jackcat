package cn.ecomb.jackcat.catalina.servletx;

import java.io.IOException;
import java.io.Writer;

/**
 * @author zhouzg
 * @date 2020-11-06.
 */
public class RespWriter extends Writer {

	private AppOutputBuffer outputBuffer;

	public RespWriter(AppOutputBuffer outputBuffer) {
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		outputBuffer.write(cbuf, off, len);
	}

	@Override
	public void write(int c) throws IOException {
		outputBuffer.write((char)c);
	}

	@Override
	public void flush() throws IOException {
		outputBuffer.flush();
	}

	@Override
	public void close() throws IOException {
		outputBuffer.close();
	}
}
