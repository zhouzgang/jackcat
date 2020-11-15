package cn.ecomb.jackcat.catalina.servletx;

import cn.ecomb.jackcat.catalina.core.Context;
import cn.ecomb.jackcat.catalina.core.WebResource;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 默认 servlet，用于处理静态资源。
 *
 * @author zhouzg
 * @date 2020-11-06.
 */
@Slf4j
public class DefaultServlet extends HttpServlet {

	protected int sendFileSize = 48 * 1024;
	protected boolean listings = false;
	protected transient WebResource resources = null;

	@Override
	public void init() throws ServletException {
		resources = (WebResource) getServletContext().getAttribute(Context.RESOURCES_ATTR);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getServletPath();
		WebResource.CacheResource resource = resources.getResource(path);

		if (!resource.exists()) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		if (resource.isDirectory()) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		} else {
			if (!checkIfHeaders(req, resp, resource)) {
				return;
			}

			String contentType = resource.getMimeType();
			if (contentType == null) {
				contentType = getServletContext().getMimeType(resource.getName());
				resource.setMimeType(contentType);
			}

			String eTag = resource.getWeakETag();
			String lastModifiedHttp = resource.getLastModifiedHttp();

			resp.setHeader("ETag", eTag);
			resp.setHeader("Last-Modified", lastModifiedHttp);
		}

		long contentLength = resource.getCacheContentLength();
		if (contentLength > 0) {
			resp.setContentType(resource.getMimeType());
			ServletOutputStream outputStream = resp.getOutputStream();
			if (!checkSendFile()) {
				byte[] resourceBody = resource.getCachedContent();
				if (resourceBody == null) {
					InputStream is = resource.getInputStream();
					InputStream inputStream = new BufferedInputStream(is, 2048);
					byte buffer[] = new byte[2048];
					int len = buffer.length;
					while (true) {
						len = inputStream.read(buffer);
						if (len == -1) {
							break;
						}
						outputStream.write(buffer, 0, len);
					}
				} else {
					outputStream.write(resourceBody);
				}
			}
		}
	}

	/**
	 * 校验 if-match, if-none-match, if-modified-since, if-unmodified-since 请求头域值
	 */
	private boolean checkIfHeaders(HttpServletRequest request, HttpServletResponse response, WebResource.CacheResource resource) {
		String ifMatch = request.getHeader("if-match");
		if (ifMatch != null && ifMatch.indexOf('*') == -1) {
			boolean match = false;
			for (String etag : ifMatch.split(",")) {
				if (etag.trim().equals(resource.getWeakETag())) {
					match = true;
				}
			}
			if (!match) {
				response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
				return false;
			}
		}
		String ifNoneMatch = request.getHeader("if-none-match");
		if (ifNoneMatch != null) {
			boolean match = false;
			if (ifNoneMatch.equals("*")) {
				match = true;
			} else {
				for (String etag : ifNoneMatch.split(",")) {
					if (etag.trim().equals(resource.getWeakETag())) {
						match = true;
					}
				}
			}
			if (match) {
				if (("GET".equals(request.getMethod())) || ("HEAD".equals(request.getMethod()))) {
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					response.setHeader("ETag", resource.getWeakETag());
				} else {
					response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
				}
				return false;
			}
		}
		long ifModifiedSince = request.getDateHeader("if-modified-since");
		if (ifModifiedSince != -1 && ifNoneMatch == null) {
			if (resource.getCacheLastModified() < (ifModifiedSince + 1000)) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				response.setHeader("ETag", resource.getWeakETag());
				return false;
			}
		}
		long ifUnmodifiedSince = request.getDateHeader("if-unmodified-since");
		if (ifUnmodifiedSince != -1) {
			if (resource.getCacheLastModified() >= (ifUnmodifiedSince + 1000)) {
				response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
				return false;
			}
		}
		return true;
	}

	private boolean checkSendFile() {
		return false;
	}
}
