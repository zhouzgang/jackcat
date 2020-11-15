package cn.ecomb.jackcat.catalina.core;

import lombok.Data;
import lombok.Getter;

import java.io.*;
import java.net.CacheResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存 web 静态资源
 *
 * @author brian.zhou
 * @date 2020/11/9
 */
public class WebResource {

	private Context context;

	public WebResource(Context context) {
		this.context = context;
	}

	private final ConcurrentHashMap<String, CacheResource> resourceCache = new ConcurrentHashMap<>();

	public CacheResource getResource(String path) {
		CacheResource resource = resourceCache.get(path);

		if (resource != null && !resource.validateResource()) {
			resourceCache.remove(path);
			resource = null;
		}

		if (resource == null) {
			resource = new CacheResource(path);
			resourceCache.put(path, resource);
			resource.validateResource();
		}
		return resource;
	}

	@Data
	public class CacheResource {
		private File resource = null;
		private String path;

		private long ttl = 5000;
		private long nextCheck;

		private long cacheLastModified = -1L;
		private long cacheContentLength = -1L;

		private byte[] cachedContent;
		private String mimeType;
		private String weakETag;

		public CacheResource(String path) {
			this.path = path;
		}

		public final String getWeakETag() {
			if (weakETag == null) {
				synchronized (this) {
					if (weakETag == null) {
						long contentLength = getCacheContentLength();
						long lastModified = getCacheLastModified();
						if (contentLength >= 0 || lastModified >= 0) {
							weakETag = "w/\"" + contentLength + "-" + lastModified + "\"";
						}
					}
				}
			}
			return weakETag;
		}

		protected boolean validateResource() {
			long now = System.currentTimeMillis();
			if (resource == null) {
				resource = new File(context.getRealPath(path));
				cacheLastModified = resource.lastModified();
				cacheContentLength = resource.length();
				cachedContent = cacheLoad();
			} else {
				if (now > nextCheck) {
					if (resource.lastModified() != getCacheLastModified()
							|| resource.length() != getCacheContentLength()) {
						return false;
					}
				}
			}
			nextCheck = ttl + now;
			return true;
		}

		/**
		 * 缓存小于 512KB 的静态资源字节数组
		 * @return
		 */
		private byte[] cacheLoad() {
			if (cacheContentLength > 0 && cacheContentLength < 512 * 1024) {
				int size = (int) cacheContentLength;
				byte[] result = new byte[size];
				int pos = 0;
				try {
					InputStream is = new FileInputStream(resource);
					while (pos < size) {
						int n = is.read(result, pos, size - pos);
						if (n < 0) {
							break;
						}
						pos += n;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
				return result;
			}
			return null;
		}

		/**
		 * 超过 512KB 的资源，使用 InputStream 读取
		 * @return
		 */
		public InputStream getInputStream() {
			if (cachedContent != null) {
				return new ByteArrayInputStream(cachedContent);
			} else {
				try {
					return new FileInputStream(resource);
				} catch (FileNotFoundException e) {
					return null;
				}
			}
		}

		public boolean exists() {
			return resource.exists();
		}

		public boolean isDirectory() {
			return resource.isDirectory();
		}

		public String getName() {
			return resource.getName();
		}

		public final String getLastModifiedHttp() {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			return sdf.format(new Date(getLastModifiedHttp()));
		}

	}

}
