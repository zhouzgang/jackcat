package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.http.Adapter;
import cn.ecomb.jackcat.http.JackRequest;
import cn.ecomb.jackcat.http.JackResponse;

/**
 * 映射 Servlet，解析 sessionId
 * @author zhouzg
 * @date 2020-11-06.
 */
public class AdapterImpl implements Adapter {

	private Connector connector;

	public AdapterImpl(Connector connector) {
		this.connector = connector;
	}

	@Override
	public void service(JackRequest request, JackResponse response) {

	}

	/**
	 * url 映射 servlet，解析 sessionId
	 * @return
	 */
	private boolean posetParseRequest() {
		return true;
	}

	/**
	 * 映射 servlet
	 * @return
	 */
	private Wrapper mapServlet() {
		return null;
	}
}
