/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.vmpipe;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.service.AbstractIoAcceptor;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IdleStatusChecker;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.transport.vmpipe.DefaultVmPipeSessionConfig;
import org.apache.mina.transport.vmpipe.VmPipe;
import org.apache.mina.transport.vmpipe.VmPipeAddress;
import org.apache.mina.transport.vmpipe.VmPipeSession;
import org.apache.mina.transport.vmpipe.VmPipeSessionConfig;

public final class VmPipeAcceptor extends AbstractIoAcceptor {
	private IdleStatusChecker idleChecker;
	static final Map<VmPipeAddress, VmPipe> boundHandlers = new HashMap();

	public VmPipeAcceptor() {
		this((Executor) null);
	}

	public VmPipeAcceptor(Executor executor) {
		super(new DefaultVmPipeSessionConfig(), executor);
		this.idleChecker = new IdleStatusChecker();
		this.executeWorker(this.idleChecker.getNotifyingTask(), "idleStatusChecker");
	}

	public TransportMetadata getTransportMetadata() {
		return VmPipeSession.METADATA;
	}

	public VmPipeSessionConfig getSessionConfig() {
		return (VmPipeSessionConfig) this.sessionConfig;
	}

	public VmPipeAddress getLocalAddress() {
		return (VmPipeAddress) super.getLocalAddress();
	}

	public VmPipeAddress getDefaultLocalAddress() {
		return (VmPipeAddress) super.getDefaultLocalAddress();
	}

	public void setDefaultLocalAddress(VmPipeAddress localAddress) {
		super.setDefaultLocalAddress(localAddress);
	}

	protected void dispose0() throws Exception {
		this.idleChecker.getNotifyingTask().cancel();
		this.unbind();
	}

	protected Set<SocketAddress> bindInternal(List<? extends SocketAddress> localAddresses) throws IOException {
		HashSet newLocalAddresses = new HashSet();
		Map arg2 = boundHandlers;
		synchronized (boundHandlers) {
			Iterator i$;
			SocketAddress a;
			VmPipeAddress localAddress;
			for (i$ = localAddresses.iterator(); i$.hasNext(); newLocalAddresses.add(localAddress)) {
				a = (SocketAddress) i$.next();
				localAddress = (VmPipeAddress) a;
				if (localAddress != null && localAddress.getPort() != 0) {
					if (localAddress.getPort() < 0) {
						throw new IOException("Bind port number must be 0 or above.");
					}

					if (boundHandlers.containsKey(localAddress)) {
						throw new IOException("Address already bound: " + localAddress);
					}
				} else {
					localAddress = null;

					for (int i$1 = 10000; i$1 < Integer.MAX_VALUE; ++i$1) {
						VmPipeAddress a2 = new VmPipeAddress(i$1);
						if (!boundHandlers.containsKey(a2) && !newLocalAddresses.contains(a2)) {
							localAddress = a2;
							break;
						}
					}

					if (localAddress == null) {
						throw new IOException("No port available.");
					}
				}
			}

			i$ = newLocalAddresses.iterator();

			while (i$.hasNext()) {
				a = (SocketAddress) i$.next();
				localAddress = (VmPipeAddress) a;
				if (boundHandlers.containsKey(localAddress)) {
					Iterator arg10 = newLocalAddresses.iterator();

					while (arg10.hasNext()) {
						SocketAddress arg11 = (SocketAddress) arg10.next();
						boundHandlers.remove(arg11);
					}

					throw new IOException("Duplicate local address: " + a);
				}

				boundHandlers.put(localAddress, new VmPipe(this, localAddress, this.getHandler(), this.getListeners()));
			}

			return newLocalAddresses;
		}
	}

	protected void unbind0(List<? extends SocketAddress> localAddresses) {
		Map arg1 = boundHandlers;
		synchronized (boundHandlers) {
			Iterator i$ = localAddresses.iterator();

			while (i$.hasNext()) {
				SocketAddress a = (SocketAddress) i$.next();
				boundHandlers.remove(a);
			}

		}
	}

	public IoSession newSession(SocketAddress remoteAddress, SocketAddress localAddress) {
		throw new UnsupportedOperationException();
	}

	void doFinishSessionInitialization(IoSession session, IoFuture future) {
		this.initSession(session, future, (IoSessionInitializer) null);
	}
}