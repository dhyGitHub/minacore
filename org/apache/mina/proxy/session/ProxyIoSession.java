/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.session;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.proxy.ProxyConnector;
import org.apache.mina.proxy.ProxyLogicHandler;
import org.apache.mina.proxy.event.IoSessionEventQueue;
import org.apache.mina.proxy.filter.ProxyFilter;
import org.apache.mina.proxy.handlers.ProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpAuthenticationMethods;

public class ProxyIoSession {
	public static final String PROXY_SESSION = ProxyConnector.class.getName() + ".ProxySession";
	private static final String DEFAULT_ENCODING = "ISO-8859-1";
	private List<HttpAuthenticationMethods> preferedOrder;
	private ProxyRequest request;
	private ProxyLogicHandler handler;
	private ProxyFilter proxyFilter;
	private IoSession session;
	private ProxyConnector connector;
	private InetSocketAddress proxyAddress = null;
	private boolean reconnectionNeeded = false;
	private String charsetName;
	private IoSessionEventQueue eventQueue = new IoSessionEventQueue(this);
	private boolean authenticationFailed;

	public ProxyIoSession(InetSocketAddress proxyAddress, ProxyRequest request) {
		this.setProxyAddress(proxyAddress);
		this.setRequest(request);
	}

	public IoSessionEventQueue getEventQueue() {
		return this.eventQueue;
	}

	public List<HttpAuthenticationMethods> getPreferedOrder() {
		return this.preferedOrder;
	}

	public void setPreferedOrder(List<HttpAuthenticationMethods> preferedOrder) {
		this.preferedOrder = preferedOrder;
	}

	public ProxyLogicHandler getHandler() {
		return this.handler;
	}

	public void setHandler(ProxyLogicHandler handler) {
		this.handler = handler;
	}

	public ProxyFilter getProxyFilter() {
		return this.proxyFilter;
	}

	public void setProxyFilter(ProxyFilter proxyFilter) {
		this.proxyFilter = proxyFilter;
	}

	public ProxyRequest getRequest() {
		return this.request;
	}

	private void setRequest(ProxyRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request cannot be null");
		} else {
			this.request = request;
		}
	}

	public IoSession getSession() {
		return this.session;
	}

	public void setSession(IoSession session) {
		this.session = session;
	}

	public ProxyConnector getConnector() {
		return this.connector;
	}

	public void setConnector(ProxyConnector connector) {
		this.connector = connector;
	}

	public InetSocketAddress getProxyAddress() {
		return this.proxyAddress;
	}

	private void setProxyAddress(InetSocketAddress proxyAddress) {
		if (proxyAddress == null) {
			throw new IllegalArgumentException("proxyAddress object cannot be null");
		} else {
			this.proxyAddress = proxyAddress;
		}
	}

	public boolean isReconnectionNeeded() {
		return this.reconnectionNeeded;
	}

	public void setReconnectionNeeded(boolean reconnectionNeeded) {
		this.reconnectionNeeded = reconnectionNeeded;
	}

	public Charset getCharset() {
		return Charset.forName(this.getCharsetName());
	}

	public String getCharsetName() {
		if (this.charsetName == null) {
			this.charsetName = "ISO-8859-1";
		}

		return this.charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	public boolean isAuthenticationFailed() {
		return this.authenticationFailed;
	}

	public void setAuthenticationFailed(boolean authenticationFailed) {
		this.authenticationFailed = authenticationFailed;
	}
}