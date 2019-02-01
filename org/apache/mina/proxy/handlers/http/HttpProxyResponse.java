/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http;

import java.util.List;
import java.util.Map;

public class HttpProxyResponse {
	private final String httpVersion;
	private final String statusLine;
	private final int statusCode;
	private final Map<String, List<String>> headers;
	private String body;

	protected HttpProxyResponse(String httpVersion, String statusLine, Map<String, List<String>> headers) {
		this.httpVersion = httpVersion;
		this.statusLine = statusLine;
		this.statusCode = statusLine.charAt(0) == 32 ? Integer.parseInt(statusLine.substring(1, 4))
				: Integer.parseInt(statusLine.substring(0, 3));
		this.headers = headers;
	}

	public final String getHttpVersion() {
		return this.httpVersion;
	}

	public final int getStatusCode() {
		return this.statusCode;
	}

	public final String getStatusLine() {
		return this.statusLine;
	}

	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public final Map<String, List<String>> getHeaders() {
		return this.headers;
	}
}