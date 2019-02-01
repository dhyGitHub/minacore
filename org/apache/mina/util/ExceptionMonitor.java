/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util;

import org.apache.mina.util.DefaultExceptionMonitor;

public abstract class ExceptionMonitor {
	private static ExceptionMonitor instance = new DefaultExceptionMonitor();

	public static ExceptionMonitor getInstance() {
		return instance;
	}

	public static void setInstance(ExceptionMonitor monitor) {
		if (monitor == null) {
			monitor = new DefaultExceptionMonitor();
		}

		instance = (ExceptionMonitor) monitor;
	}

	public abstract void exceptionCaught(Throwable arg0);
}