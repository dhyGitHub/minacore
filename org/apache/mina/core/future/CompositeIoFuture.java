/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.future;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;

public class CompositeIoFuture<E extends IoFuture> extends DefaultIoFuture {
	private final CompositeIoFuture<E>.NotifyingListener listener = new CompositeIoFuture.NotifyingListener();
	private final AtomicInteger unnotified = new AtomicInteger();
	private volatile boolean constructionFinished;

	public CompositeIoFuture(Iterable<E> children) {
		super((IoSession) null);
		Iterator i$ = children.iterator();

		while (i$.hasNext()) {
			IoFuture f = (IoFuture) i$.next();
			f.addListener(this.listener);
			this.unnotified.incrementAndGet();
		}

		this.constructionFinished = true;
		if (this.unnotified.get() == 0) {
			this.setValue(Boolean.valueOf(true));
		}

	}

	private class NotifyingListener implements IoFutureListener<IoFuture> {
		private NotifyingListener() {
		}

		public void operationComplete(IoFuture future) {
			if (CompositeIoFuture.this.unnotified.decrementAndGet() == 0
					&& CompositeIoFuture.this.constructionFinished) {
				CompositeIoFuture.this.setValue(Boolean.valueOf(true));
			}

		}
	}
}