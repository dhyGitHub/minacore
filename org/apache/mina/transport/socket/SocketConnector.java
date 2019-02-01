/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket;

import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.transport.socket.SocketSessionConfig;

public interface SocketConnector extends IoConnector {
	InetSocketAddress getDefaultRemoteAddress();

	SocketSessionConfig getSessionConfig();

	void setDefaultRemoteAddress(InetSocketAddress arg0);
}