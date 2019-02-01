/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util;

import org.apache.mina.util.ExceptionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultExceptionMonitor extends ExceptionMonitor {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMonitor.class);

	public void exceptionCaught(Throwable cause) {
		if (cause instanceof Error) {
			throw (Error) cause;
		} else {
			LOGGER.warn("Unexpected exception.", cause);
		}
	}
}