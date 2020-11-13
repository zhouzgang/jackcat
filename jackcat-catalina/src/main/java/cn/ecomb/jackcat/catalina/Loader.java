package cn.ecomb.jackcat.catalina;

import cn.ecomb.jackcat.catalina.core.Context;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	protected final Map<String, ResourceEntry> resources = new ConcurrentHashMap<>();


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

	public void stop() {
		resources.clear();
	}

	public static class ResourceEntry {
		/**
		 * 0 从其他资源加载
		 * 1 从 WEB-INF/classes 加载的资源
		 */
		public int type = 0;
		/** 最后一次修改，用于热加载 */
		public long lastModified;
		// todo 为什么这里要加 volatile
		public volatile Class<?> loadClass = null;

		public URL source = null;

		public byte[] binaryContent = null;
	}
}
