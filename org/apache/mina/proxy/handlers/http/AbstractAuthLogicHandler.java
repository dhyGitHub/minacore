/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http;

import java.util.List;
import java.util.Map;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.ProxyRequest;
import org.apache.mina.proxy.handlers.http.AbstractHttpLogicHandler;
import org.apache.mina.proxy.handlers.http.HttpProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpProxyResponse;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAuthLogicHandler {
	private static final Logger logger = LoggerFactory.getLogger(AbstractAuthLogicHandler.class);
	protected ProxyRequest request;
	protected ProxyIoSession proxyIoSession;
	protected int step = 0;

	protected AbstractAuthLogicHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
		this.proxyIoSession = proxyIoSession;
		this.request = proxyIoSession.getRequest();
		if (this.request == null || !(this.request instanceof HttpProxyRequest)) {
			throw new IllegalArgumentException("request parameter should be a non null HttpProxyRequest instance");
		}
	}

	public abstract void doHandshake(NextFilter arg0) throws ProxyAuthException;

	public abstract void handleResponse(HttpProxyResponse arg0) throws ProxyAuthException;

	protected void writeRequest(NextFilter nextFilter, HttpProxyRequest request) throws ProxyAuthException {
		logger.debug("  sending HTTP request");
		((AbstractHttpLogicHandler) this.proxyIoSession.getHandler()).writeRequest(nextFilter, request);
	}

	public static void addKeepAliveHeaders(Map<String, List<String>> headers) {
		StringUtilities.addValueToHeader(headers, "Keep-Alive", "300", true);
		StringUtilities.addValueToHeader(headers, "Proxy-Connection", "keep-Alive", true);
	}
}