/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.multiton;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

@Deprecated
public interface SingleSessionIoHandler {
	void sessionCreated() throws Exception;

	void sessionOpened() throws Exception;

	void sessionClosed() throws Exception;

	void sessionIdle(IdleStatus arg0) throws Exception;

	void exceptionCaught(Throwable arg0) throws Exception;

	void inputClosed(IoSession arg0);

	void messageReceived(Object arg0) throws Exception;

	void messageSent(Object arg0) throws Exception;
}