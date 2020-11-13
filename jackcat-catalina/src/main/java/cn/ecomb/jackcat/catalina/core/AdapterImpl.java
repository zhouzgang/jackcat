package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.catalina.servletx.Request;
import cn.ecomb.jackcat.catalina.servletx.Response;
import cn.ecomb.jackcat.http.Adapter;
import cn.ecomb.jackcat.http.JackRequest;
import cn.ecomb.jackcat.http.JackResponse;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.TreeMap;

/**
 * 映射 Servlet，解析 sessionId
 * @author zhouzg
 * @date 2020-11-06.
 */
@Slf4j
public class AdapterImpl implements Adapter {

	private Connector connector;

	public AdapterImpl(Connector connector) {
		this.connector = connector;
	}

	@Override
	public void service(JackRequest jackRequest, JackResponse jackResponse) throws IOException {
		Request request = new Request();
		request.setJackRequest(jackRequest);
		Response response = new Response();
		response.setJackResponse(jackResponse);
		response.setRequest(request);
		request.setResponse(response);

		try {
			if (postParseRequest(jackRequest, request, jackResponse, response)) {
			connector.getContext().getPipeline().handle(request, response);
			}
			response.finish();
		} catch (ServletException e) {
			e.printStackTrace();
		}finally {
			request.recycle();
			response.recycle();
		}
	}

	/**
	 * url 映射 servlet，解析 sessionId
	 * @return
	 */
	private boolean postParseRequest(JackRequest jackRequest, Request request, JackResponse jackResponse, Response response) {
		Context context = connector.getContext();
		String uri = jackRequest.getUri();
		try {
			uri = URLDecoder.decode(uri, jackRequest.getEncoding().name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 匹配容器，在正式的环境中，这里应该是一个上下文数组，可能会存在多个上下文的情况
		if (uri.startsWith(context.getDocBase(), 1)) {
			request.setContext(context);
		} else {
			log.error("匹配上下文失败，404");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return false;
		}

		uri = uri.substring(uri.indexOf(context.getDocBase()) + context.getDocBase().length());
		if ("".equals(uri)) {
			uri = "/";
		}

		boolean mapRequired = true;
		while (mapRequired) {
			Wrapper mapWrapper = mapServlet(context, uri);
			request.setWrapper(mapWrapper);

			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
						request.setSessionId(cookie.getValue());
					}
				}
			}

			if (log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder(120);
				sb.append("映射 Servlet\r\n======Mapping Result======");
				sb.append("\r\n  Request Path: ").append(uri);
				sb.append("\r\n  Context: /").append(context.getDocBase());
				sb.append("\r\n  Wrapper: ").append(mapWrapper);
				sb.append("\r\n  jsessionid: ").append(request.getRequestedSessionId());
				sb.append("\r\n==========================");
				log.debug(sb.toString());
			}

			mapRequired = false;

			if (context.isPaused()) {
				log.debug("web 应用 {} 正在热部署", context.getDocBase());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				request.recycle();
				mapWrapper = null;
				mapRequired = true;
			}
		}

		return true;
	}

	/**
	 * 映射 servlet
	 * @return
	 */
	private Wrapper mapServlet(Context context, String uri) {
		Wrapper mapWrapper = null;

		TreeMap<String, Wrapper> exactWrappers = context.getExactWrappers();
		String key = exactWrappers.floorKey(uri);
		if (key != null && uri.equals(key)) {
			mapWrapper = exactWrappers.get(key);
		}

		if (mapWrapper == null) {
			TreeMap<String, Wrapper> treeMap = context.getWildcardWrappers();
			key = exactWrappers.floorKey(uri);
			if (key != null && (uri.startsWith(key) || uri.endsWith(key) || uri.contains(key + "/"))) {
				mapWrapper = treeMap.get(key);
			}
		}

		if (mapWrapper == null) {
			TreeMap<String, Wrapper> treeMap = context.getExtensionWrappers();
			key = treeMap.floorKey(uri);
			if (key != null && uri.endsWith("." + key)) {
				mapWrapper = treeMap.get(key);
			}
		}

		if (mapWrapper == null) {
			if (uri.endsWith("/")) {
				uri += context.getWelcomeFile();
			}
		}

		if (mapWrapper == null) {
			mapWrapper = context.getDefaultWrapper();
		}

		mapWrapper.setWrapperPath(uri);

		return mapWrapper;
	}
}
