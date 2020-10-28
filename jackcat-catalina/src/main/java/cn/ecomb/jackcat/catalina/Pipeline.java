package cn.ecomb.jackcat.catalina;

import cn.ecomb.jackcat.catalina.session.Request;
import cn.ecomb.jackcat.catalina.session.Response;

/**
 * 管道，配合阀门形成有固定为节点的阀门链
 *
 * @author brian.zhou
 * @date 2020/10/28
 */
public class Pipeline {

	protected Container container = null;

	/** 基础阀门，也就是最后一个阀门 */
	private Valve basic = null;
	private Valve first = null;

	public Pipeline(Container container) {
		this.container = container;
	}

	/**
	 * 依次调用阀门，处理相关逻辑
	 * @param request   请求对象
	 * @param response  返回对象
	 */
	public void handle(Request request, Response response) {
		if (first != null) {
			first.invoke(request, response);
		} else {
			basic.invoke(request, response);
		}
	}

	/**
	 * 设置基础阀门
	 * @param newBasic 新的尾阀门
	 */
	public void setBasic(Valve newBasic) {
		newBasic.setContainer(container);
		if (basic == newBasic) {
			return;
		}
		Valve current = first;
		while (current != null) {
			if (current.getNext() == basic) {
				current.setNext(newBasic);
				break;
			}
			current = current.getNext();
		}
		basic = newBasic;
	}

	/**
	 * 添加阀门，正序最后插入新加阀门
	 * @param valve 新加阀门
	 */
	public void addValve(Valve valve) {
		if (first == null) {
			first = valve;
			first.setNext(basic);
			return;
		}

		Valve current = first;
		while (current != null) {
			if (current.getNext() == basic) {
				current.setNext(valve);
				valve.setNext(basic);
				break;
			}
			current = current.getNext();
		}
	}

}
