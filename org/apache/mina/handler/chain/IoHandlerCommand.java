/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.chain;

import org.apache.mina.core.session.IoSession;

public interface IoHandlerCommand {
	void execute(IoHandlerCommand.NextCommand arg0, IoSession arg1, Object arg2) throws Exception;

	public interface NextCommand {
		void execute(IoSession arg0, Object arg1) throws Exception;
	}
}