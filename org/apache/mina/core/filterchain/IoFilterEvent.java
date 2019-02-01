/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.filterchain;

import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEvent;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoFilterEvent extends IoEvent {
	private static final Logger LOGGER = LoggerFactory.getLogger(IoFilterEvent.class);
	private static final boolean DEBUG;
	private final NextFilter nextFilter;

	public IoFilterEvent(NextFilter nextFilter, IoEventType type, IoSession session, Object parameter) {
		super(type, session, parameter);
		if (nextFilter == null) {
			throw new IllegalArgumentException("nextFilter must not be null");
		} else {
			this.nextFilter = nextFilter;
		}
	}

	public NextFilter getNextFilter() {
		return this.nextFilter;
	}

	public void fire() {
		IoSession session = this.getSession();
		NextFilter nextFilter = this.getNextFilter();
		IoEventType type = this.getType();
		if (DEBUG) {
			LOGGER.debug("Firing a {} event for session {}", type, Long.valueOf(session.getId()));
		}

		WriteRequest writeRequest;
		switch (IoFilterEvent.SyntheticClass_1.$SwitchMap$org$apache$mina$core$session$IoEventType[type.ordinal()]) {
		case 1:
			Object parameter = this.getParameter();
			nextFilter.messageReceived(session, parameter);
			break;
		case 2:
			writeRequest = (WriteRequest) this.getParameter();
			nextFilter.messageSent(session, writeRequest);
			break;
		case 3:
			writeRequest = (WriteRequest) this.getParameter();
			nextFilter.filterWrite(session, writeRequest);
			break;
		case 4:
			nextFilter.filterClose(session);
			break;
		case 5:
			Throwable throwable = (Throwable) this.getParameter();
			nextFilter.exceptionCaught(session, throwable);
			break;
		case 6:
			nextFilter.sessionIdle(session, (IdleStatus) this.getParameter());
			break;
		case 7:
			nextFilter.sessionOpened(session);
			break;
		case 8:
			nextFilter.sessionCreated(session);
			break;
		case 9:
			nextFilter.sessionClosed(session);
			break;
		default:
			throw new IllegalArgumentException("Unknown event type: " + type);
		}

		if (DEBUG) {
			LOGGER.debug("Event {} has been fired for session {}", type, Long.valueOf(session.getId()));
		}

	}

	static {
		DEBUG = LOGGER.isDebugEnabled();
	}
}