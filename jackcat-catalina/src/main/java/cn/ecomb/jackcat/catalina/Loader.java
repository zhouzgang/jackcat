package cn.ecomb.jackcat.catalina;

import cn.ecomb.jackcat.catalina.core.Context;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;

/**
 * 加载服务 jar 包
 *
 * @author brian.zhou
 * @date 2020/11/9
 */
public class Loader extends URLClassLoader {

	private Context context;
	private ClassLoader javaseClassLoader;

	private String classesPath;
	private String libraryPath;


	public Loader(ClassLoader parent, Context context) throws IOException {
		super(new URL[0], parent);
		this.context = context;
		this.javaseClassLoader = getSystemClassLoader();
		init();
	}

	public void init() throws IOException {
		classesPath = context.getRealPath("/WEB-INF/classes");
		libraryPath = context.getRealPath("/WEB-INF/lib");

		File webLib = new File(libraryPath);
		if (webLib.exists()) {
			addURL(new URL(webLib.toURI().toString()));
			String[] fileNames = webLib.list();
			if (fileNames != null) {
				for (int i = 0; i < fileNames.length; i++) {
					String fileName = fileNames[i].toLowerCase(Locale.ENGLISH);
					if (!fileName.endsWith(".jar")) {
						continue;
					}
					File file = new File(webLib, fileNames[i]);
					file = file.getCanonicalFile();
					addURL(new URL(file.toURI().toString()));
				}
			}
		}
	}

	public void backgroundProcess() {

	}
}
