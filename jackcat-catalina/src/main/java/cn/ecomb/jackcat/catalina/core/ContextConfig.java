package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.catalina.Lifecycle;
import cn.ecomb.jackcat.utils.digester.Digester;
import lombok.extern.slf4j.Slf4j;


/**
 * @author brian.zhou
 * @date 2020/11/9
 */
@Slf4j
public class ContextConfig implements Lifecycle.LifecycleListener {

	private Context context;
	private boolean deployed = false;

	private Digester webXmlParser;

	@Override
	public void lifecycleEvent(Lifecycle.LifecycleEvent lifecycleEvent) {
		context = (Context) lifecycleEvent.getSource();
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
		webXmlParser = new Digester();


	}

	private void deployApp() {

	}

	private void stop() {

	}
}
