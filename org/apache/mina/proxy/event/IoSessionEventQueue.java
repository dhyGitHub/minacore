/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.event;

import java.util.LinkedList;
import java.util.Queue;
import org.apache.mina.proxy.event.IoSessionEvent;
import org.apache.mina.proxy.event.IoSessionEventType;
import org.apache.mina.proxy.handlers.socks.SocksProxyRequest;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoSessionEventQueue {
	private static final Logger logger = LoggerFactory.getLogger(IoSessionEventQueue.class);
	private ProxyIoSession proxyIoSession;
	private Queue<IoSessionEvent> sessionEventsQueue = new LinkedList();

	public IoSessionEventQueue(ProxyIoSession proxyIoSession) {
		this.proxyIoSession = proxyIoSession;
	}

	private void discardSessionQueueEvents() {
		Queue arg0 = this.sessionEventsQueue;
		synchronized (this.sessionEventsQueue) {
			this.sessionEventsQueue.clear();
			logger.debug("Event queue CLEARED");
		}
	}

	public void enqueueEventIfNecessary(IoSessionEvent evt) {
		logger.debug("??? >> Enqueue {}", evt);
		if (this.proxyIoSession.getRequest() instanceof SocksProxyRequest) {
			evt.deliverEvent();
		} else {
			if (this.proxyIoSession.getHandler().isHandshakeComplete()) {
				evt.deliverEvent();
			} else if (evt.getType() == IoSessionEventType.CLOSED) {
				if (this.proxyIoSession.isAuthenticationFailed()) {
					this.proxyIoSession.getConnector().cancelConnectFuture();
					this.discardSessionQueueEvents();
					evt.deliverEvent();
				} else {
					this.discardSessionQueueEvents();
				}
			} else if (evt.getType() == IoSessionEventType.OPENED) {
				this.enqueueSessionEvent(evt);
				evt.deliverEvent();
			} else {
				this.enqueueSessionEvent(evt);
			}

		}
	}

	public void flushPendingSessionEvents() throws Exception {
		Queue arg0 = this.sessionEventsQueue;
		synchronized (this.sessionEventsQueue) {
			IoSessionEvent evt;
			while ((evt = (IoSessionEvent) this.sessionEventsQueue.poll()) != null) {
				logger.debug(" Flushing buffered event: {}", evt);
				evt.deliverEvent();
			}

		}
	}

	private void enqueueSessionEvent(IoSessionEvent evt) {
		Queue arg1 = this.sessionEventsQueue;
		synchronized (this.sessionEventsQueue) {
			logger.debug("Enqueuing event: {}", evt);
			this.sessionEventsQueue.offer(evt);
		}
	}
}