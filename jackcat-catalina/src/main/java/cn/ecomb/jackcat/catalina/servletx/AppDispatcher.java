package cn.ecomb.jackcat.catalina.servletx;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author zhouzg
 * @date 2020-11-06.
 */
public class AppDispatcher implements RequestDispatcher {

	@Override
	public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {

	}

	@Override
	public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {

	}
}
