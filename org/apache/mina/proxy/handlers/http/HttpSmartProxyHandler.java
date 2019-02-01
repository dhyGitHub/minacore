/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.AbstractHttpLogicHandler;
import org.apache.mina.proxy.handlers.http.HttpAuthenticationMethods;
import org.apache.mina.proxy.handlers.http.HttpProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpProxyResponse;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSmartProxyHandler extends AbstractHttpLogicHandler {
	private static final Logger logger = LoggerFactory.getLogger(HttpSmartProxyHandler.class);
	private boolean requestSent = false;
	private AbstractAuthLogicHandler authHandler;

	public HttpSmartProxyHandler(ProxyIoSession proxyIoSession) {
		super(proxyIoSession);
	}

	public void doHandshake(NextFilter nextFilter) throws ProxyAuthException {
		logger.debug(" doHandshake()");
		if (this.authHandler != null) {
			this.authHandler.doHandshake(nextFilter);
		} else {
			if (this.requestSent) {
				throw new ProxyAuthException("Authentication request already sent");
			}

			logger.debug("  sending HTTP request");
			HttpProxyRequest req = (HttpProxyRequest) this.getProxyIoSession().getRequest();
			Object headers = req.getHeaders() != null ? req.getHeaders() : new HashMap();
			AbstractAuthLogicHandler.addKeepAliveHeaders((Map) headers);
			req.setHeaders((Map) headers);
			this.writeRequest(nextFilter, req);
			this.requestSent = true;
		}

	}

	private void autoSelectAuthHandler(HttpProxyResponse response) throws ProxyAuthException {
		List values = (List) response.getHeaders().get("Proxy-Authenticate");
		ProxyIoSession proxyIoSession = this.getProxyIoSession();
		if (values != null && values.size() != 0) {
			if (this.getProxyIoSession().getPreferedOrder() == null) {
				int i$ = -1;
				Iterator method = values.iterator();

				label78: while (true) {
					while (true) {
						if (!method.hasNext()) {
							break label78;
						}

						String i$1 = (String) method.next();
						i$1 = i$1.toLowerCase();
						if (i$1.contains("ntlm")) {
							i$ = HttpAuthenticationMethods.NTLM.getId();
							break label78;
						}

						if (i$1.contains("digest") && i$ != HttpAuthenticationMethods.NTLM.getId()) {
							i$ = HttpAuthenticationMethods.DIGEST.getId();
						} else if (i$1.contains("basic") && i$ == -1) {
							i$ = HttpAuthenticationMethods.BASIC.getId();
						}
					}
				}

				if (i$ != -1) {
					try {
						this.authHandler = HttpAuthenticationMethods.getNewHandler(i$, proxyIoSession);
					} catch (Exception arg8) {
						logger.debug("Following exception occured:", arg8);
					}
				}

				if (this.authHandler == null) {
					this.authHandler = HttpAuthenticationMethods.NO_AUTH.getNewHandler(proxyIoSession);
				}
			} else {
				Iterator i$2 = proxyIoSession.getPreferedOrder().iterator();

				while (i$2.hasNext()) {
					HttpAuthenticationMethods method1 = (HttpAuthenticationMethods) i$2.next();
					if (this.authHandler != null) {
						break;
					}

					if (method1 == HttpAuthenticationMethods.NO_AUTH) {
						this.authHandler = HttpAuthenticationMethods.NO_AUTH.getNewHandler(proxyIoSession);
						break;
					}

					Iterator i$3 = values.iterator();

					while (i$3.hasNext()) {
						String proxyAuthHeader = (String) i$3.next();
						proxyAuthHeader = proxyAuthHeader.toLowerCase();

						try {
							if (proxyAuthHeader.contains("basic") && method1 == HttpAuthenticationMethods.BASIC) {
								this.authHandler = HttpAuthenticationMethods.BASIC.getNewHandler(proxyIoSession);
								break;
							}

							if (proxyAuthHeader.contains("digest") && method1 == HttpAuthenticationMethods.DIGEST) {
								this.authHandler = HttpAuthenticationMethods.DIGEST.getNewHandler(proxyIoSession);
								break;
							}

							if (proxyAuthHeader.contains("ntlm") && method1 == HttpAuthenticationMethods.NTLM) {
								this.authHandler = HttpAuthenticationMethods.NTLM.getNewHandler(proxyIoSession);
								break;
							}
						} catch (Exception arg9) {
							logger.debug("Following exception occured:", arg9);
						}
					}
				}
			}
		} else {
			this.authHandler = HttpAuthenticationMethods.NO_AUTH.getNewHandler(proxyIoSession);
		}

		if (this.authHandler == null) {
			throw new ProxyAuthException("Unknown authentication mechanism(s): " + values);
		}
	}

	public void handleResponse(HttpProxyResponse response) throws ProxyAuthException {
		if (!this.isHandshakeComplete() && ("close"
				.equalsIgnoreCase(StringUtilities.getSingleValuedHeader(response.getHeaders(), "Proxy-Connection"))
				|| "close".equalsIgnoreCase(
						StringUtilities.getSingleValuedHeader(response.getHeaders(), "Connection")))) {
			this.getProxyIoSession().setReconnectionNeeded(true);
		}

		if (response.getStatusCode() == 407) {
			if (this.authHandler == null) {
				this.autoSelectAuthHandler(response);
			}

			this.authHandler.handleResponse(response);
		} else {
			throw new ProxyAuthException(
					"Error: unexpected response code " + response.getStatusLine() + " received from proxy.");
		}
	}
}