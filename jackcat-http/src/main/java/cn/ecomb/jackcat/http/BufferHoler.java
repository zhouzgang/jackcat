package cn.ecomb.jackcat.http;

import java.nio.ByteBuffer;

/**
 * @author zhouzg
 * @date 2020-11-01.
 */
public interface BufferHoler {

	void setByteBuffer(ByteBuffer byteBuffer);

	ByteBuffer getByteBuffer();
}
