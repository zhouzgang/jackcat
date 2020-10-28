package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.catalina.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 应用程序在内部的表现类，包含 web.xml 配置的参数、Servlet 和 Filter
 *
 * @author zhouzg
 * @date 2020-10-29.
 */
public class Context extends Container {

    private final Logger log = LoggerFactory.getLogger(Context.class);

    private Connector connector;

    @Override
    public void init() {

    }

    @Override
    public void stop() {

    }
}
