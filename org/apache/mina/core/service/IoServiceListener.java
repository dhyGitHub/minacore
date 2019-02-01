/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.service;

import java.util.EventListener;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public interface IoServiceListener extends EventListener {
	void serviceActivated(IoService arg0) throws Exception;

	void serviceIdle(IoService arg0, IdleStatus arg1) throws Exception;

	void serviceDeactivated(IoService arg0) throws Exception;

	void sessionCreated(IoSession arg0) throws Exception;

	void sessionClosed(IoSession arg0) throws Exception;

	void sessionDestroyed(IoSession arg0) throws Exception;
}