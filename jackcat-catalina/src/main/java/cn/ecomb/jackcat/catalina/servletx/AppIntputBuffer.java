package cn.ecomb.jackcat.catalina.servletx;

import cn.ecomb.jackcat.http.BufferHoler;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zhouzg
 * @date 2020-11-06.
 */
public class AppIntputBuffer extends ServletInputStream implements BufferHoler {

	@Override
	public void setByteBuffer(ByteBuffer byteBuffer) {

	}

	@Override
	public ByteBuffer getByteBuffer() {
		return null;
	}

	@Override
	public int read() throws IOException {
		return 0;
	}
}
