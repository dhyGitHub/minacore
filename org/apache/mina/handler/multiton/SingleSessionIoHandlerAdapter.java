/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.multiton;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.multiton.SingleSessionIoHandler;

@Deprecated
public class SingleSessionIoHandlerAdapter implements SingleSessionIoHandler {
	private final IoSession session;

	public SingleSessionIoHandlerAdapter(IoSession session) {
		if (session == null) {
			throw new IllegalArgumentException("session");
		} else {
			this.session = session;
		}
	}

	protected IoSession getSession() {
		return this.session;
	}

	public void exceptionCaught(Throwable th) throws Exception {
	}

	public void inputClosed(IoSession session) {
	}

	public void messageReceived(Object message) throws Exception {
	}

	public void messageSent(Object message) throws Exception {
	}

	public void sessionClosed() throws Exception {
	}

	public void sessionCreated() throws Exception {
	}

	public void sessionIdle(IdleStatus status) throws Exception {
	}

	public void sessionOpened() throws Exception {
	}
}