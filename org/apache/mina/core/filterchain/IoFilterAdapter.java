/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.filterchain;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public class IoFilterAdapter implements IoFilter {
	public void init() throws Exception {
	}

	public void destroy() throws Exception {
	}

	public void onPreAdd(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
	}

	public void onPostAdd(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
	}

	public void onPreRemove(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
	}

	public void onPostRemove(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
	}

	public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
		nextFilter.sessionCreated(session);
	}

	public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		nextFilter.sessionOpened(session);
	}

	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		nextFilter.sessionClosed(session);
	}

	public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
		nextFilter.sessionIdle(session, status);
	}

	public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
		nextFilter.exceptionCaught(session, cause);
	}

	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		nextFilter.messageReceived(session, message);
	}

	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		nextFilter.messageSent(session, writeRequest);
	}

	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		nextFilter.filterWrite(session, writeRequest);
	}

	public void filterClose(NextFilter nextFilter, IoSession session) throws Exception {
		nextFilter.filterClose(session);
	}

	public void inputClosed(NextFilter nextFilter, IoSession session) throws Exception {
		nextFilter.inputClosed(session);
	}

	public String toString() {
		return this.getClass().getSimpleName();
	}
}