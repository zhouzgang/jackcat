package cn.ecomb.jackcat.http;

import cn.ecomb.jackcat.net.NioChannel;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * 输入缓存区
 *
 * @author zhouzg
 * @date 2020-11-01.
 */
@Data
public class InputBuffer implements Recyclable, BufferHoler{

	private static final Logger logger = LoggerFactory.getLogger(InputBuffer.class);

	/** 数据解析状态 */
	public enum ParseStatus {
		METHOD, URI, VERSION, QUERY, HEAD_NAME, HEAD_VALUE, HEAD_END, DONE
	}

	private ParseStatus parseStatus;

	private boolean parsingHeader = true;
	private int maxHeaderSize = 8192;

	private NioChannel channel;
	private JackRequest request;
	private BodyCodec bodyCodec;
	private ByteBuffer byteBuffer;

	/** 请求体开始的位置 */
	private int bodyOps;
	/** 最大的 post 大小 */
	private int maxPostSize = 1 * 1024 * 1024;
	private ByteBuffer body = ByteBuffer.allocate(maxPostSize);
	private ByteBuffer bodyView = null;


	public InputBuffer(JackRequest request) {
		this.request = request;
	}

	@Override
	public void recycle() {

	}

	@Override
	public void setByteBuffer(ByteBuffer byteBuffer) {

	}

	@Override
	public ByteBuffer getByteBuffer() {
		return null;
	}
}
