package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.catalina.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author brian.zhou
 * @date 2020/11/9
 */
public class ContextConfig implements Lifecycle.LifecycleListener {

	private Logger logger = LoggerFactory.getLogger(ContextConfig.class);

	@Override
	public void lifecycleEvent(Lifecycle.LifecycleEvent lifecycleEvent) {
		switch (lifecycleEvent.getType()) {
			case INIT:
				init();
				break;
			case START:
				deployApp();
				break;
			case STOP:
				stop();
				break;
			default:
				break;
		}
	}

	private void init() {

	}

	private void deployApp() {

	}

	private void stop() {

	}
}
