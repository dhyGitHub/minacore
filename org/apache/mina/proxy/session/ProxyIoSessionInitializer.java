/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.session;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.proxy.session.ProxyIoSession;

public class ProxyIoSessionInitializer<T extends ConnectFuture> implements IoSessionInitializer<T> {
	private final IoSessionInitializer<T> wrappedSessionInitializer;
	private final ProxyIoSession proxyIoSession;

	public ProxyIoSessionInitializer(IoSessionInitializer<T> wrappedSessionInitializer, ProxyIoSession proxyIoSession) {
		this.wrappedSessionInitializer = wrappedSessionInitializer;
		this.proxyIoSession = proxyIoSession;
	}

	public ProxyIoSession getProxySession() {
		return this.proxyIoSession;
	}

	public void initializeSession(IoSession session, T future) {
		if (this.wrappedSessionInitializer != null) {
			this.wrappedSessionInitializer.initializeSession(session, future);
		}

		if (this.proxyIoSession != null) {
			this.proxyIoSession.setSession(session);
			session.setAttribute(ProxyIoSession.PROXY_SESSION, this.proxyIoSession);
		}

	}
}