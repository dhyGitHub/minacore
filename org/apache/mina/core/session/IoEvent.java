/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public class IoEvent implements Runnable {
	private final IoEventType type;
	private final IoSession session;
	private final Object parameter;

	public IoEvent(IoEventType type, IoSession session, Object parameter) {
		if (type == null) {
			throw new IllegalArgumentException("type");
		} else if (session == null) {
			throw new IllegalArgumentException("session");
		} else {
			this.type = type;
			this.session = session;
			this.parameter = parameter;
		}
	}

	public IoEventType getType() {
		return this.type;
	}

	public IoSession getSession() {
		return this.session;
	}

	public Object getParameter() {
		return this.parameter;
	}

	public void run() {
		this.fire();
	}

	public void fire() {
		switch (IoEvent.SyntheticClass_1.$SwitchMap$org$apache$mina$core$session$IoEventType[this.getType()
				.ordinal()]) {
		case 1:
			this.getSession().getFilterChain().fireMessageReceived(this.getParameter());
			break;
		case 2:
			this.getSession().getFilterChain().fireMessageSent((WriteRequest) this.getParameter());
			break;
		case 3:
			this.getSession().getFilterChain().fireFilterWrite((WriteRequest) this.getParameter());
			break;
		case 4:
			this.getSession().getFilterChain().fireFilterClose();
			break;
		case 5:
			this.getSession().getFilterChain().fireExceptionCaught((Throwable) this.getParameter());
			break;
		case 6:
			this.getSession().getFilterChain().fireSessionIdle((IdleStatus) this.getParameter());
			break;
		case 7:
			this.getSession().getFilterChain().fireSessionOpened();
			break;
		case 8:
			this.getSession().getFilterChain().fireSessionCreated();
			break;
		case 9:
			this.getSession().getFilterChain().fireSessionClosed();
			break;
		default:
			throw new IllegalArgumentException("Unknown event type: " + this.getType());
		}

	}

	public String toString() {
		return this.getParameter() == null ? "[" + this.getSession() + "] " + this.getType().name()
				: "[" + this.getSession() + "] " + this.getType().name() + ": " + this.getParameter();
	}
}