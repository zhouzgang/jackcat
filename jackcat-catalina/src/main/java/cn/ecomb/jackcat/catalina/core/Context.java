package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.catalina.Container;
import cn.ecomb.jackcat.catalina.Loader;
import cn.ecomb.jackcat.catalina.servletx.FilterWrapper;
import cn.ecomb.jackcat.catalina.session.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * 应用程序在内部的表现类，包含 web.xml 配置的参数、Servlet 和 Filter
 *
 * @author zhouzg
 * @date 2020-10-29.
 */
public class Context extends Container {

    private final Logger log = LoggerFactory.getLogger(Context.class);

    public static final String APP_WEB_XML = "WEB-INF/web.xml";
    public static final String RESOURCES_ATTR = "app.resources";

    private Connector connector;
    /** 默认 servlet */
    private Wrapper defaultWrapper;
    private WebResource resource;
    private Manager manager;
    private Loader loader;

    private HashMap<String, FilterWrapper> filters = new HashMap<>();
    private HashMap<String, String> mimeMappings = new HashMap<>();


    public Context() {
        pipeline.setBasic(new ContextBasicValve());
        addLifecycleListener(new ContextConfig());
    }

    @Override
    public void init() {
        defaultWrapper = new Wrapper();

        resource = new WebResource();


        mimeMappings.put("css","text/css");
        mimeMappings.put("exe","application/octet-stream");
        mimeMappings.put("gif","image/gif");
        mimeMappings.put("htm","text/html");
        mimeMappings.put("html","text/html");
        mimeMappings.put("ico","image/x-icon");
        mimeMappings.put("jpe","image/jpeg");
        mimeMappings.put("jpeg","image/jpeg");
        mimeMappings.put("jpg","image/jpeg");
        mimeMappings.put("js","application/javascript");
        mimeMappings.put("json","application/json");
        mimeMappings.put("png","image/png");
        mimeMappings.put("svg","image/svg+xml");
        mimeMappings.put("txt","text/plain");
        mimeMappings.put("xml","application/xml");

        fireLifecycleEvent(LifecycleEventType.INIT);
    }

    @Override
    public void startInternal() throws Exception {
        // 添加一个用于报错的阀门
        pipeline.addValve(new ErrorReportValve());
        // 管理 session
        manager = new Manager();
        // 初始化 web 应用 jar 的加载器
        loader = new Loader();
        // 初始化并启动连接器
        connector = new Connector();
        connector.setContext(this);
        connector.start();
    }

    @Override
    public void stop() {
        connector.stop();
        filters.values().forEach(filterWrapper -> {
            filterWrapper.release();
        });
        filters.clear();
    }

    @Override
    public void backgroundProcess() {

    }
}
