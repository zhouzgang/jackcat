package cn.ecomb.jackcat.http;

import lombok.Data;

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

	@Override
	public void recycle() {

	}
}
