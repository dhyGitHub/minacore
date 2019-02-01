/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.filterchain;

import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public interface IoFilter {
	void init() throws Exception;

	void destroy() throws Exception;

	void onPreAdd(IoFilterChain arg0, String arg1, IoFilter.NextFilter arg2) throws Exception;

	void onPostAdd(IoFilterChain arg0, String arg1, IoFilter.NextFilter arg2) throws Exception;

	void onPreRemove(IoFilterChain arg0, String arg1, IoFilter.NextFilter arg2) throws Exception;

	void onPostRemove(IoFilterChain arg0, String arg1, IoFilter.NextFilter arg2) throws Exception;

	void sessionCreated(IoFilter.NextFilter arg0, IoSession arg1) throws Exception;

	void sessionOpened(IoFilter.NextFilter arg0, IoSession arg1) throws Exception;

	void sessionClosed(IoFilter.NextFilter arg0, IoSession arg1) throws Exception;

	void sessionIdle(IoFilter.NextFilter arg0, IoSession arg1, IdleStatus arg2) throws Exception;

	void exceptionCaught(IoFilter.NextFilter arg0, IoSession arg1, Throwable arg2) throws Exception;

	void inputClosed(IoFilter.NextFilter arg0, IoSession arg1) throws Exception;

	void messageReceived(IoFilter.NextFilter arg0, IoSession arg1, Object arg2) throws Exception;

	void messageSent(IoFilter.NextFilter arg0, IoSession arg1, WriteRequest arg2) throws Exception;

	void filterClose(IoFilter.NextFilter arg0, IoSession arg1) throws Exception;

	void filterWrite(IoFilter.NextFilter arg0, IoSession arg1, WriteRequest arg2) throws Exception;

	public interface NextFilter {
		void sessionCreated(IoSession arg0);

		void sessionOpened(IoSession arg0);

		void sessionClosed(IoSession arg0);

		void sessionIdle(IoSession arg0, IdleStatus arg1);

		void exceptionCaught(IoSession arg0, Throwable arg1);

		void inputClosed(IoSession arg0);

		void messageReceived(IoSession arg0, Object arg1);

		void messageSent(IoSession arg0, WriteRequest arg1);

		void filterWrite(IoSession arg0, WriteRequest arg1);

		void filterClose(IoSession arg0);
	}
}