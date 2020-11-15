package cn.ecomb.jackcat.catalina.session;

import lombok.Data;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouzg
 * @date 2020-11-06.
 */
@Data
public class Session implements HttpSession, Serializable {


	private static final long serialVersionUID = 1L;

	private String id;
	private long creationTime = 0L;
	private int maxInactiveInterval = -1;
	private transient long lastAccessedTime = 0L;

	private transient Manager manager = null;

	private volatile boolean isValid = false;
	private boolean isNew;

	private Map<String, Object> attributes = new ConcurrentHashMap<>();

	public void expire() {
		setValid(false);
		attributes.clear();
		manager.remove(getId());
	}


	public void endAccess() {
		isNew = false;
		lastAccessedTime = System.currentTimeMillis();
	}

	public boolean isValid() {
		if (!isValid) {
			return false;
		}

		if (maxInactiveInterval > 0) {
			long now = System.currentTimeMillis();
			int timeIdle = (int)((now - lastAccessedTime) / 1000L);
			if (timeIdle >= maxInactiveInterval) {
				expire();
			}
		}

		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
		lastAccessedTime = creationTime;
	}

	@Override
	public long getCreationTime() {
		return 0;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		maxInactiveInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		return 0;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public Object getValue(String name) {
		return null;
	}

	@Override
	public Enumeration getAttributeNames() {
		return null;
	}

	@Override
	public String[] getValueNames() {
		return new String[0];
	}

	@Override
	public void setAttribute(String name, Object value) {
		if (value == null) {
			removeAttribute(name);
		}
		attributes.put(name, value);
	}

	@Override
	public void putValue(String name, Object value) {

	}

	@Override
	public void removeValue(String name) {

	}

	@Override
	public void invalidate() {
		expire();
	}

	@Override
	public boolean isNew() {
		return isNew;
	}
}
