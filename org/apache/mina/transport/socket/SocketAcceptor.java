/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket;

import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;

public interface SocketAcceptor extends IoAcceptor {
	InetSocketAddress getLocalAddress();

	InetSocketAddress getDefaultLocalAddress();

	void setDefaultLocalAddress(InetSocketAddress arg0);

	boolean isReuseAddress();

	void setReuseAddress(boolean arg0);

	int getBacklog();

	void setBacklog(int arg0);

	SocketSessionConfig getSessionConfig();
}