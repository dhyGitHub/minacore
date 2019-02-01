/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.demux;

import org.apache.mina.core.session.IoSession;

public interface MessageHandler<E> {
	MessageHandler<Object> NOOP = new MessageHandler() {
		public void handleMessage(IoSession session, Object message) {
		}
	};

	void handleMessage(IoSession arg0, E arg1) throws Exception;
}