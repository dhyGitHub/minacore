/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.future;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;

public class DefaultCloseFuture extends DefaultIoFuture implements CloseFuture {
	public DefaultCloseFuture(IoSession session) {
		super(session);
	}

	public boolean isClosed() {
		return this.isDone() ? ((Boolean) this.getValue()).booleanValue() : false;
	}

	public void setClosed() {
		this.setValue(Boolean.TRUE);
	}

	public CloseFuture await() throws InterruptedException {
		return (CloseFuture) super.await();
	}

	public CloseFuture awaitUninterruptibly() {
		return (CloseFuture) super.awaitUninterruptibly();
	}

	public CloseFuture addListener(IoFutureListener<?> listener) {
		return (CloseFuture) super.addListener(listener);
	}

	public CloseFuture removeListener(IoFutureListener<?> listener) {
		return (CloseFuture) super.removeListener(listener);
	}
}