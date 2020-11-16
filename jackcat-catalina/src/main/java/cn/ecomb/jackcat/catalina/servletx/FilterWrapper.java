package cn.ecomb.jackcat.catalina.servletx;

import cn.ecomb.jackcat.catalina.core.Context;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author zhouzg
 * @date 2020-11-06.
 */
@Slf4j
public class FilterWrapper implements FilterConfig {

	private Context context;
	private transient Filter filter = null;

	private String filterClass = null;
	private String filterName = null;

	private List<String> urlPatterns = new ArrayList<>();
	private boolean matchAllUrlPatterns = false;

	public void addUrlPattern(String urlPattern) {
		if ("*".equals(urlPattern)) {
			matchAllUrlPatterns = true;
		} else {
			urlPatterns.add(urlPattern);
		}
	}

	public Filter getFilter() throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, ServletException {
		if (filter != null) {
			return filter;
		}
		ClassLoader classLoader = context.getLoader();
		Class clazz = classLoader.loadClass(filterClass);
		filter = (Filter) clazz.newInstance();
		filter.init(this);
		return filter;
	}

	public FilterWrapper setContext(Context context) {
		this.context = context;
		return this;
	}

	public boolean isMatchAllUrlPatterns() {
		return matchAllUrlPatterns;
	}

	public List<String> getUrlPatterns() {
		return urlPatterns;
	}

	@Override
	public String getFilterName() {
		return filterName;
	}

	@Override
	public ServletContext getServletContext() {
		return context.getAppContext();
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
