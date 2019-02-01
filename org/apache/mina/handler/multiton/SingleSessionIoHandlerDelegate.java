/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.multiton;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.multiton.SingleSessionIoHandler;
import org.apache.mina.handler.multiton.SingleSessionIoHandlerFactory;

@Deprecated
public class SingleSessionIoHandlerDelegate implements IoHandler {
	public static final AttributeKey HANDLER = new AttributeKey(SingleSessionIoHandlerDelegate.class, "handler");
	private final SingleSessionIoHandlerFactory factory;

	public SingleSessionIoHandlerDelegate(SingleSessionIoHandlerFactory factory) {
		if (factory == null) {
			throw new IllegalArgumentException("factory");
		} else {
			this.factory = factory;
		}
	}

	public SingleSessionIoHandlerFactory getFactory() {
		return this.factory;
	}

	public void sessionCreated(IoSession session) throws Exception {
		SingleSessionIoHandler handler = this.factory.getHandler(session);
		session.setAttribute(HANDLER, handler);
		handler.sessionCreated();
	}

	public void sessionOpened(IoSession session) throws Exception {
		SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
		handler.sessionOpened();
	}

	public void sessionClosed(IoSession session) throws Exception {
		SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
		handler.sessionClosed();
	}

	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
		handler.sessionIdle(status);
	}

	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
		handler.exceptionCaught(cause);
	}

	public void messageReceived(IoSession session, Object message) throws Exception {
		SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
		handler.messageReceived(message);
	}

	public void messageSent(IoSession session, Object message) throws Exception {
		SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
		handler.messageSent(message);
	}

	public void inputClosed(IoSession session) throws Exception {
		SingleSessionIoHandler handler = (SingleSessionIoHandler) session.getAttribute(HANDLER);
		handler.inputClosed(session);
	}
}