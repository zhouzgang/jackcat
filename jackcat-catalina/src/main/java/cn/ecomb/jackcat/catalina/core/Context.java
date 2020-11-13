package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.catalina.Container;
import cn.ecomb.jackcat.catalina.Loader;
import cn.ecomb.jackcat.catalina.servletx.AppContext;
import cn.ecomb.jackcat.catalina.servletx.FilterWrapper;
import cn.ecomb.jackcat.catalina.session.Manager;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 应用程序在内部的表现类，包含 web.xml 配置的参数、Servlet 和 Filter
 *
 * @author zhouzg
 * @date 2020-10-29.
 */
@Data
public class Context extends Container {

    private final Logger log = LoggerFactory.getLogger(Context.class);

    /** 正在热部署 */
    private volatile boolean paused = false;

    /** 应用的名称 */
    private String docBase;
    private String docBasePath;

    private String appBase = "webapp";
    private String welcomeFile = "index.xml";

    public static final String APP_WEB_XML = "WEB-INF/web.xml";
    public static final String RESOURCES_ATTR = "app.resources";

    private Connector connector;
    /** 默认 servlet */
    private Wrapper defaultWrapper;
    private AppContext appContext;
    private WebResource resource;

    /**
     * 容器类加载器
     */
    private ClassLoader parentClassLoader = Context.class.getClassLoader();
    private Manager manager;
    private Loader loader;

    private HashMap<String, FilterWrapper> filters = new HashMap<>();
    private HashMap<String, String> mimeMappings = new HashMap<>();

    /**
     * context-param 配置参数
     * todo 这里需要线程安全
     */
    private final ConcurrentHashMap<String, String> params = new ConcurrentHashMap<>();

    /**
     * 精确匹配 URL，比如 /user
     */
    private TreeMap<String, Wrapper> exactWrappers = new TreeMap<>();
    /**
     * 扩展名匹配，比如 '*.' 为前缀的 URL，*.action。存储 key 为 action
     */
    private TreeMap<String, Wrapper> extensionWrappers = new TreeMap<>();
    /**
     * 模糊匹配，'/*' 结尾的 URL，'/user/*',存储 key 为 /user
     */
    private TreeMap<String, Wrapper> wildcardWrappers = new TreeMap<>();



    public Context() {
        pipeline.setBasic(new ContextBasicValve());
        addLifecycleListener(new ContextConfig());
    }

    @Override
    public void init() {
        defaultWrapper = new Wrapper();
        defaultWrapper.setName("default");
        // 设置默认的 servlet
        defaultWrapper.setServletClass("");
        addChild(defaultWrapper);

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
        loader = new Loader(parentClassLoader, this);
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
        if (loader != null) {
            loader.backgroundProcess();
        }

        if (manager != null) {
            manager.backgroundProcess();
        }
    }

    public void reload() throws IOException {
        setPaused(true);

        children.values().forEach(container -> {
            ((Wrapper)container).stop();
        });

        filters.values().forEach(filterWrapper -> {
            filterWrapper.release();
        });

        loader.stop();
        loader = new Loader(parentClassLoader, this);

        setPaused(false);
    }

    /**
     * 获取文件真实绝对路径
     *
     * @param path 相对路径
     * @return
     */
    public String getRealPath(String path) {
        File file = new File(getDocBasePath(), path);
        return file.getAbsolutePath();
    }

    public String getDocBasePath() {
        if (docBasePath == null) {
            Path base = Paths.get(System.getProperty("jackcat.base"), appBase, docBase);
            docBasePath = base.toAbsolutePath().toString();
        }
        return docBasePath;
    }

    public String findMineMapping(String extension) {
        return mimeMappings.get(extension);
    }
}
