/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.util;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public class ReferenceCountingFilter extends IoFilterAdapter {
	private final IoFilter filter;
	private int count = 0;

	public ReferenceCountingFilter(IoFilter filter) {
		this.filter = filter;
	}

	public void init() throws Exception {
	}

	public void destroy() throws Exception {
	}

	public synchronized void onPreAdd(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
		if (0 == this.count) {
			this.filter.init();
		}

		++this.count;
		this.filter.onPreAdd(parent, name, nextFilter);
	}

	public synchronized void onPostRemove(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
		this.filter.onPostRemove(parent, name, nextFilter);
		--this.count;
		if (0 == this.count) {
			this.filter.destroy();
		}

	}

	public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
		this.filter.exceptionCaught(nextFilter, session, cause);
	}

	public void filterClose(NextFilter nextFilter, IoSession session) throws Exception {
		this.filter.filterClose(nextFilter, session);
	}

	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		this.filter.filterWrite(nextFilter, session, writeRequest);
	}

	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		this.filter.messageReceived(nextFilter, session, message);
	}

	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		this.filter.messageSent(nextFilter, session, writeRequest);
	}

	public void onPostAdd(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
		this.filter.onPostAdd(parent, name, nextFilter);
	}

	public void onPreRemove(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
		this.filter.onPreRemove(parent, name, nextFilter);
	}

	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		this.filter.sessionClosed(nextFilter, session);
	}

	public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
		this.filter.sessionCreated(nextFilter, session);
	}

	public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
		this.filter.sessionIdle(nextFilter, session, status);
	}

	public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		this.filter.sessionOpened(nextFilter, session);
	}
}