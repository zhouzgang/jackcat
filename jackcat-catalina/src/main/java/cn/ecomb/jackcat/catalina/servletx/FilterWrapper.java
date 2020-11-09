package cn.ecomb.jackcat.catalina.servletx;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * @author zhouzg
 * @date 2020-11-06.
 */
@Slf4j
public class FilterWrapper implements FilterConfig {

	private transient Filter filter = null;

	private String filterClass = null;
	private String filterName = null;

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

	public void release() {
		if (filter != null) {
			log.debug("destroy filter: {}", filterClass);
			filter.destroy();
			filter = null;
		}
	}
}
