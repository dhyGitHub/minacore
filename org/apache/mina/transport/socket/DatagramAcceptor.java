/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket;

import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSessionRecycler;
import org.apache.mina.transport.socket.DatagramSessionConfig;

public interface DatagramAcceptor extends IoAcceptor {
	InetSocketAddress getLocalAddress();

	InetSocketAddress getDefaultLocalAddress();

	void setDefaultLocalAddress(InetSocketAddress arg0);

	IoSessionRecycler getSessionRecycler();

	void setSessionRecycler(IoSessionRecycler arg0);

	DatagramSessionConfig getSessionConfig();
}