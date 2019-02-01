/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.future;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;

public interface ConnectFuture extends IoFuture {
	IoSession getSession();

	Throwable getException();

	boolean isConnected();

	boolean isCanceled();

	void setSession(IoSession arg0);

	void setException(Throwable arg0);

	boolean cancel();

	ConnectFuture await() throws InterruptedException;

	ConnectFuture awaitUninterruptibly();

	ConnectFuture addListener(IoFutureListener<?> arg0);

	ConnectFuture removeListener(IoFutureListener<?> arg0);
}