package cn.ecomb.jackcat.catalina.servletx;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
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
	 *
	 * @return
	 */
	public static AppFilterChain createFilterChain() {
		return null;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {

	}

	public void release() {
		filters.clear();
		servlet = null;
		pos = 0;
		n = 0;
	}
}
