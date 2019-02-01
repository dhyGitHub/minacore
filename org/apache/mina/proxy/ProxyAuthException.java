/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy;

import javax.security.sasl.SaslException;

public class ProxyAuthException extends SaslException {
	private static final long serialVersionUID = -6511596809517532988L;

	public ProxyAuthException(String message) {
		super(message);
	}

	public ProxyAuthException(String message, Throwable ex) {
		super(message, ex);
	}
}