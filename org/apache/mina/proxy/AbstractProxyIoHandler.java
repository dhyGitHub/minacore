/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.proxy.handlers.socks.SocksProxyRequest;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProxyIoHandler extends IoHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(AbstractProxyIoHandler.class);

	public abstract void proxySessionOpened(IoSession arg0) throws Exception;

	public final void sessionOpened(IoSession session) throws Exception {
		ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
		if (!(proxyIoSession.getRequest() instanceof SocksProxyRequest) && !proxyIoSession.isAuthenticationFailed()
				&& !proxyIoSession.getHandler().isHandshakeComplete()) {
			logger.debug("Filtered session opened event !");
		} else {
			this.proxySessionOpened(session);
		}

	}
}