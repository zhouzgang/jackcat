package cn.ecomb.jackcat.http;

import java.util.Objects;

/**
 * Http 请求状态
 *
 * @author zhouzg
 * @date 2020-11-03.
 */
public enum HttpStatus {

	SC_COUTINUE(100, "Continue"),
	SC_OK(200, "OK"),
	SC_BAD_REQUEST(400, "Bad Request"),
	SC_NOT_FOUND(404, "Not Found"),
	SC_INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
	SC_NOT_MODIFIED(304, "Not Modified");

	private int code;
	private String msg;

	HttpStatus(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	public static HttpStatus getByCode(int code) {
		for (HttpStatus status : HttpStatus.values()) {
			if (status.getCode() == code) {
				return status;
			}
		}
		return null;
	}
}


