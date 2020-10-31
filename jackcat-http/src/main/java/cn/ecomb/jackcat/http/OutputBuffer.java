package cn.ecomb.jackcat.http;

import cn.ecomb.jackcat.net.NioChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * 输出数据缓存区
 *
 * @author zhouzg
 * @date 2020-11-01.
 */
public class OutputBuffer implements Recyclable {

	private static final Logger logger = LoggerFactory.getLogger(OutputBuffer.class);

	public static final byte[] HTTP_1_1 = "HTTP/1.1".getBytes();
	public static final byte[] CRLF_BYTES = "\r\n".getBytes();

	private ByteBuffer byteBuffer;
	private BodyCodec bodyCodec;
	private NioChannel channel;
	private JackResponse response;


	@Override
	public void recycle() {

	}
}
