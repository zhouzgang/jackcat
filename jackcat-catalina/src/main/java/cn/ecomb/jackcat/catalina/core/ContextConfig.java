package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.catalina.Lifecycle;
import cn.ecomb.jackcat.utils.digester.Digester;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


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
	public void lifecycleEvent(Lifecycle.LifecycleEvent lifecycleEvent) throws IOException, SAXException {
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
		webXmlParser.setClassLoader(context.getLoader());

		webXmlParser.addCallMethod("web-app/context-param","addParameter", 2);
		webXmlParser.addCallParam("web-app/context-param/param-name", 0);
		webXmlParser.addCallParam("web-app/context-param/param-value", 1);

		webXmlParser.addCallMethod("web-app/distributable", "setDistributable", 0, new Class[]{Boolean.TYPE});

		webXmlParser.addObjectCreate("web-app/filter", "com.dunwoo.tomcat.container.servletx.FilterWrapper");
		webXmlParser.addCallMethod("web-app/filter/filter-class", "setFilterClass", 0);
		webXmlParser.addCallMethod("web-app/filter/filter-name", "setFilterName", 0);
		webXmlParser.addSetNext("web-app/filter","addFilterWrapper");

		webXmlParser.addCallMethodMultiRule("web-app/filter-mapping","addFilterMapping", 2, 1);
		webXmlParser.addCallParam("web-app/filter-mapping/filter-name",0);
		webXmlParser.addCallParamMultiRule("web-app/filter-mapping/url-pattern", 1);

		webXmlParser.addObjectCreate("web-app/servlet","com.dunwoo.tomcat.container.core.Wrapper");
		webXmlParser.addCallMethod("web-app/servlet/servlet-class", "setServletClass", 0);
		webXmlParser.addCallMethod("web-app/servlet/servlet-name", "setName", 0);
		webXmlParser.addSetNext("web-app/servlet", "addChild", "com.dunwoo.tomcat.container.Container");

		webXmlParser.addCallMethodMultiRule("web-app/servlet-mapping","addServletMapping", 2, 1);
		webXmlParser.addCallParam("web-app/servlet-mapping/servlet-name", 0);
		webXmlParser.addCallParamMultiRule("web-app/servlet-mapping/url-pattern", 1);

	}

	private void deployApp() throws IOException, SAXException {
		File appBase = new File(System.getProperty("jackcat.base"), context.getAppBase());
		File[] apps = appBase.listFiles();
		if (apps == null || apps.length == 0) {
			throw new IllegalArgumentException("必须在[" + System.getProperty("jackcat.base") + "]部署且只能部署 一个 web 应用才能启动");
		}

		if (apps.length > 1) {
			throw new IllegalArgumentException("只支持部署一个 web 应用");
		}

		if (!deployed) {
			File docBase = apps[0];

			context.setDocBase(docBase.getName());
			context.setDocBasePath(docBase.getAbsolutePath());

			File webXml = new File(docBase, Context.APP_WEB_XML);
			InputSource in = new InputSource(new FileInputStream(webXml));
			webXmlParser.push(context);
			webXmlParser.parse(in);
			deployed = true;
			log.info("部署 web 应用：{}", context.getDocBase());
		}

	}

	private void stop() {

	}
}
