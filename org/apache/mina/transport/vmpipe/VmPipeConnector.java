/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.vmpipe;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.DefaultConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.AbstractIoConnector;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IdleStatusChecker;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.transport.vmpipe.DefaultVmPipeSessionConfig;
import org.apache.mina.transport.vmpipe.VmPipe;
import org.apache.mina.transport.vmpipe.VmPipeAcceptor;
import org.apache.mina.transport.vmpipe.VmPipeAddress;
import org.apache.mina.transport.vmpipe.VmPipeFilterChain;
import org.apache.mina.transport.vmpipe.VmPipeSession;
import org.apache.mina.transport.vmpipe.VmPipeSessionConfig;
import org.apache.mina.util.ExceptionMonitor;

public final class VmPipeConnector extends AbstractIoConnector {
	private IdleStatusChecker idleChecker;
	private static final Set<VmPipeAddress> TAKEN_LOCAL_ADDRESSES = new HashSet();
	private static int nextLocalPort = -1;
	private static final IoFutureListener<IoFuture> LOCAL_ADDRESS_RECLAIMER = new VmPipeConnector.LocalAddressReclaimer();

	public VmPipeConnector() {
		this((Executor) null);
	}

	public VmPipeConnector(Executor executor) {
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

	protected ConnectFuture connect0(SocketAddress remoteAddress, SocketAddress localAddress,
			IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
		VmPipe entry = (VmPipe) VmPipeAcceptor.boundHandlers.get(remoteAddress);
		if (entry == null) {
			return DefaultConnectFuture.newFailedFuture(new IOException("Endpoint unavailable: " + remoteAddress));
		} else {
			DefaultConnectFuture future = new DefaultConnectFuture();

			VmPipeAddress actualLocalAddress;
			try {
				actualLocalAddress = nextLocalAddress();
			} catch (IOException arg11) {
				return DefaultConnectFuture.newFailedFuture(arg11);
			}

			VmPipeSession localSession = new VmPipeSession(this, this.getListeners(), actualLocalAddress,
					this.getHandler(), entry);
			this.initSession(localSession, future, sessionInitializer);
			localSession.getCloseFuture().addListener(LOCAL_ADDRESS_RECLAIMER);

			try {
				IoFilterChain remoteSession = localSession.getFilterChain();
				this.getFilterChainBuilder().buildFilterChain(remoteSession);
				this.getListeners().fireSessionCreated(localSession);
				this.idleChecker.addSession(localSession);
			} catch (Exception arg10) {
				future.setException(arg10);
				return future;
			}

			VmPipeSession remoteSession1 = localSession.getRemoteSession();
			((VmPipeAcceptor) remoteSession1.getService()).doFinishSessionInitialization(remoteSession1,
					(IoFuture) null);

			try {
				IoFilterChain e = remoteSession1.getFilterChain();
				entry.getAcceptor().getFilterChainBuilder().buildFilterChain(e);
				entry.getListeners().fireSessionCreated(remoteSession1);
				this.idleChecker.addSession(remoteSession1);
			} catch (Exception arg9) {
				ExceptionMonitor.getInstance().exceptionCaught(arg9);
				remoteSession1.closeNow();
			}

			((VmPipeFilterChain) localSession.getFilterChain()).start();
			((VmPipeFilterChain) remoteSession1.getFilterChain()).start();
			return future;
		}
	}

	protected void dispose0() throws Exception {
		this.idleChecker.getNotifyingTask().cancel();
	}

	private static VmPipeAddress nextLocalAddress() throws IOException {
		Set arg = TAKEN_LOCAL_ADDRESSES;
		synchronized (TAKEN_LOCAL_ADDRESSES) {
			if (nextLocalPort >= 0) {
				nextLocalPort = -1;
			}

			for (int i = 0; i < Integer.MAX_VALUE; ++i) {
				VmPipeAddress answer = new VmPipeAddress(nextLocalPort--);
				if (!TAKEN_LOCAL_ADDRESSES.contains(answer)) {
					TAKEN_LOCAL_ADDRESSES.add(answer);
					return answer;
				}
			}

			throw new IOException("Can\'t assign a local VM pipe port.");
		}
	}

	private static class LocalAddressReclaimer implements IoFutureListener<IoFuture> {
		private LocalAddressReclaimer() {
		}

		public void operationComplete(IoFuture future) {
			synchronized (VmPipeConnector.TAKEN_LOCAL_ADDRESSES) {
				VmPipeConnector.TAKEN_LOCAL_ADDRESSES.remove(future.getSession().getLocalAddress());
			}
		}
	}
}