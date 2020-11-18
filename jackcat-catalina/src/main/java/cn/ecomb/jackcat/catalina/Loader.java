package cn.ecomb.jackcat.catalina;

import cn.ecomb.jackcat.catalina.core.Context;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加载服务 jar 包
 * 这里要多考虑一下，像支付->支付第三方这种以扩展插件的形式解耦，需要用到类的灵活加载。
 *
 * @author brian.zhou
 * @date 2020/11/9
 */
@Slf4j
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
			//todo 这种是在干什么？
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
		boolean modified = false;

		for (Map.Entry<String, ResourceEntry> entry: resources.entrySet()){
			if (entry.getValue().type == 1) {
				long cachedLastModified = entry.getValue().lastModified;
				long lastModified = new File(classesPath, entry.getKey()).lastModified();
				if (lastModified != cachedLastModified) {
					log.debug("资源 {} 已经被加载，加载过的时间 {}, 现在时间",
							entry.getKey(), cachedLastModified, lastModified);
					modified = true;
					break;
				}
			}
		}

		// 检查是否有 jar 添加和删除
		if (modified) {
			try {
				context.reload();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		resources.clear();
	}


	public ClassLoader getClassLoader() {
		return this;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return super.loadClass(name);
	}

	/**
	 * 打破双亲委派机制
	 *
	 * @param name
	 * @param resolve
	 * @return
	 * @throws ClassNotFoundException
	 */
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = null;

		// 检查本地缓存是否已经加载
		ResourceEntry entry = resources.get(name);
		if (entry != null) {
			clazz = entry.loadedClass;
		}

		// 检查是否已经被虚拟机加载
		if (clazz != null) {
			if (resolve) {
				resolveClass(clazz);
			}
			return clazz;
		}

		// 尝试使用系统类加载器加载，防止覆盖公共类，比如 rt.jar
		javaseClassLoader.loadClass(name);
		if (clazz != null) {
			if (resolve) {
				resolveClass(clazz);
			}
			return clazz;
		}

		clazz = findClass(name);
		if (clazz != null) {
			if (resolve) {
				resolveClass(clazz);
			}
			return clazz;
		}

		if (clazz == null) {
			clazz = getParent().loadClass(name);
			if (clazz != null) {
				if (resolve) {
					resolveClass(clazz);
				}
				return clazz;
			}
		}

		throw new ClassNotFoundException(name);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> clazz = null;
		clazz = findClassInternal(name);

		if (clazz == null) {
			clazz = super.findClass(name);
		}
		return clazz;
	}

	public Class<?> findClassInternal(String name) {
		ResourceEntry resource = resources.get(name);
		Class<?> clazz = null;

		if (resource == null) {
			String path = nameToPath(name);
			Path classFile = Paths.get(classesPath, path);
			if (Files.exists(classFile)) {
				resource = new ResourceEntry();
				resource.type = 1;
				resources.put(path, resource);

				try {
					InputStream is = Files.newInputStream(Paths.get(classesPath, path));
					byte[] content = new byte[is.available()];
					is.read(content);

					clazz = defineClass(name, content, 0, content.length);

					resource.lastModified = classFile.toFile().lastModified();
					resource.loadedClass = clazz;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			clazz = resource.loadedClass;
		}
		return clazz;
	}

	private String nameToPath(String name) {
		return name.replace('.', '/').concat(".class");
	}

	@Override
	public URL getResource(String name) {
		URL url = findResource(name);

		if (url == null) {
			url = getParent().getResource(name);
		}
		return url;
	}

	@Override
	public URL findResource(String name) {
		URL url = null;
		ResourceEntry resource = resources.get(name);
		if (resource == null) {

			Path path = Paths.get(classesPath, name);
			if (Files.exists(path)) {
				resource = new ResourceEntry();
				resources.put(name, resource);

				try {
					url = path.toUri().toURL();
					resource.source = url;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		} else {
			url = resource.source;
		}

		if (url == null) {
			url = super.findResource(name);
		}
		return url;
	}

	/**
	 * 缓存的已加载类资源
	 */
	public static class ResourceEntry {
		/**
		 * 0 从其他资源加载
		 * 1 从 WEB-INF/classes 加载的资源
		 */
		public int type = 0;
		/** 最后一次修改，用于热加载 */
		public long lastModified;
		// todo 为什么这里要加 volatile
		public volatile Class<?> loadedClass = null;

		public URL source = null;

		public byte[] binaryContent = null;
	}

	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer("WebappClassLoader\r\n");
		sb.append("  context: /");
		sb.append(context.getDocBase());
		sb.append("\r\n");
		sb.append("  repositories:\r\n");
		URL[] repositories = getURLs();
		if (repositories != null) {
			for (int i = 0; i < repositories.length; i++) {
				sb.append("    ");
				sb.append(repositories[i]);
				sb.append("\r\n");
			}
		}

		sb.append("----------> Parent Classloader:\r\n");
		sb.append(getParent().toString());
		sb.append("\r\n");
		return (sb.toString());

	}
}
