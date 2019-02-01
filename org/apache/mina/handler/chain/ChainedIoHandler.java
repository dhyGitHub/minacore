/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.chain;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.chain.IoHandlerChain;
import org.apache.mina.handler.chain.IoHandlerCommand.NextCommand;

public class ChainedIoHandler extends IoHandlerAdapter {
	private final IoHandlerChain chain;

	public ChainedIoHandler() {
		this.chain = new IoHandlerChain();
	}

	public ChainedIoHandler(IoHandlerChain chain) {
		if (chain == null) {
			throw new IllegalArgumentException("chain");
		} else {
			this.chain = chain;
		}
	}

	public IoHandlerChain getChain() {
		return this.chain;
	}

	public void messageReceived(IoSession session, Object message) throws Exception {
		this.chain.execute((NextCommand) null, session, message);
	}
}