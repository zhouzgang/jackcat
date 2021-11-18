package cn.ecomb.jackcat.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author brian.zhou
 * @date 2020/11/25
 */
public class AdapterImplTest implements Adapter{

	private JackRequest request;
	private JackResponse response;

	@Override
	public void service(JackRequest jackRequest, JackResponse jackResponse) throws IOException {
		request = jackRequest;
		response = jackResponse;
		// 触发请求体的读取和解析
		request.getParams().get("none");

		StringBuilder content = new StringBuilder();
		content.append("Server version: " + "RxTomcat/1.1" + "\r\n");
		content.append("OS Name:        " + System.getProperty("os.name") + "\r\n");
		content.append("OS Version:     " + System.getProperty("os.version") + "\r\n");
		content.append("Architecture:   " + System.getProperty("os.arch") + "\r\n");
		content.append("JVM Version:    " + System.getProperty("java.runtime.version") + "\r\n");
		content.append("JVM Vendor:     " + System.getProperty("java.vm.vendor") + "\r\n\r\n");
		content.append(request.toString());

		byte[] bytes = content.toString().getBytes(StandardCharsets.UTF_8);
		response.setContentType("text/plan");
		response.doWrite(ByteBuffer.wrap(bytes));
		response.action(ActionHook.ActionCode.CLOSE, null);
	}
}
