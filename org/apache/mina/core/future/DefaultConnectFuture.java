/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.future;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;

public class DefaultConnectFuture extends DefaultIoFuture implements ConnectFuture {
	private static final Object CANCELED = new Object();

	public DefaultConnectFuture() {
		super((IoSession) null);
	}

	public static ConnectFuture newFailedFuture(Throwable exception) {
		DefaultConnectFuture failedFuture = new DefaultConnectFuture();
		failedFuture.setException(exception);
		return failedFuture;
	}

	public IoSession getSession() {
		Object v = this.getValue();
		if (v instanceof IoSession) {
			return (IoSession) v;
		} else if (v instanceof RuntimeException) {
			throw (RuntimeException) v;
		} else if (v instanceof Error) {
			throw (Error) v;
		} else if (v instanceof Throwable) {
			throw (RuntimeIoException) (new RuntimeIoException("Failed to get the session.")).initCause((Throwable) v);
		} else {
			return null;
		}
	}

	public Throwable getException() {
		Object v = this.getValue();
		return v instanceof Throwable ? (Throwable) v : null;
	}

	public boolean isConnected() {
		return this.getValue() instanceof IoSession;
	}

	public boolean isCanceled() {
		return this.getValue() == CANCELED;
	}

	public void setSession(IoSession session) {
		if (session == null) {
			throw new IllegalArgumentException("session");
		} else {
			this.setValue(session);
		}
	}

	public void setException(Throwable exception) {
		if (exception == null) {
			throw new IllegalArgumentException("exception");
		} else {
			this.setValue(exception);
		}
	}

	public boolean cancel() {
		return this.setValue(CANCELED);
	}

	public ConnectFuture await() throws InterruptedException {
		return (ConnectFuture) super.await();
	}

	public ConnectFuture awaitUninterruptibly() {
		return (ConnectFuture) super.awaitUninterruptibly();
	}

	public ConnectFuture addListener(IoFutureListener<?> listener) {
		return (ConnectFuture) super.addListener(listener);
	}

	public ConnectFuture removeListener(IoFutureListener<?> listener) {
		return (ConnectFuture) super.removeListener(listener);
	}
}