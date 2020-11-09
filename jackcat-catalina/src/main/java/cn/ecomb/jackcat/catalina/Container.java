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
	protected HashMap<String, Container> children = new HashMap<String, Container>();
	/** 管道 */
	protected Pipeline pipeline = new Pipeline(this);

	/** 维持容器的后台线程 */
	private Thread thread;
	/** 线程是否关闭 */
	protected boolean threadDone = false;
	/** 线程唤醒执行时间段 */
	private int backgroundProcessorDelay = 5;

	/**
	 * 初始化
	 * 启动子容器
	 * 启动本容器的后台线程
	 */
	@Override
	public void start() throws Exception {
		fireLifecycleEvent(LifecycleEventType.START);

		startInternal();

		children.values().forEach(container -> {
			if (container != null) {
				try {
					container.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		if (thread == null && backgroundProcessorDelay > 0) {
			threadDone = false;
			String threadName = "Container-daemon[" + getName() + "]";
			thread = new Thread(new ContainerBackGroundThread(), threadName);
			thread.setDaemon(true);
			thread.start();
		}
	}

	/**
	 * 初始化容器内部操作
	 */
	public abstract void startInternal() throws Exception;

	/**
	 * 后台业务处理线程
	 * 为什么容器实现 Runnable 接口
	 */
	protected class ContainerBackGroundThread implements Runnable{
		@Override
		public void run() {
			while (!threadDone) {
				backgroundProcess();
				try {
					Thread.sleep(backgroundProcessorDelay * 1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 容器业务处理
	 */
	public abstract void backgroundProcess();

	public String getName() {
		return name;
	}

	public Container findChildren(String name) {
		return children.get(name);
	}

	public void addValve(Valve valve) {
		pipeline.addValve(valve);
	}

	public Pipeline getPipeline() {
		return pipeline;
	}
}
