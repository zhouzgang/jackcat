package cn.ecomb.jackcat.http;

import cn.ecomb.jackcat.net.Handler;
import cn.ecomb.jackcat.net.NioChannel;
import cn.ecomb.jackcat.net.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 使用 Http 协议处理请求数据
 * 1. 使用 Http 协议读写数据
 * 2. 封装 request，response
 * 3. 调用 service，执行具体的请求功能
 *
 * @author zhouzg
 * @date 2020-10-31.
 */
public class HttpProcessor implements Processor, ActionHook{

	private static final Logger logger = LoggerFactory.getLogger(HttpProcessor.class);

	private InputBuffer inBuffer;
	private OutputBuffer outBuffer;

	private JackRequest request;
	private JackResponse response;

	private Adapter adapter;

	private boolean keepAlive = true;
	private boolean error = false;

	public HttpProcessor() {
		request = new JackRequest();
		inBuffer = new InputBuffer();
	}

	@Override
	public Handler.SocketState process(NioChannel channel) {
		return null;
	}

	@Override
	public void action(ActionCode code, Object... param) {

	}
}
