/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.service;

import java.util.Map;
import java.util.Set;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.service.IoServiceStatistics;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionDataStructureFactory;

public interface IoService {
	TransportMetadata getTransportMetadata();

	void addListener(IoServiceListener arg0);

	void removeListener(IoServiceListener arg0);

	boolean isDisposing();

	boolean isDisposed();

	void dispose();

	void dispose(boolean arg0);

	IoHandler getHandler();

	void setHandler(IoHandler arg0);

	Map<Long, IoSession> getManagedSessions();

	int getManagedSessionCount();

	IoSessionConfig getSessionConfig();

	IoFilterChainBuilder getFilterChainBuilder();

	void setFilterChainBuilder(IoFilterChainBuilder arg0);

	DefaultIoFilterChainBuilder getFilterChain();

	boolean isActive();

	long getActivationTime();

	Set<WriteFuture> broadcast(Object arg0);

	IoSessionDataStructureFactory getSessionDataStructureFactory();

	void setSessionDataStructureFactory(IoSessionDataStructureFactory arg0);

	int getScheduledWriteBytes();

	int getScheduledWriteMessages();

	IoServiceStatistics getStatistics();
}