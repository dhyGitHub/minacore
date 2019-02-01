/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.future;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;

public interface ReadFuture extends IoFuture {
	Object getMessage();

	boolean isRead();

	boolean isClosed();

	Throwable getException();

	void setRead(Object arg0);

	void setClosed();

	void setException(Throwable arg0);

	ReadFuture await() throws InterruptedException;

	ReadFuture awaitUninterruptibly();

	ReadFuture addListener(IoFutureListener<?> arg0);

	ReadFuture removeListener(IoFutureListener<?> arg0);
}