/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers;

import java.net.InetSocketAddress;

public abstract class ProxyRequest {
	private InetSocketAddress endpointAddress = null;

	public ProxyRequest() {
	}

	public ProxyRequest(InetSocketAddress endpointAddress) {
		this.endpointAddress = endpointAddress;
	}

	public InetSocketAddress getEndpointAddress() {
		return this.endpointAddress;
	}
}