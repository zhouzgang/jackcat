package cn.ecomb.jackcat.http.codecs;

import cn.ecomb.jackcat.http.BodyCodec;
import cn.ecomb.jackcat.http.BufferHoler;
import cn.ecomb.jackcat.http.InputBuffer;
import cn.ecomb.jackcat.http.OutputBuffer;

import java.nio.ByteBuffer;

/**
 * @author zhouzg
 * @date 2020-11-05.
 */
public class ChunkedCodec implements BodyCodec {


	@Override
	public int doRead(InputBuffer inputBuffer, BufferHoler holer) {
		return 0;
	}

	@Override
	public void endRead(InputBuffer inputBuffer) {

	}

	@Override
	public void doWrite(OutputBuffer outputBuffer, ByteBuffer src) {

	}

	@Override
	public void endWrite(OutputBuffer outputBuffer) {

	}
}
