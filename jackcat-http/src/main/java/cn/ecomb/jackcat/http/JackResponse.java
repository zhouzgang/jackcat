package cn.ecomb.jackcat.http;

import lombok.Data;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * 原始的 Http 响应对象
 *
 * @author zhouzg
 * @date 2020-10-29.
 */
@Data
public class JackResponse implements Recyclable, ActionHook{

	private boolean committed = false;

	private int status = 200;
	private String message;
	private String contentType = null;
	private String contentLanguage = null;
	private int contentLength = -1;
	private String characterEncoding = "utf-8";
	private HashMap<String, String> headers = new HashMap<>();

	private ActionHook actionHook;

	@Override
	public void action(ActionCode code, Object... param) {
		if (actionHook != null) {
			if (param == null) {
				actionHook.action(code, this);
			} else {
				actionHook.action(code, param);
			}
		}
	}

	/**
	 * 写入响应体数据
	 * @param byteBuffer
	 */
	public void doWrite(ByteBuffer byteBuffer) {
		action(ActionCode.WRITE_BODY, byteBuffer);
	}

	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public String getContentType() {
		String ret = contentType;
		if (ret != null && characterEncoding != null) {
			ret = ret + ";charset=" + characterEncoding;
		}
		return ret;
	}

	@Override
	public void recycle() {
		committed = false;
		contentType = null;
		contentLength = -1;
		headers.clear();
		status = 200;
		message = "";
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Response headers: \r\n");
		builder.append("HTTP/1.1 ");
		builder.append(status);
		builder.append(HttpStatus.getByCode(status).getMsg()).append("\r\n");
		return builder.toString();
	}

	public void reset() throws IllegalStateException {
		if (committed) {
			throw new IllegalStateException();
		}
		recycle();
	}
}
