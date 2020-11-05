package cn.ecomb.jackcat.http.codecs;

import cn.ecomb.jackcat.http.BodyCodec;
import cn.ecomb.jackcat.http.BufferHoler;
import cn.ecomb.jackcat.http.InputBuffer;
import cn.ecomb.jackcat.http.OutputBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zhouzg
 * @date 2020-11-05.
 */
public class IdentityCodec implements BodyCodec {

	private int contentLength = -1;
	private int remaining;

	public IdentityCodec(int length) {
		this.contentLength = length;
		this.remaining = length;
	}

	@Override
	public int doRead(InputBuffer inputBuffer, BufferHoler holer) throws IOException {
		int result = -1;
		if (contentLength > 0 && remaining > 0) {
			int n = inputBuffer.readBodyBytes(holer);
			ByteBuffer view = holer.getByteBuffer();
			if (n > remaining) {
				view.limit(view.position() + remaining);
				result = remaining;
			} else {
				result = n;
				remaining -= n;
			}
		}
		return result;
	}

	@Override
	public void endRead(InputBuffer inputBuffer) throws IOException {
		int swallowed = 0;
		while (remaining > 0) {
			int n = inputBuffer.readBody(null);
			if (n > 0) {
				swallowed += n;
				remaining -= n;
				if (swallowed > maxSwallowSize) {
					throw new IOException("max Swallow size");
				}
			} else {
				remaining = 0;
			}
		}
	}

	@Override
	public void doWrite(OutputBuffer outputBuffer, ByteBuffer src) throws IOException {
		outputBuffer.write(src);
	}

	@Override
	public void endWrite(OutputBuffer outputBuffer) {

	}
}
