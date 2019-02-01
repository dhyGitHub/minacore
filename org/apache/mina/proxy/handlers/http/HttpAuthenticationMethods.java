/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http;

import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.basic.HttpBasicAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.basic.HttpNoAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.digest.HttpDigestAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.ntlm.HttpNTLMAuthLogicHandler;
import org.apache.mina.proxy.session.ProxyIoSession;

public enum HttpAuthenticationMethods {
	NO_AUTH(1), BASIC(2), NTLM(3), DIGEST(4);

	private final int id;

	private HttpAuthenticationMethods(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public AbstractAuthLogicHandler getNewHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
		return getNewHandler(this.id, proxyIoSession);
	}

	public static AbstractAuthLogicHandler getNewHandler(int method, ProxyIoSession proxyIoSession)
			throws ProxyAuthException {
		return (AbstractAuthLogicHandler) (method == BASIC.id ? new HttpBasicAuthLogicHandler(proxyIoSession)
				: (method == DIGEST.id ? new HttpDigestAuthLogicHandler(proxyIoSession)
						: (method == NTLM.id ? new HttpNTLMAuthLogicHandler(proxyIoSession)
								: (method == NO_AUTH.id ? new HttpNoAuthLogicHandler(proxyIoSession) : null))));
	}
}