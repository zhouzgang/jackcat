package cn.ecomb.jackcat.catalina.session;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouzg
 * @date 2020-11-06.
 */
public class Manager {

	protected Map<String, Session> sessions = new ConcurrentHashMap<>();
	protected Random random = new Random();

	public Session createSession() {
		Session session = new Session();
		session.setValid(true);
		session.setCreationTime(System.currentTimeMillis());
		session.setMaxInactiveInterval(60);
		String sessionId = generateSessionId();
		session.setManager(this);
		add(session);
		return session;
	}

	/**
	 * 生成一个唯一 SessionId
	 * @return
	 */
	protected synchronized String generateSessionId() {
		byte[] randomBytes = new byte[16];
		StringBuilder builder = new StringBuilder();
		// todo 这里的逻辑是什么
		do {
			random.nextBytes(randomBytes);
			for (int i = 0; i < randomBytes.length; i++) {
				int heigh = randomBytes[i] & 0xf0 >> 4;
				int low = (randomBytes[i] & 0xf);
				builder.append(Integer.toHexString(heigh));
				builder.append(Integer.toHexString(low));
			}
		} while (sessions.containsKey(builder.toString()));
		return builder.toString();
	}

	/**
	 *  周期性操作调用
	 */
	public void backgroundProcess() {
		Session[] sessions = findSessions();
		for (Session session : sessions) {
			if (session != null && !session.isValid()) {

			}
		}
	}

	public void add(Session session) {
		sessions.put(session.getId(), session);
	}

	public void remove(String id) {
		sessions.remove(id);
	}

	public Session[] findSessions() {
		return sessions.values().toArray(new Session[0]);
	}

	public Session findSession(String id) {
		return sessions.get(id);
	}

}
