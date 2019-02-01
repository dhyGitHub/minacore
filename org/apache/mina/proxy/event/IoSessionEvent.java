/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.event;

import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.proxy.event.IoSessionEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoSessionEvent {
	private static final Logger logger = LoggerFactory.getLogger(IoSessionEvent.class);
	private final NextFilter nextFilter;
	private final IoSession session;
	private final IoSessionEventType type;
	private IdleStatus status;

	public IoSessionEvent(NextFilter nextFilter, IoSession session, IoSessionEventType type) {
		this.nextFilter = nextFilter;
		this.session = session;
		this.type = type;
	}

	public IoSessionEvent(NextFilter nextFilter, IoSession session, IdleStatus status) {
		this(nextFilter, session, IoSessionEventType.IDLE);
		this.status = status;
	}

	public void deliverEvent() {
		logger.debug("Delivering event {}", this);
		deliverEvent(this.nextFilter, this.session, this.type, this.status);
	}

	private static void deliverEvent(NextFilter nextFilter, IoSession session, IoSessionEventType type,
			IdleStatus status) {
		switch (IoSessionEvent.SyntheticClass_1.$SwitchMap$org$apache$mina$proxy$event$IoSessionEventType[type
				.ordinal()]) {
		case 1:
			nextFilter.sessionCreated(session);
			break;
		case 2:
			nextFilter.sessionOpened(session);
			break;
		case 3:
			nextFilter.sessionIdle(session, status);
			break;
		case 4:
			nextFilter.sessionClosed(session);
		}

	}

	public String toString() {
		StringBuilder sb = new StringBuilder(IoSessionEvent.class.getSimpleName());
		sb.append('@');
		sb.append(Integer.toHexString(this.hashCode()));
		sb.append(" - [ ").append(this.session);
		sb.append(", ").append(this.type);
		sb.append(']');
		return sb.toString();
	}

	public IdleStatus getStatus() {
		return this.status;
	}

	public NextFilter getNextFilter() {
		return this.nextFilter;
	}

	public IoSession getSession() {
		return this.session;
	}

	public IoSessionEventType getType() {
		return this.type;
	}
}