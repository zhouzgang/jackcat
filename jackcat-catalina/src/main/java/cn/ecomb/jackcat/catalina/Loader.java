package cn.ecomb.jackcat.catalina;

import cn.ecomb.jackcat.catalina.core.Context;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 加载服务 jar 包
 *
 * @author brian.zhou
 * @date 2020/11/9
 */
public class Loader extends URLClassLoader {

	private Context context;
	private ClassLoader javaseClassLoader;


	public Loader(ClassLoader parent, Context context) {
		super(new URL[0], parent);
		this.context = context;
//		this.javaseClassLoader

	}

	public void backgroundProcess() {

	}
}
