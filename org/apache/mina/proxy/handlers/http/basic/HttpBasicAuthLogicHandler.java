/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http.basic;

import java.util.HashMap;
import java.util.Map;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.HttpProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpProxyResponse;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.StringUtilities;
import org.apache.mina.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpBasicAuthLogicHandler extends AbstractAuthLogicHandler {
	private static final Logger logger = LoggerFactory.getLogger(HttpBasicAuthLogicHandler.class);

	public HttpBasicAuthLogicHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
		super(proxyIoSession);
		((HttpProxyRequest) this.request).checkRequiredProperties(new String[] { "USER", "PWD" });
	}

	public void doHandshake(NextFilter nextFilter) throws ProxyAuthException {
		logger.debug(" doHandshake()");
		if (this.step > 0) {
			throw new ProxyAuthException("Authentication request already sent");
		} else {
			HttpProxyRequest req = (HttpProxyRequest) this.request;
			Object headers = req.getHeaders() != null ? req.getHeaders() : new HashMap();
			String username = (String) req.getProperties().get("USER");
			String password = (String) req.getProperties().get("PWD");
			StringUtilities.addValueToHeader((Map) headers, "Proxy-Authorization",
					"Basic " + createAuthorization(username, password), true);
			addKeepAliveHeaders((Map) headers);
			req.setHeaders((Map) headers);
			this.writeRequest(nextFilter, req);
			++this.step;
		}
	}

	public static String createAuthorization(String username, String password) {
		return new String(Base64.encodeBase64((username + ":" + password).getBytes()));
	}

	public void handleResponse(HttpProxyResponse response) throws ProxyAuthException {
		if (response.getStatusCode() != 407) {
			throw new ProxyAuthException("Received error response code (" + response.getStatusLine() + ").");
		}
	}
}