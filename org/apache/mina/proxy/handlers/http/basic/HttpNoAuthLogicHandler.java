/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http.basic;

import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.HttpProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpProxyResponse;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpNoAuthLogicHandler extends AbstractAuthLogicHandler {
	private static final Logger logger = LoggerFactory.getLogger(HttpNoAuthLogicHandler.class);

	public HttpNoAuthLogicHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
		super(proxyIoSession);
	}

	public void doHandshake(NextFilter nextFilter) throws ProxyAuthException {
		logger.debug(" doHandshake()");
		this.writeRequest(nextFilter, (HttpProxyRequest) this.request);
		++this.step;
	}

	public void handleResponse(HttpProxyResponse response) throws ProxyAuthException {
		throw new ProxyAuthException("Received error response code (" + response.getStatusLine() + ").");
	}
}