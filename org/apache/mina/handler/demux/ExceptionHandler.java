/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.demux;

import org.apache.mina.core.session.IoSession;

public interface ExceptionHandler<E extends Throwable> {
	ExceptionHandler<Throwable> NOOP = new ExceptionHandler() {
		public void exceptionCaught(IoSession session, Throwable cause) {
		}
	};
	ExceptionHandler<Throwable> CLOSE = new ExceptionHandler() {
		public void exceptionCaught(IoSession session, Throwable cause) {
			session.closeNow();
		}
	};

	void exceptionCaught(IoSession arg0, E arg1) throws Exception;
}