/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.future;

import java.util.concurrent.TimeUnit;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;

public interface IoFuture {
	IoSession getSession();

	IoFuture await() throws InterruptedException;

	boolean await(long arg0, TimeUnit arg2) throws InterruptedException;

	boolean await(long arg0) throws InterruptedException;

	IoFuture awaitUninterruptibly();

	boolean awaitUninterruptibly(long arg0, TimeUnit arg2);

	boolean awaitUninterruptibly(long arg0);

	@Deprecated
	void join();

	@Deprecated
	boolean join(long arg0);

	boolean isDone();

	IoFuture addListener(IoFutureListener<?> arg0);

	IoFuture removeListener(IoFutureListener<?> arg0);
}