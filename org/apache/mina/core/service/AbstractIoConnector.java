/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.service;

import java.net.SocketAddress;
import java.util.concurrent.Executor;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionInitializer;

public abstract class AbstractIoConnector extends AbstractIoService implements IoConnector {
	private long connectTimeoutCheckInterval = 50L;
	private long connectTimeoutInMillis = 60000L;
	private SocketAddress defaultRemoteAddress;
	private SocketAddress defaultLocalAddress;

	protected AbstractIoConnector(IoSessionConfig sessionConfig, Executor executor) {
		super(sessionConfig, executor);
	}

	public long getConnectTimeoutCheckInterval() {
		return this.connectTimeoutCheckInterval;
	}

	public void setConnectTimeoutCheckInterval(long minimumConnectTimeout) {
		if (this.getConnectTimeoutMillis() < minimumConnectTimeout) {
			this.connectTimeoutInMillis = minimumConnectTimeout;
		}

		this.connectTimeoutCheckInterval = minimumConnectTimeout;
	}

	public final int getConnectTimeout() {
		return (int) this.connectTimeoutInMillis / 1000;
	}

	public final long getConnectTimeoutMillis() {
		return this.connectTimeoutInMillis;
	}

	public final void setConnectTimeout(int connectTimeout) {
		this.setConnectTimeoutMillis((long) connectTimeout * 1000L);
	}

	public final void setConnectTimeoutMillis(long connectTimeoutInMillis) {
		if (connectTimeoutInMillis <= this.connectTimeoutCheckInterval) {
			this.connectTimeoutCheckInterval = connectTimeoutInMillis;
		}

		this.connectTimeoutInMillis = connectTimeoutInMillis;
	}

	public SocketAddress getDefaultRemoteAddress() {
		return this.defaultRemoteAddress;
	}

	public final void setDefaultLocalAddress(SocketAddress localAddress) {
		this.defaultLocalAddress = localAddress;
	}

	public final SocketAddress getDefaultLocalAddress() {
		return this.defaultLocalAddress;
	}

	public final void setDefaultRemoteAddress(SocketAddress defaultRemoteAddress) {
		if (defaultRemoteAddress == null) {
			throw new IllegalArgumentException("defaultRemoteAddress");
		} else if (!this.getTransportMetadata().getAddressType().isAssignableFrom(defaultRemoteAddress.getClass())) {
			throw new IllegalArgumentException("defaultRemoteAddress type: " + defaultRemoteAddress.getClass()
					+ " (expected: " + this.getTransportMetadata().getAddressType() + ")");
		} else {
			this.defaultRemoteAddress = defaultRemoteAddress;
		}
	}

	public final ConnectFuture connect() {
		SocketAddress defaultRemoteAddress = this.getDefaultRemoteAddress();
		if (defaultRemoteAddress == null) {
			throw new IllegalStateException("defaultRemoteAddress is not set.");
		} else {
			return this.connect(defaultRemoteAddress, (SocketAddress) null, (IoSessionInitializer) null);
		}
	}

	public ConnectFuture connect(IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
		SocketAddress defaultRemoteAddress = this.getDefaultRemoteAddress();
		if (defaultRemoteAddress == null) {
			throw new IllegalStateException("defaultRemoteAddress is not set.");
		} else {
			return this.connect(defaultRemoteAddress, (SocketAddress) null, sessionInitializer);
		}
	}

	public final ConnectFuture connect(SocketAddress remoteAddress) {
		return this.connect(remoteAddress, (SocketAddress) null, (IoSessionInitializer) null);
	}

	public ConnectFuture connect(SocketAddress remoteAddress,
			IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
		return this.connect(remoteAddress, (SocketAddress) null, sessionInitializer);
	}

	public ConnectFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
		return this.connect(remoteAddress, localAddress, (IoSessionInitializer) null);
	}

	public final ConnectFuture connect(SocketAddress remoteAddress, SocketAddress localAddress,
			IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
		if (this.isDisposing()) {
			throw new IllegalStateException("The connector is being disposed.");
		} else if (remoteAddress == null) {
			throw new IllegalArgumentException("remoteAddress");
		} else if (!this.getTransportMetadata().getAddressType().isAssignableFrom(remoteAddress.getClass())) {
			throw new IllegalArgumentException("remoteAddress type: " + remoteAddress.getClass() + " (expected: "
					+ this.getTransportMetadata().getAddressType() + ")");
		} else if (localAddress != null
				&& !this.getTransportMetadata().getAddressType().isAssignableFrom(localAddress.getClass())) {
			throw new IllegalArgumentException("localAddress type: " + localAddress.getClass() + " (expected: "
					+ this.getTransportMetadata().getAddressType() + ")");
		} else {
			if (this.getHandler() == null) {
				if (!this.getSessionConfig().isUseReadOperation()) {
					throw new IllegalStateException("handler is not set.");
				}

				this.setHandler(new IoHandler() {
					public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
					}

					public void messageReceived(IoSession session, Object message) throws Exception {
					}

					public void messageSent(IoSession session, Object message) throws Exception {
					}

					public void sessionClosed(IoSession session) throws Exception {
					}

					public void sessionCreated(IoSession session) throws Exception {
					}

					public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
					}

					public void sessionOpened(IoSession session) throws Exception {
					}

					public void inputClosed(IoSession session) throws Exception {
					}
				});
			}

			return this.connect0(remoteAddress, localAddress, sessionInitializer);
		}
	}

	protected abstract ConnectFuture connect0(SocketAddress arg0, SocketAddress arg1,
			IoSessionInitializer<? extends ConnectFuture> arg2);

	protected final void finishSessionInitialization0(final IoSession session, IoFuture future) {
		future.addListener(new IoFutureListener() {
			public void operationComplete(ConnectFuture future) {
				if (future.isCanceled()) {
					session.closeNow();
				}

			}
		});
	}

	public String toString() {
		TransportMetadata m = this.getTransportMetadata();
		return '(' + m.getProviderName() + ' ' + m.getName() + " connector: " + "managedSessionCount: "
				+ this.getManagedSessionCount() + ')';
	}
}