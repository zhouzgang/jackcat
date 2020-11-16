package cn.ecomb.jackcat.catalina.servletx;

import cn.ecomb.jackcat.catalina.core.Context;
import cn.ecomb.jackcat.catalina.core.Wrapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 实现责任链模式
 *
 * @author zhouzg
 * @date 2020-11-06.
 */
@Slf4j
public class AppFilterChain implements FilterChain {

	private List<FilterWrapper> filters = new ArrayList<>();
	private Servlet servlet;

	private int pos = 0;
	private int n = 0;

	/**
	 * 根据请求映射 Servlet 创建一个过滤器链
	 *
	 * @return
	 */
	public static AppFilterChain createFilterChain(ServletRequest request, Wrapper wrapper, Servlet servlet) {
		if (servlet == null) {
			return null;
		}
		AppFilterChain chain = new AppFilterChain();
		chain.setServlet(servlet);

		Context context = (Context) wrapper.getParent();
		HashMap<String, FilterWrapper> filters = context.getFilters();
		if (filters == null || filters.size() == 0) {
			return chain;
		}
		String requestPath = wrapper.getWrapperPath();

		for (FilterWrapper filter : filters.values()) {
			if (!matchFilterURL(filter, requestPath)) {
				continue;
			}
			chain.addFilter(filter);
		}

		return chain;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
		// todo 为什么这里要用下标的形式，为什么要在 if 结束时，return 出去，调试时要看看内部运行逻辑
		if (pos < n) {
			FilterWrapper wrapper = filters.get(pos++);
			try {
				Filter filter = wrapper.getFilter();
				filter.doFilter(request, response, this);
			} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
				throw new ServletException("Filter execution threw an exception", e);
			}
			return;
		}

		servlet.service(request, response);
	}

	public void addFilter(FilterWrapper filterWrapper) {
		filters.add(filterWrapper);
		n++;
	}

	public AppFilterChain setServlet(Servlet servlet) {
		this.servlet = servlet;
		return this;
	}

	public void release() {
		filters.clear();
		servlet = null;
		pos = 0;
		n = 0;
	}

	private static boolean matchFilterURL(FilterWrapper filter, String path) {
		if (filter.isMatchAllUrlPatterns()) {
			return true;
		}

		if (path == null) {
			return false;
		}

		List<String> testPaths = filter.getUrlPatterns();
		for (String testPath : testPaths) {
			if (testPath.equals(path)) {
				return true;
			}

			if (testPath.equals("/*")) {
				return true;
			}

			if (testPath.endsWith("/*")) {
				return testPath.regionMatches(0, path, 0, testPath.length() - 2)
						&& (path.length() == testPath.length() - 2
							|| '/' == path.charAt(testPath.length() - 2));
			}

			if (testPath.startsWith("*.")) {
				int slash = path.lastIndexOf('/');
				int period = path.lastIndexOf('.');
				if ((slash >= 0 && period > slash && period != path.length() - 1) &&
						((path.length() - period) == (testPath.length() - 1))) {
					return (testPath.regionMatches(2, path, period + 1, testPath.length() - 2));
				}
			}

		}
		return false;
	}
}
