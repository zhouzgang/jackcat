package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.catalina.Valve;
import cn.ecomb.jackcat.catalina.servletx.AppFilterChain;
import cn.ecomb.jackcat.catalina.servletx.Request;
import cn.ecomb.jackcat.catalina.servletx.Response;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Wrapper 处理通道的最后一步，加载对应的 Servlet，创建 FilterChain，调用 Servce.service 方法
 *
 * @author zhouzg
 * @date 2020-11-11.
 */
@Slf4j
public class WrapperBasicValve extends Valve {

	@Override
	public void invoke(Request request, Response response) {
		Wrapper wrapper = request.getWrapper();

		Servlet servlet = null;
		try {
			servlet = wrapper.buildServlet();
		} catch (ServletException e) {
			log.error("创建 servlet 报错");
			request.setAttribute("javax.servlet.exception", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		AppFilterChain filterChain = AppFilterChain.createFilterChain(request, wrapper, servlet);

		if (servlet != null && filterChain != null) {
			try {
				filterChain.doFilter(request, response);
			} catch (IOException | ServletException e) {
				log.error("执行 Service.service 方法报错");
				request.setAttribute("javax.servlet.exception", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

			}finally {
				filterChain.release();
			}
		}

	}
}
