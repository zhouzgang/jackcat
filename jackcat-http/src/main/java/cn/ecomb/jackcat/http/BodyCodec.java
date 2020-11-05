package cn.ecomb.jackcat.http;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 数据编解码器
 *
 * @author zhouzg
 * @date 2020-11-01.
 */
public interface BodyCodec {

	/** 最大吞吐大小 */
	int maxSwallowSize = 1 * 1024 * 1024;

	/**
	 * 读取请求体数据
	 *
	 * @param inputBuffer 关联的http请求解析类
	 * @param holer 请求体数据
	 * @return 读取状态 -1 表示请求读取完毕，>=0 表示读到数据
	 */
	int doRead(InputBuffer inputBuffer, BufferHoler holer) throws IOException;

	/**
	 * 如果服务端准备发送异常响应，但是请求体还有数据未读（比如当上传一个过大的文件时，服务端
	 * 发现超过限制，但客户端仍在发送数据） 这个时候，为了让客户端能够接收到响应，服务端应该继
	 * 续纯读取剩余的请求体数据，如果超过 maxSwallowSize 抛异常关闭连接
	 *
	 * @param inputBuffer 关联的http请求解析类
	 */
	void endRead(InputBuffer inputBuffer) throws IOException;

	/**
	 * 将响应体写到缓存区
	 *
	 * @param outputBuffer 关联的响应编码处理类
	 * @param src 需要写入的数据
	 */
	void doWrite(OutputBuffer outputBuffer, ByteBuffer src) throws IOException;

	void endWrite(OutputBuffer outputBuffer);


}
