/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.future;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;

public interface CloseFuture extends IoFuture {
	boolean isClosed();

	void setClosed();

	CloseFuture await() throws InterruptedException;

	CloseFuture awaitUninterruptibly();

	CloseFuture addListener(IoFutureListener<?> arg0);

	CloseFuture removeListener(IoFutureListener<?> arg0);
}