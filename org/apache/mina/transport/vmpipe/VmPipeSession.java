/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.vmpipe;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListenerSupport;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.transport.vmpipe.DefaultVmPipeSessionConfig;
import org.apache.mina.transport.vmpipe.VmPipe;
import org.apache.mina.transport.vmpipe.VmPipeAddress;
import org.apache.mina.transport.vmpipe.VmPipeFilterChain;
import org.apache.mina.transport.vmpipe.VmPipeSessionConfig;

class VmPipeSession extends AbstractIoSession {
	static final TransportMetadata METADATA = new DefaultTransportMetadata("mina", "vmpipe", false, false,
			VmPipeAddress.class, VmPipeSessionConfig.class, new Class[] { Object.class });
	private final IoServiceListenerSupport serviceListeners;
	private final VmPipeAddress localAddress;
	private final VmPipeAddress remoteAddress;
	private final VmPipeAddress serviceAddress;
	private final VmPipeFilterChain filterChain;
	private final VmPipeSession remoteSession;
	private final Lock lock;
	final BlockingQueue<Object> receivedMessageQueue;

	VmPipeSession(IoService service, IoServiceListenerSupport serviceListeners, VmPipeAddress localAddress,
			IoHandler handler, VmPipe remoteEntry) {
		super(service);
		this.config = new DefaultVmPipeSessionConfig();
		this.serviceListeners = serviceListeners;
		this.lock = new ReentrantLock();
		this.localAddress = localAddress;
		this.remoteAddress = this.serviceAddress = remoteEntry.getAddress();
		this.filterChain = new VmPipeFilterChain(this);
		this.receivedMessageQueue = new LinkedBlockingQueue();
		this.remoteSession = new VmPipeSession(this, remoteEntry);
	}

	private VmPipeSession(VmPipeSession remoteSession, VmPipe entry) {
		super(entry.getAcceptor());
		this.config = new DefaultVmPipeSessionConfig();
		this.serviceListeners = entry.getListeners();
		this.lock = remoteSession.lock;
		this.localAddress = this.serviceAddress = remoteSession.remoteAddress;
		this.remoteAddress = remoteSession.localAddress;
		this.filterChain = new VmPipeFilterChain(this);
		this.remoteSession = remoteSession;
		this.receivedMessageQueue = new LinkedBlockingQueue();
	}

	public IoProcessor<VmPipeSession> getProcessor() {
		return this.filterChain.getProcessor();
	}

	IoServiceListenerSupport getServiceListeners() {
		return this.serviceListeners;
	}

	public VmPipeSessionConfig getConfig() {
		return (VmPipeSessionConfig) this.config;
	}

	public IoFilterChain getFilterChain() {
		return this.filterChain;
	}

	public VmPipeSession getRemoteSession() {
		return this.remoteSession;
	}

	public TransportMetadata getTransportMetadata() {
		return METADATA;
	}

	public VmPipeAddress getRemoteAddress() {
		return this.remoteAddress;
	}

	public VmPipeAddress getLocalAddress() {
		return this.localAddress;
	}

	public VmPipeAddress getServiceAddress() {
		return this.serviceAddress;
	}

	void increaseWrittenBytes0(int increment, long currentTime) {
		super.increaseWrittenBytes(increment, currentTime);
	}

	WriteRequestQueue getWriteRequestQueue0() {
		return super.getWriteRequestQueue();
	}

	Lock getLock() {
		return this.lock;
	}
}