package cn.ecomb.jackcat.http;

import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 原始 Http 请求对象封装，请求方法、参数、uri、请求头
 *
 * @author zhouzg
 * @date 2020-10-29.
 */
@Data
public class JackRequest implements Recyclable, ActionHook{

	/** 网络协议 */
	private String protocol;
	/** 请求方法 */
	private String method;
	private String uri;

	private ByteBuffer queryBuffer;
	private int queryBufferOps = -1;

	private String contentType;
	private int contentLength = -1;

	private HashMap<String, String> headers = new HashMap<>();
	private HashMap<String, String> params = new HashMap<>();
	private HashMap<String, String> attributes = new HashMap<>();

	private boolean paramParsed = false;
	private boolean paramParseFail = false;

	private ActionHook actionHook;

	@Override
	public void action(ActionCode actionCode, Object... param) {
		if (actionHook != null) {
			if (param == null) {
				actionHook.action(actionCode, this);
			} else {
				actionHook.action(actionCode, param);
			}
		}
	}

	@Override
	public void recycle() {
		contentType = null;
		contentLength = -1;

		paramParsed = false;
		paramParseFail = false;
		params.clear();
		headers.clear();
		attributes.clear();
		queryBufferOps = -1;
		if (queryBuffer != null) {
			queryBuffer.clear();
			queryBuffer = null;
		}

	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	public void remove(String key) {
		headers.remove(key);
	}

	public HashMap<String, String> getParam() {
		if (!paramParsed) {
			action(ActionCode.PARSE_PARAMS, null);
		}
		return params;
	}

	public String getContentType() {
		if (contentType == null) {
			contentType = headers.get("content-type");
		}
		return contentType;
	}

	public Charset getEncoding() {
		String contentType = getContentType();
		if (contentType != null) {
			int start = contentType.indexOf("charset=");
			if (start > 0) {
				String encoding = contentType.substring(start + 8);
				return Charset.forName(encoding.trim());
			}
		}
		return StandardCharsets.UTF_8;
	}

	public int getContentLength() {
		if (contentLength > 0) {
			return contentLength;
		}
		String head = getHeader("content-length");
		if (head != null) {
			return Integer.valueOf(head);
		}
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Request headers: \r\n");
		builder.append(method).append(" ");
		builder.append(uri);
		if (queryBuffer != null) {
			queryBuffer.reset();
			builder.append(new String(queryBuffer.array(),0, queryBuffer.remaining()));
		}
		builder.append(" ").append(protocol).append("\r\n");
		for (Map.Entry<String, String> header : headers.entrySet()) {
			builder.append(header.getKey()).append(":").append(header.getValue()).append("\r\n");
		}
		builder.append("\r\n");
		for (Map.Entry<String, String> param : params.entrySet()) {
			builder.append(param.getKey()).append("=").append(param.getValue()).append("\r\n");
		}

		return builder.toString();
	}
}
