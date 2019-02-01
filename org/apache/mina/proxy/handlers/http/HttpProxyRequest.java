/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.ProxyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpProxyRequest extends ProxyRequest {
	private static final Logger logger = LoggerFactory.getLogger(HttpProxyRequest.class);
	private final String httpVerb;
	private final String httpURI;
	private String httpVersion;
	private String host;
	private Map<String, List<String>> headers;
	private transient Map<String, String> properties;

	public HttpProxyRequest(InetSocketAddress endpointAddress) {
		this((InetSocketAddress) endpointAddress, "HTTP/1.0", (Map) null);
	}

	public HttpProxyRequest(InetSocketAddress endpointAddress, String httpVersion) {
		this((InetSocketAddress) endpointAddress, httpVersion, (Map) null);
	}

	public HttpProxyRequest(InetSocketAddress endpointAddress, String httpVersion, Map<String, List<String>> headers) {
		this.httpVerb = "CONNECT";
		if (!endpointAddress.isUnresolved()) {
			this.httpURI = endpointAddress.getHostName() + ":" + endpointAddress.getPort();
		} else {
			this.httpURI = endpointAddress.getAddress().getHostAddress() + ":" + endpointAddress.getPort();
		}

		this.httpVersion = httpVersion;
		this.headers = headers;
	}

	public HttpProxyRequest(String httpURI) {
		this("GET", httpURI, "HTTP/1.0", (Map) null);
	}

	public HttpProxyRequest(String httpURI, String httpVersion) {
		this("GET", httpURI, httpVersion, (Map) null);
	}

	public HttpProxyRequest(String httpVerb, String httpURI, String httpVersion) {
		this(httpVerb, httpURI, httpVersion, (Map) null);
	}

	public HttpProxyRequest(String httpVerb, String httpURI, String httpVersion, Map<String, List<String>> headers) {
		this.httpVerb = httpVerb;
		this.httpURI = httpURI;
		this.httpVersion = httpVersion;
		this.headers = headers;
	}

	public final String getHttpVerb() {
		return this.httpVerb;
	}

	public String getHttpVersion() {
		return this.httpVersion;
	}

	public void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}

	public final synchronized String getHost() {
		if (this.host == null) {
			if (this.getEndpointAddress() != null && !this.getEndpointAddress().isUnresolved()) {
				this.host = this.getEndpointAddress().getHostName();
			}

			if (this.host == null && this.httpURI != null) {
				try {
					this.host = (new URL(this.httpURI)).getHost();
				} catch (MalformedURLException arg1) {
					logger.debug("Malformed URL", arg1);
				}
			}
		}

		return this.host;
	}

	public final String getHttpURI() {
		return this.httpURI;
	}

	public final Map<String, List<String>> getHeaders() {
		return this.headers;
	}

	public final void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	public Map<String, String> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public void checkRequiredProperties(String... propNames) throws ProxyAuthException {
		StringBuilder sb = new StringBuilder();
		String[] arr$ = propNames;
		int len$ = propNames.length;

		for (int i$ = 0; i$ < len$; ++i$) {
			String propertyName = arr$[i$];
			if (this.properties.get(propertyName) == null) {
				sb.append(propertyName).append(' ');
			}
		}

		if (sb.length() > 0) {
			sb.append("property(ies) missing in request");
			throw new ProxyAuthException(sb.toString());
		}
	}

	public String toHttpString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getHttpVerb()).append(' ').append(this.getHttpURI()).append(' ').append(this.getHttpVersion())
				.append("\r\n");
		boolean hostHeaderFound = false;
		if (this.getHeaders() != null) {
			Iterator i$ = this.getHeaders().entrySet().iterator();

			while (i$.hasNext()) {
				Entry header = (Entry) i$.next();
				if (!hostHeaderFound) {
					hostHeaderFound = ((String) header.getKey()).equalsIgnoreCase("host");
				}

				Iterator i$1 = ((List) header.getValue()).iterator();

				while (i$1.hasNext()) {
					String value = (String) i$1.next();
					sb.append((String) header.getKey()).append(": ").append(value).append("\r\n");
				}
			}

			if (!hostHeaderFound && this.getHttpVersion() == "HTTP/1.1") {
				sb.append("Host: ").append(this.getHost()).append("\r\n");
			}
		}

		sb.append("\r\n");
		return sb.toString();
	}
}