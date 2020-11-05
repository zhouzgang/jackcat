package cn.ecomb.jackcat.catalina.servletx;

import cn.ecomb.jackcat.http.BufferHoler;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zhouzg
 * @date 2020-11-06.
 */
public class AppOutputBuffer extends ServletOutputStream implements BufferHoler {

	@Override
	public void setByteBuffer(ByteBuffer byteBuffer) {

	}

	@Override
	public ByteBuffer getByteBuffer() {
		return null;
	}

	@Override
	public void write(int b) throws IOException {

	}
}
