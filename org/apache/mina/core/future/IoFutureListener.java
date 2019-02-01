/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.future;

import java.util.EventListener;
import org.apache.mina.core.future.IoFuture;

public interface IoFutureListener<F extends IoFuture> extends EventListener {
	IoFutureListener<IoFuture> CLOSE = new IoFutureListener() {
		public void operationComplete(IoFuture future) {
			future.getSession().closeNow();
		}
	};

	void operationComplete(F arg0);
}