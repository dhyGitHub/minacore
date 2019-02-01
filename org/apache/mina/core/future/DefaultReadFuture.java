/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.future;

import java.io.IOException;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.session.IoSession;

public class DefaultReadFuture extends DefaultIoFuture implements ReadFuture {
	private static final Object CLOSED = new Object();

	public DefaultReadFuture(IoSession session) {
		super(session);
	}

	public Object getMessage() {
		if (this.isDone()) {
			Object v = this.getValue();
			if (v == CLOSED) {
				return null;
			} else if (v instanceof RuntimeException) {
				throw (RuntimeException) v;
			} else if (v instanceof Error) {
				throw (Error) v;
			} else if (!(v instanceof IOException) && !(v instanceof Exception)) {
				return v;
			} else {
				throw new RuntimeIoException((Exception) v);
			}
		} else {
			return null;
		}
	}

	public boolean isRead() {
		if (!this.isDone()) {
			return false;
		} else {
			Object v = this.getValue();
			return v != CLOSED && !(v instanceof Throwable);
		}
	}

	public boolean isClosed() {
		return this.isDone() ? this.getValue() == CLOSED : false;
	}

	public Throwable getException() {
		if (this.isDone()) {
			Object v = this.getValue();
			if (v instanceof Throwable) {
				return (Throwable) v;
			}
		}

		return null;
	}

	public void setClosed() {
		this.setValue(CLOSED);
	}

	public void setRead(Object message) {
		if (message == null) {
			throw new IllegalArgumentException("message");
		} else {
			this.setValue(message);
		}
	}

	public void setException(Throwable exception) {
		if (exception == null) {
			throw new IllegalArgumentException("exception");
		} else {
			this.setValue(exception);
		}
	}

	public ReadFuture await() throws InterruptedException {
		return (ReadFuture) super.await();
	}

	public ReadFuture awaitUninterruptibly() {
		return (ReadFuture) super.awaitUninterruptibly();
	}

	public ReadFuture addListener(IoFutureListener<?> listener) {
		return (ReadFuture) super.addListener(listener);
	}

	public ReadFuture removeListener(IoFutureListener<?> listener) {
		return (ReadFuture) super.removeListener(listener);
	}
}