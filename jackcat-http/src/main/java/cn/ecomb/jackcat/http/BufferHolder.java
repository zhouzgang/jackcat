package cn.ecomb.jackcat.http;

import java.nio.ByteBuffer;

/**
 * @author zhouzg
 * @date 2020-11-01.
 */
public interface BufferHolder {

	void setByteBuffer(ByteBuffer byteBuffer);

	ByteBuffer getByteBuffer();
}
