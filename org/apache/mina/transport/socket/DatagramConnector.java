/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket;

import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.transport.socket.DatagramSessionConfig;

public interface DatagramConnector extends IoConnector {
	InetSocketAddress getDefaultRemoteAddress();

	DatagramSessionConfig getSessionConfig();

	void setDefaultRemoteAddress(InetSocketAddress arg0);
}