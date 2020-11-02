package cn.ecomb.jackcat.http;

/**
 * Http 协议标识符
 *
 * @author zhouzg
 * @date 2020-11-03.
 */
public class HttpTag {

	public static final byte CR = '\r';
	public static final byte LF = '\n';
	public static final byte SP = ' ';
	public static final byte COLON = ':';
	public static final byte SEMI_COLON = ';';
	public static final byte QUESTION = '?';

	public static final byte[] HTTP_1_1 = "HTTP/1.1".getBytes();
	public static final byte[] CRLF = "\r\n".getBytes();
	public static final byte[] END_CHUNK = "0\r\n\r\n".getBytes();

}
