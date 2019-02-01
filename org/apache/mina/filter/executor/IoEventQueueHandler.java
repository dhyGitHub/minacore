/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.executor;

import java.util.EventListener;
import org.apache.mina.core.session.IoEvent;

public interface IoEventQueueHandler extends EventListener {
	IoEventQueueHandler NOOP = new IoEventQueueHandler() {
		public boolean accept(Object source, IoEvent event) {
			return true;
		}

		public void offered(Object source, IoEvent event) {
		}

		public void polled(Object source, IoEvent event) {
		}
	};

	boolean accept(Object arg0, IoEvent arg1);

	void offered(Object arg0, IoEvent arg1);

	void polled(Object arg0, IoEvent arg1);
}