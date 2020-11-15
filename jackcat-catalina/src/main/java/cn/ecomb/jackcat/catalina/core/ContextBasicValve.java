package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.catalina.Valve;
import cn.ecomb.jackcat.catalina.servletx.Request;
import cn.ecomb.jackcat.catalina.servletx.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author brian.zhou
 * @date 2020/11/9
 */
public class ContextBasicValve extends Valve {
	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		String requestUri = request.getRequestURI();
		if (requestUri.startsWith("/META-INF/", 0)
				|| requestUri.equalsIgnoreCase("/META-INF")
				|| requestUri.startsWith("/WEB-INF/", 0)
				|| requestUri.equalsIgnoreCase("/META-INF")) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		Context context = request.getContext();
		// todo 为什么要在这里切换上下文的类加载器
		ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(context.getLoader());

		Wrapper wrapper = request.getWrapper();
		wrapper.getPipeline().handle(request, response);

		Thread.currentThread().setContextClassLoader(oldClassLoader);
	}
}
