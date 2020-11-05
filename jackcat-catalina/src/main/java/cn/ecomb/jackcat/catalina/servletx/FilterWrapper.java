package cn.ecomb.jackcat.catalina.servletx;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * @author zhouzg
 * @date 2020-11-06.
 */
public class FilterWrapper implements FilterConfig {

	@Override
	public String getFilterName() {
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public String getInitParameter(String name) {
		return null;
	}

	@Override
	public Enumeration getInitParameterNames() {
		return null;
	}
}
