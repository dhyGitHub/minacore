/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import java.net.SocketAddress;
import java.util.Set;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;

public interface IoSession {
	long getId();

	IoService getService();

	IoHandler getHandler();

	IoSessionConfig getConfig();

	IoFilterChain getFilterChain();

	WriteRequestQueue getWriteRequestQueue();

	TransportMetadata getTransportMetadata();

	ReadFuture read();

	WriteFuture write(Object arg0);

	WriteFuture write(Object arg0, SocketAddress arg1);

	CloseFuture close(boolean arg0);

	CloseFuture closeNow();

	CloseFuture closeOnFlush();

	@Deprecated
	CloseFuture close();

	@Deprecated
	Object getAttachment();

	@Deprecated
	Object setAttachment(Object arg0);

	Object getAttribute(Object arg0);

	Object getAttribute(Object arg0, Object arg1);

	Object setAttribute(Object arg0, Object arg1);

	Object setAttribute(Object arg0);

	Object setAttributeIfAbsent(Object arg0, Object arg1);

	Object setAttributeIfAbsent(Object arg0);

	Object removeAttribute(Object arg0);

	boolean removeAttribute(Object arg0, Object arg1);

	boolean replaceAttribute(Object arg0, Object arg1, Object arg2);

	boolean containsAttribute(Object arg0);

	Set<Object> getAttributeKeys();

	boolean isConnected();

	boolean isActive();

	boolean isClosing();

	boolean isSecured();

	CloseFuture getCloseFuture();

	SocketAddress getRemoteAddress();

	SocketAddress getLocalAddress();

	SocketAddress getServiceAddress();

	void setCurrentWriteRequest(WriteRequest arg0);

	void suspendRead();

	void suspendWrite();

	void resumeRead();

	void resumeWrite();

	boolean isReadSuspended();

	boolean isWriteSuspended();

	void updateThroughput(long arg0, boolean arg2);

	long getReadBytes();

	long getWrittenBytes();

	long getReadMessages();

	long getWrittenMessages();

	double getReadBytesThroughput();

	double getWrittenBytesThroughput();

	double getReadMessagesThroughput();

	double getWrittenMessagesThroughput();

	int getScheduledWriteMessages();

	long getScheduledWriteBytes();

	Object getCurrentWriteMessage();

	WriteRequest getCurrentWriteRequest();

	long getCreationTime();

	long getLastIoTime();

	long getLastReadTime();

	long getLastWriteTime();

	boolean isIdle(IdleStatus arg0);

	boolean isReaderIdle();

	boolean isWriterIdle();

	boolean isBothIdle();

	int getIdleCount(IdleStatus arg0);

	int getReaderIdleCount();

	int getWriterIdleCount();

	int getBothIdleCount();

	long getLastIdleTime(IdleStatus arg0);

	long getLastReaderIdleTime();

	long getLastWriterIdleTime();

	long getLastBothIdleTime();
}