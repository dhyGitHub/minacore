/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;

public class SessionAttributeInitializingFilter extends IoFilterAdapter {
	private final Map<String, Object> attributes = new ConcurrentHashMap();

	public SessionAttributeInitializingFilter() {
	}

	public SessionAttributeInitializingFilter(Map<String, ? extends Object> attributes) {
		this.setAttributes(attributes);
	}

	public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	public Object setAttribute(String key, Object value) {
		return value == null ? this.removeAttribute(key) : this.attributes.put(key, value);
	}

	public Object setAttribute(String key) {
		return this.attributes.put(key, Boolean.TRUE);
	}

	public Object removeAttribute(String key) {
		return this.attributes.remove(key);
	}

	boolean containsAttribute(String key) {
		return this.attributes.containsKey(key);
	}

	public Set<String> getAttributeKeys() {
		return this.attributes.keySet();
	}

	public void setAttributes(Map<String, ? extends Object> attributes) {
		if (attributes == null) {
			attributes = new ConcurrentHashMap();
		}

		this.attributes.clear();
		this.attributes.putAll((Map) attributes);
	}

	public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
		Iterator i$ = this.attributes.entrySet().iterator();

		while (i$.hasNext()) {
			Entry e = (Entry) i$.next();
			session.setAttribute(e.getKey(), e.getValue());
		}

		nextFilter.sessionCreated(session);
	}
}