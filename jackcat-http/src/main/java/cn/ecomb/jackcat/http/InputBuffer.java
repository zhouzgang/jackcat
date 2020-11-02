package cn.ecomb.jackcat.http;

import cn.ecomb.jackcat.net.NioChannel;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
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

	/** 当前解析的状态 */
	private ParseStatus parseStatus;

	private boolean parsingHeader = true;
	private int maxHeaderSize = 8192;

	private NioChannel channel;
	private JackRequest request;
	private BodyCodec bodyCodec;
	/** 应用 Channel 内部的 readByteBuffer */
	private ByteBuffer byteBuffer;

	/** 请求体开始的位置 */
	private int bodyOps;
	/** 最大的 post 大小 */
	private int maxPostSize = 1 * 1024 * 1024;
	private ByteBuffer body = ByteBuffer.allocate(maxPostSize);
	private ByteBuffer bodyView = null;

	/** 请求头信息在字节数组中结束的位置，即请求体开始位置 */
	private int rHead;
	private StringBuffer sb = new StringBuffer();

	public InputBuffer(JackRequest request) {
		this.request = request;
	}

	private String takeString() {
		String ret = sb.toString();
		sb.setLength(0);
		return ret;
	}

	public void setChannel(NioChannel channel) {
		this.channel = channel;
		byteBuffer = channel.getReadBuff();
		byteBuffer.position(0).limit(0);
	}

	/**
	 *  解析请求行和请求头
	 * @return
	 */
	public boolean parseRequestLineAndHeads() throws IOException {

		logger.debug("解析请求头和情趣行");
		// 这里的处理方式很有意思，通过状态流转的方式，循环读取，并解析
		// 这里是不是可以一次性循环读取完，再解析会好一点？
		do {
			if (byteBuffer.position() >= byteBuffer.limit() && !fill(false)) {
				return false;
			}

			byte chr = byteBuffer.get();
			switch (parseStatus) {
				case METHOD:
					break;
				case URI:
					break;
				case QUERY:
					break;
				case VERSION:
					break;
				case HEAD_NAME:
					break;
				case HEAD_VALUE:
					break;
				case HEAD_END:
					break;
				default:
					break;
			}

		} while (parseStatus != ParseStatus.DONE);


		return true;
	}

	/**
	 * 从 socket 通道里读取字节
	 * todo 原版的这里设计的很乱，调用来调用去的，思路一点的不清晰，或者说代码写的不通俗易懂。
	 * @param block
	 */
	public boolean fill(boolean block) throws IOException {
		if (parsingHeader) {
			if (byteBuffer.limit() > maxHeaderSize) {
				throw new IllegalArgumentException("请求头太大");
			}
		} else {
			// todo 这里的设置有点不明白
			byteBuffer.limit(rHead).position(rHead);
		}
		// 这里的 mark，reset 是 byteBuffer 的一种操作形式，需要在熟悉一下
		byteBuffer.mark();
		byteBuffer.limit(byteBuffer.capacity());
		int n = channel.read(byteBuffer, block);
		byteBuffer.limit(byteBuffer.position()).reset();
		if (n == -1) {
			throw new EOFException("EOF, 通道被关闭");
		}
		return n > 0;
	}


	/**
	 * 读取请求体
	 * @param holer
	 * @return
	 */
	public int readBody(BufferHoler holer) {
		return 0;
	}

	/**
	 * 读取请求体
	 * @param holer
	 * @return
	 */
	public int readBodyBytes(BufferHoler holer) {
		return 0;
	}


	/**
	 * 解析 GET 和 POST 请求参数
	 */
	public void readAndParseBody() {

	}


	/**
	 * 解析请求参数
	 * @param body
	 */
	private void parseParam(byte[] body) {

	}

	public void setRequest(JackRequest request) {
		this.request = request;
	}

	public void setBodyCodec(BodyCodec codec) {
		this.bodyCodec = codec;
	}

	public void end() {
		if (bodyCodec != null) {
			bodyCodec.endRead(this);
		}
	}

	@Override
	public void recycle() {
		request.recycle();
		parseStatus = ParseStatus.METHOD;
		parsingHeader = true;
		byteBuffer.clear();
		bodyCodec = null;
		rHead = 0;
	}

	@Override
	public void setByteBuffer(ByteBuffer byteBuffer) {
		bodyView = byteBuffer;
	}

	@Override
	public ByteBuffer getByteBuffer() {
		return bodyView;
	}
}
