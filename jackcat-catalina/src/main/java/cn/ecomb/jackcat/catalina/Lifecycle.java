package cn.ecomb.jackcat.catalina;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 生命周期抽象类，统一定义周期
 *
 * @author brian.zhou
 * @date 2020/10/28
 */
public abstract class Lifecycle {

	/** 初始化 */
	public abstract void init() throws IOException, SAXException;
	/** 启动 */
	public abstract void start() throws IOException, Exception;
	/** 停止 */
	public abstract void stop();

	/** 观察者模式，事件通知列表 */
	private List<LifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<LifecycleListener>();

	/**
	 * 添加事件监听者
	 * @param listener 监听者
	 */
	public void addLifecycleListener(LifecycleListener listener) {
		lifecycleListeners.add(listener);
	}

	/**
	 * 删除事件监听者
	 * @param listener 监听者
	 */
	public void removeLifecycleListener(LifecycleListener listener) {
		lifecycleListeners.remove(listener);
	}

	public void fireLifecycleEvent(LifecycleEventType type) throws IOException, SAXException {
		fireLifecycleEvent(type, null);
	}

	/**
	 * 通知注册过的监听者
	 * @param type  事件类型
	 * @param data  数据
	 */
	public void fireLifecycleEvent(LifecycleEventType type, Object data) throws IOException, SAXException {
		LifecycleEvent event = new LifecycleEvent(this, type, data);
		for (LifecycleListener listener : lifecycleListeners) {
			listener.lifecycleEvent(event);
		}
	}

	public interface LifecycleListener {
		void lifecycleEvent(LifecycleEvent lifecycleEvent) throws IOException, SAXException;
	}

	/**
	 * 生命周期事件封装对象
	 */
	public static final class LifecycleEvent extends EventObject {
		private static final long serialVersionUID = 0;
		private final Object data;
		private final LifecycleEventType type;

		public LifecycleEvent(Lifecycle lifecycle, LifecycleEventType type, Object data) {
			super(lifecycle);
			this.data = data;
			this.type = type;
		}

		public Object getData() {
			return data;
		}

		public LifecycleEventType getType() {
			return type;
		}

		private Lifecycle getLifecycle() {
			return (Lifecycle) getSource();
		}
	}

	public enum LifecycleEventType {
		/** 初始化 */
		INIT,
		/** 启动 */
		START,
		/** 停止 */
		STOP
	}



}
