/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.service;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public interface IoHandler {
	void sessionCreated(IoSession arg0) throws Exception;

	void sessionOpened(IoSession arg0) throws Exception;

	void sessionClosed(IoSession arg0) throws Exception;

	void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception;

	void exceptionCaught(IoSession arg0, Throwable arg1) throws Exception;

	void messageReceived(IoSession arg0, Object arg1) throws Exception;

	void messageSent(IoSession arg0, Object arg1) throws Exception;

	void inputClosed(IoSession arg0) throws Exception;
}