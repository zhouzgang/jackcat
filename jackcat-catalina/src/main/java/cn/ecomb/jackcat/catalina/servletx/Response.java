package cn.ecomb.jackcat.catalina.servletx;

import cn.ecomb.jackcat.http.JackResponse;
import cn.ecomb.jackcat.http.Recyclable;
import lombok.Data;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * 返回体
 * @author brian.zhou
 * @date 2020/10/28
 */
@Data
public class Response implements HttpServletResponse, Recyclable {

	private JackResponse jackResponse;
	private Request request;

	private AppOutputBuffer outputBuffer;
	private PrintWriter printWriter;

	private boolean error = false;
	private boolean usingOutputStream = false;
	private boolean usingWriter = false;

	public void finish() throws IOException {
		outputBuffer.close();
	}


	public void setSuspended(boolean suspended) {
		outputBuffer.setSuspended(suspended);
	}

	public int getStatus() {
		return jackResponse.getStatus();
	}

	public String getMessage() {
		return jackResponse.getMessage();
	}

	public PrintWriter getReporter() {
		if (outputBuffer.isNew()) {
			if (printWriter == null) {
				printWriter = new PrintWriter(new RespWriter(outputBuffer));
			}
			return printWriter;
		} else {
			return null;
		}

	}

	@Override
	public void recycle() {

	}

	@Override
	public void addCookie(Cookie cookie) {

	}

	@Override
	public boolean containsHeader(String name) {
		return false;
	}

	@Override
	public String encodeURL(String url) {
		return null;
	}

	@Override
	public String encodeRedirectURL(String url) {
		return null;
	}

	@Override
	public String encodeUrl(String url) {
		return null;
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return null;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		if (isCommitted()) {
			throw new IllegalStateException("Cannot call sendError() after the response has been committed");
		}

		setStatus(sc);
		error = true;
		jackResponse.setStatus(sc);
		jackResponse.setMessage(msg);
		outputBuffer.recycle();
		setSuspended(true);
	}

	@Override
	public void sendError(int sc) throws IOException {
		sendError(sc, null);
	}

	@Override
	public void sendRedirect(String location) throws IOException {

	}

	@Override
	public void setDateHeader(String name, long date) {

	}

	@Override
	public void addDateHeader(String name, long date) {

	}

	@Override
	public void setHeader(String name, String value) {

	}

	@Override
	public void addHeader(String name, String value) {

	}

	@Override
	public void setIntHeader(String name, int value) {

	}

	@Override
	public void addIntHeader(String name, int value) {

	}

	@Override
	public void setStatus(int sc) {

	}

	@Override
	public void setStatus(int sc, String sm) {

	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (usingOutputStream) {
			throw new IllegalStateException("getOutputStream() has already been called for this response");
		}
		usingWriter = true;
		if (printWriter == null) {
			printWriter = new PrintWriter(new RespWriter(outputBuffer));
		}
		return printWriter;
	}

	@Override
	public void setCharacterEncoding(String charset) {

	}

	@Override
	public void setContentLength(int len) {

	}

	@Override
	public void setContentType(String type) {

	}

	@Override
	public void setBufferSize(int size) {

	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {

	}

	@Override
	public void resetBuffer() {

	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {

	}

	@Override
	public void setLocale(Locale loc) {

	}

	@Override
	public Locale getLocale() {
		return null;
	}
}
