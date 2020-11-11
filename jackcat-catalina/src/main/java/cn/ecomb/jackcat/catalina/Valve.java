package cn.ecomb.jackcat.catalina;

import cn.ecomb.jackcat.catalina.servletx.Request;
import cn.ecomb.jackcat.catalina.servletx.Response;

import javax.servlet.ServletException;

/**
 * 阀门抽象类，每个实现类都有机会处理请求，判断是否继续走下去
 *
 * @author brian.zhou
 * @date 2020/10/28
 */
public abstract class Valve {

	/** 属于那个容器的阀门 */
	protected Container container = null;

	/** 下一个阀门 */
	private Valve next = null;

	public Valve getNext() {
		return next;
	}

	public void setNext(Valve next) {
		this.next = next;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	/**
	 * 调用阀门功能，检查或修改 Request 和 Response，
	 * 如果没到最后生成 Response ，则调用下一个阀门
	 *
	 * @param request   请求对象
	 * @param response  返回对象
	 */
	public abstract void invoke(Request request, Response response) throws ServletException;
}
