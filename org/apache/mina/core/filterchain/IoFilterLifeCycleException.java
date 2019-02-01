/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.filterchain;

public class IoFilterLifeCycleException extends RuntimeException {
	private static final long serialVersionUID = -5542098881633506449L;

	public IoFilterLifeCycleException() {
	}

	public IoFilterLifeCycleException(String message) {
		super(message);
	}

	public IoFilterLifeCycleException(String message, Throwable cause) {
		super(message, cause);
	}

	public IoFilterLifeCycleException(Throwable cause) {
		super(cause);
	}
}