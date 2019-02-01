/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.service;

import java.net.SocketAddress;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSessionInitializer;

public interface IoConnector extends IoService {

	int getConnectTimeout();

	long getConnectTimeoutMillis();

	void setConnectTimeout(int arg0);

	void setConnectTimeoutMillis(long arg0);

	SocketAddress getDefaultRemoteAddress();

	void setDefaultRemoteAddress(SocketAddress arg0);

	SocketAddress getDefaultLocalAddress();

	void setDefaultLocalAddress(SocketAddress arg0);

	ConnectFuture connect();

	ConnectFuture connect(IoSessionInitializer<? extends ConnectFuture> arg0);

	ConnectFuture connect(SocketAddress arg0);

	ConnectFuture connect(SocketAddress arg0, IoSessionInitializer<? extends ConnectFuture> arg1);

	ConnectFuture connect(SocketAddress arg0, SocketAddress arg1);

	ConnectFuture connect(SocketAddress arg0, SocketAddress arg1, IoSessionInitializer<? extends ConnectFuture> arg2);
}