package cn.ecomb.jackcat.catalina;

import java.util.HashMap;

/**
 * 容器抽象类
 * 容器类的需求是什么？
 *
 * @author brian.zhou
 * @date 2020/10/28
 */
public abstract class Container extends Lifecycle{

	/** 容器名字 */
	protected String name;

	/** 父容器 */
	protected Container parent;

	/** 子容器 */
	protected HashMap<String, Container> children = new HashMap<>();

	/** 管道 */
	protected Pipeline pipeline = new Pipeline(this);


}
