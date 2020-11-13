package cn.ecomb.jackcat.catalina.servletx;

import cn.ecomb.jackcat.catalina.core.Context;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouzg
 * @date 2020-11-06.
 */
public class AppContext implements ServletContext {

	private Context context;

	protected Map<String, Object> attributes = new ConcurrentHashMap<>();

	public AppContext(Context context) {
		this.context = context;
	}

	@Override
	public String getContextPath() {
		return context.getDocBasePath();
	}

	@Override
	public ServletContext getContext(String uripath) {
		return null;
	}

	@Override
	public int getMajorVersion() {
		return 0;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public String getMimeType(String file) {
		if (file == null || "".equals(file)) {
			return null;
		}
		int period = file.lastIndexOf(".");
		if (period < 0)
			return null;
		String extension = file.substring(period + 1);
		if (extension.length() < 1)
			return null;
		return (context.findMineMapping(extension));
	}

	@Override
	public Set getResourcePaths(String path) {
		return null;
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		return null;
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		return null;
	}

	@Override
	public Enumeration getServlets() {
		return null;
	}

	@Override
	public Enumeration getServletNames() {
		return null;
	}

	@Override
	public void log(String msg) {

	}

	@Override
	public void log(Exception exception, String msg) {

	}

	@Override
	public void log(String message, Throwable throwable) {

	}

	@Override
	public String getRealPath(String path) {
		return null;
	}

	@Override
	public String getServerInfo() {
		return null;
	}

	@Override
	public String getInitParameter(String name) {
		return context.getParams().get(name);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		return null;
	}

	@Override
	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public String getServletContextName() {
		return context.getDocBase();
	}
}
