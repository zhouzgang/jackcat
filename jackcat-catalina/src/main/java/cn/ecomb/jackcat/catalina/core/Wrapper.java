package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.catalina.Container;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * 与 servlet 一一对应，用于包装 servlet 的生命周期方法，加载，初始化和销毁
 *
 * @author zhouzg
 * @date 2020-10-29.
 */
public class Wrapper extends Container implements ServletConfig {


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
