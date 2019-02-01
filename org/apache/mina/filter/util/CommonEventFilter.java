/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.util;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterEvent;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public abstract class CommonEventFilter extends IoFilterAdapter {
	protected abstract void filter(IoFilterEvent arg0) throws Exception;

	public final void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
		this.filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_CREATED, session, (Object) null));
	}

	public final void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		this.filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_OPENED, session, (Object) null));
	}

	public final void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		this.filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_CLOSED, session, (Object) null));
	}

	public final void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
		this.filter(new IoFilterEvent(nextFilter, IoEventType.SESSION_IDLE, session, status));
	}

	public final void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
		this.filter(new IoFilterEvent(nextFilter, IoEventType.EXCEPTION_CAUGHT, session, cause));
	}

	public final void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		this.filter(new IoFilterEvent(nextFilter, IoEventType.MESSAGE_RECEIVED, session, message));
	}

	public final void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest)
			throws Exception {
		this.filter(new IoFilterEvent(nextFilter, IoEventType.MESSAGE_SENT, session, writeRequest));
	}

	public final void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest)
			throws Exception {
		this.filter(new IoFilterEvent(nextFilter, IoEventType.WRITE, session, writeRequest));
	}

	public final void filterClose(NextFilter nextFilter, IoSession session) throws Exception {
		this.filter(new IoFilterEvent(nextFilter, IoEventType.CLOSE, session, (Object) null));
	}
}