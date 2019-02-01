/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionAttributeMap;
import org.apache.mina.core.session.IoSessionDataStructureFactory;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;

public class DefaultIoSessionDataStructureFactory implements IoSessionDataStructureFactory {
	public IoSessionAttributeMap getAttributeMap(IoSession session) throws Exception {
		return new DefaultIoSessionDataStructureFactory.DefaultIoSessionAttributeMap();
	}

	public WriteRequestQueue getWriteRequestQueue(IoSession session) throws Exception {
		return new DefaultIoSessionDataStructureFactory.DefaultWriteRequestQueue();
	}

	private static class DefaultWriteRequestQueue implements WriteRequestQueue {
		private final Queue<WriteRequest> q = new ConcurrentLinkedQueue();

		public void dispose(IoSession session) {
		}

		public void clear(IoSession session) {
			this.q.clear();
		}

		public boolean isEmpty(IoSession session) {
			return this.q.isEmpty();
		}

		public void offer(IoSession session, WriteRequest writeRequest) {
			this.q.offer(writeRequest);
		}

		public WriteRequest poll(IoSession session) {
			WriteRequest answer = (WriteRequest) this.q.poll();
			if (answer == AbstractIoSession.CLOSE_REQUEST) {
				session.closeNow();
				this.dispose(session);
				answer = null;
			}

			return answer;
		}

		public String toString() {
			return this.q.toString();
		}

		public int size() {
			return this.q.size();
		}
	}

	private static class DefaultIoSessionAttributeMap implements IoSessionAttributeMap {
		private final ConcurrentHashMap<Object, Object> attributes = new ConcurrentHashMap(4);

		public Object getAttribute(IoSession session, Object key, Object defaultValue) {
			if (key == null) {
				throw new IllegalArgumentException("key");
			} else if (defaultValue == null) {
				return this.attributes.get(key);
			} else {
				Object object = this.attributes.putIfAbsent(key, defaultValue);
				return object == null ? defaultValue : object;
			}
		}

		public Object setAttribute(IoSession session, Object key, Object value) {
			if (key == null) {
				throw new IllegalArgumentException("key");
			} else {
				return value == null ? this.attributes.remove(key) : this.attributes.put(key, value);
			}
		}

		public Object setAttributeIfAbsent(IoSession session, Object key, Object value) {
			if (key == null) {
				throw new IllegalArgumentException("key");
			} else {
				return value == null ? null : this.attributes.putIfAbsent(key, value);
			}
		}

		public Object removeAttribute(IoSession session, Object key) {
			if (key == null) {
				throw new IllegalArgumentException("key");
			} else {
				return this.attributes.remove(key);
			}
		}

		public boolean removeAttribute(IoSession session, Object key, Object value) {
			if (key == null) {
				throw new IllegalArgumentException("key");
			} else if (value == null) {
				return false;
			} else {
				try {
					return this.attributes.remove(key, value);
				} catch (NullPointerException arg4) {
					return false;
				}
			}
		}

		public boolean replaceAttribute(IoSession session, Object key, Object oldValue, Object newValue) {
			try {
				return this.attributes.replace(key, oldValue, newValue);
			} catch (NullPointerException arg5) {
				return false;
			}
		}

		public boolean containsAttribute(IoSession session, Object key) {
			return this.attributes.containsKey(key);
		}

		public Set<Object> getAttributeKeys(IoSession session) {
			ConcurrentHashMap arg1 = this.attributes;
			synchronized (this.attributes) {
				return new HashSet(this.attributes.keySet());
			}
		}

		public void dispose(IoSession session) throws Exception {
		}
	}
}