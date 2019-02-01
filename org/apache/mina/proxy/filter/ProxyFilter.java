/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.filter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.ProxyLogicHandler;
import org.apache.mina.proxy.event.IoSessionEvent;
import org.apache.mina.proxy.event.IoSessionEventType;
import org.apache.mina.proxy.filter.ProxyHandshakeIoBuffer;
import org.apache.mina.proxy.handlers.ProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpSmartProxyHandler;
import org.apache.mina.proxy.handlers.socks.Socks4LogicHandler;
import org.apache.mina.proxy.handlers.socks.Socks5LogicHandler;
import org.apache.mina.proxy.handlers.socks.SocksProxyRequest;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyFilter extends IoFilterAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyFilter.class);

	public void onPreAdd(IoFilterChain chain, String name, NextFilter nextFilter) {
		if (chain.contains(ProxyFilter.class)) {
			throw new IllegalStateException("A filter chain cannot contain more than one ProxyFilter.");
		}
	}

	public void onPreRemove(IoFilterChain chain, String name, NextFilter nextFilter) {
		IoSession session = chain.getSession();
		session.removeAttribute(ProxyIoSession.PROXY_SESSION);
	}

	public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
		ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
		proxyIoSession.setAuthenticationFailed(true);
		super.exceptionCaught(nextFilter, session, cause);
	}

	private ProxyLogicHandler getProxyHandler(IoSession session) {
		ProxyLogicHandler handler = ((ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION)).getHandler();
		if (handler == null) {
			throw new IllegalStateException();
		} else if (handler.getProxyIoSession().getProxyFilter() != this) {
			throw new IllegalArgumentException("Not managed by this filter.");
		} else {
			return handler;
		}
	}

	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws ProxyAuthException {
		ProxyLogicHandler handler = this.getProxyHandler(session);
		synchronized (handler) {
			IoBuffer buf = (IoBuffer) message;
			if (handler.isHandshakeComplete()) {
				nextFilter.messageReceived(session, buf);
			} else {
				LOGGER.debug(" Data Read: {} ({})", handler, buf);

				while (true) {
					if (!buf.hasRemaining() || handler.isHandshakeComplete()) {
						if (buf.hasRemaining()) {
							LOGGER.debug(" Passing remaining data to next filter");
							nextFilter.messageReceived(session, buf);
						}
						break;
					}

					LOGGER.debug(" Pre-handshake - passing to handler");
					int pos = buf.position();
					handler.messageReceived(nextFilter, buf);
					if (buf.position() == pos || session.isClosing()) {
						return;
					}
				}
			}

		}
	}

	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) {
		this.writeData(nextFilter, session, writeRequest, false);
	}

	public void writeData(NextFilter nextFilter, IoSession session, WriteRequest writeRequest,
			boolean isHandshakeData) {
		ProxyLogicHandler handler = this.getProxyHandler(session);
		synchronized (handler) {
			if (handler.isHandshakeComplete()) {
				nextFilter.filterWrite(session, writeRequest);
			} else if (isHandshakeData) {
				LOGGER.debug("   handshake data: {}", writeRequest.getMessage());
				nextFilter.filterWrite(session, writeRequest);
			} else if (!session.isConnected()) {
				LOGGER.debug(" Write request on closed session. Request ignored.");
			} else {
				LOGGER.debug(" Handshaking is not complete yet. Buffering write request.");
				handler.enqueueWriteRequest(nextFilter, writeRequest);
			}

		}
	}

	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		if (writeRequest.getMessage() == null || !(writeRequest.getMessage() instanceof ProxyHandshakeIoBuffer)) {
			nextFilter.messageSent(session, writeRequest);
		}
	}

	public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
		LOGGER.debug("Session created: " + session);
		ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
		LOGGER.debug("  get proxyIoSession: " + proxyIoSession);
		proxyIoSession.setProxyFilter(this);
		ProxyLogicHandler handler = proxyIoSession.getHandler();
		if (handler == null) {
			ProxyRequest request = proxyIoSession.getRequest();
			Object handler1;
			if (request instanceof SocksProxyRequest) {
				SocksProxyRequest req = (SocksProxyRequest) request;
				if (req.getProtocolVersion() == 4) {
					handler1 = new Socks4LogicHandler(proxyIoSession);
				} else {
					handler1 = new Socks5LogicHandler(proxyIoSession);
				}
			} else {
				handler1 = new HttpSmartProxyHandler(proxyIoSession);
			}

			proxyIoSession.setHandler((ProxyLogicHandler) handler1);
			((ProxyLogicHandler) handler1).doHandshake(nextFilter);
		}

		proxyIoSession.getEventQueue()
				.enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, IoSessionEventType.CREATED));
	}

	public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
		proxyIoSession.getEventQueue()
				.enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, IoSessionEventType.OPENED));
	}

	public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
		ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
		proxyIoSession.getEventQueue().enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, status));
	}

	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
		proxyIoSession.getEventQueue()
				.enqueueEventIfNecessary(new IoSessionEvent(nextFilter, session, IoSessionEventType.CLOSED));
	}
}