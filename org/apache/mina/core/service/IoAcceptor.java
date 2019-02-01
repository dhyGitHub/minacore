/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.service;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.Set;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSession;

public interface IoAcceptor extends IoService {
	SocketAddress getLocalAddress();

	Set<SocketAddress> getLocalAddresses();

	SocketAddress getDefaultLocalAddress();

	List<SocketAddress> getDefaultLocalAddresses();

	void setDefaultLocalAddress(SocketAddress arg0);

	void setDefaultLocalAddresses(SocketAddress arg0, SocketAddress... arg1);

	void setDefaultLocalAddresses(Iterable<? extends SocketAddress> arg0);

	void setDefaultLocalAddresses(List<? extends SocketAddress> arg0);

	boolean isCloseOnDeactivation();

	void setCloseOnDeactivation(boolean arg0);

	void bind() throws IOException;

	void bind(SocketAddress arg0) throws IOException;

	void bind(SocketAddress arg0, SocketAddress... arg1) throws IOException;

	void bind(SocketAddress... arg0) throws IOException;

	void bind(Iterable<? extends SocketAddress> arg0) throws IOException;

	void unbind();

	void unbind(SocketAddress arg0);

	void unbind(SocketAddress arg0, SocketAddress... arg1);

	void unbind(Iterable<? extends SocketAddress> arg0);

	IoSession newSession(SocketAddress arg0, SocketAddress arg1);
}