/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.future;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;

public interface WriteFuture extends IoFuture {
	boolean isWritten();

	Throwable getException();

	void setWritten();

	void setException(Throwable arg0);

	WriteFuture await() throws InterruptedException;

	WriteFuture awaitUninterruptibly();

	WriteFuture addListener(IoFutureListener<?> arg0);

	WriteFuture removeListener(IoFutureListener<?> arg0);
}