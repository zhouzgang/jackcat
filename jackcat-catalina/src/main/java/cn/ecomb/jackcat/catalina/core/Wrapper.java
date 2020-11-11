package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.catalina.Container;
import cn.ecomb.jackcat.catalina.Loader;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Enumeration;

/**
 * 与 servlet 一一对应，用于包装 servlet 的生命周期方法，加载，初始化和销毁
 *
 * @author zhouzg
 * @date 2020-10-29.
 */
@Slf4j
@Data
public class Wrapper extends Container implements ServletConfig {

    private volatile Servlet servlet;
    private String servletClass;

    public Wrapper() {
        pipeline.setBasic(new WrapperBasicValve());
    }

    /**
     * 创建 servlet 单例
     * @return
     */
    public Servlet buildServlet() throws ServletException {
        if (servlet == null) {
            synchronized (Wrapper.class) {
                if (servlet != null) {
                    servlet = loadServlet();
                }
            }
        }
        return servlet;
    }

    public Servlet loadServlet() throws ServletException {
        Servlet servlet = null;
        Class clazz;
        Context context = (Context) getParent();
        Loader loader = context.getLoader();

        try {
            if (loader != null) {
                clazz = loader.loadClass(servletClass);
            } else {
                clazz = Class.forName(servletClass);
            }

            servlet = (Servlet) clazz.newInstance();
            servlet.init(this);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return servlet;
    }

    @Override
    public void init() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void startInternal() throws Exception {

    }

    @Override
    public void backgroundProcess() {

    }

    @Override
    public String getServletName() {
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration getInitParameterNames() {
        return null;
    }
}
