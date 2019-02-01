/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.session.ProxyIoSession;

public interface ProxyLogicHandler {
	boolean isHandshakeComplete();

	void messageReceived(NextFilter arg0, IoBuffer arg1) throws ProxyAuthException;

	void doHandshake(NextFilter arg0) throws ProxyAuthException;

	ProxyIoSession getProxyIoSession();

	void enqueueWriteRequest(NextFilter arg0, WriteRequest arg1);
}