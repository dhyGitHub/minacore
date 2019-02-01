/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.filterchain.DefaultIoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.AbstractIoAcceptor;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.AbstractIoSessionConfig;
import org.apache.mina.core.session.DefaultIoSessionDataStructureFactory;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;

public class DummySession extends AbstractIoSession {
	private static final TransportMetadata TRANSPORT_METADATA = new DefaultTransportMetadata("mina", "dummy", false,
			false, SocketAddress.class, IoSessionConfig.class, new Class[] { Object.class });
	private static final SocketAddress ANONYMOUS_ADDRESS = new SocketAddress() {
		private static final long serialVersionUID = -496112902353454179L;

		public String toString() {
			return "?";
		}
	};
	private volatile IoService service;
	private volatile IoSessionConfig config = new AbstractIoSessionConfig() {
	};
	private final IoFilterChain filterChain = new DefaultIoFilterChain(this);
	private final IoProcessor<IoSession> processor;
	private volatile IoHandler handler = new IoHandlerAdapter();
	private volatile SocketAddress localAddress;
	private volatile SocketAddress remoteAddress;
	private volatile TransportMetadata transportMetadata;

	public DummySession() {
		super(new AbstractIoAcceptor(new AbstractIoSessionConfig() {
		}, new Executor() {
			public void execute(Runnable command) {
			}
		}) {
			protected Set<SocketAddress> bindInternal(List<? extends SocketAddress> localAddresses) throws Exception {
				throw new UnsupportedOperationException();
			}

			protected void unbind0(List<? extends SocketAddress> localAddresses) throws Exception {
				throw new UnsupportedOperationException();
			}

			public IoSession newSession(SocketAddress remoteAddress, SocketAddress localAddress) {
				throw new UnsupportedOperationException();
			}

			public TransportMetadata getTransportMetadata() {
				return DummySession.TRANSPORT_METADATA;
			}

			protected void dispose0() throws Exception {
			}

			public IoSessionConfig getSessionConfig() {
				return this.sessionConfig;
			}
		});
		this.localAddress = ANONYMOUS_ADDRESS;
		this.remoteAddress = ANONYMOUS_ADDRESS;
		this.transportMetadata = TRANSPORT_METADATA;
		this.processor = new IoProcessor() {
			public void add(IoSession session) {
			}

			public void flush(IoSession session) {
				DummySession s = (DummySession) session;
				WriteRequest req = s.getWriteRequestQueue().poll(session);
				if (req != null) {
					Object m = req.getMessage();
					if (m instanceof FileRegion) {
						FileRegion file = (FileRegion) m;

						try {
							file.getFileChannel().position(file.getPosition() + file.getRemainingBytes());
							file.update(file.getRemainingBytes());
						} catch (IOException arg6) {
							s.getFilterChain().fireExceptionCaught(arg6);
						}
					}

					DummySession.this.getFilterChain().fireMessageSent(req);
				}

			}

			public void write(IoSession session, WriteRequest writeRequest) {
				WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
				writeRequestQueue.offer(session, writeRequest);
				if (!session.isWriteSuspended()) {
					this.flush(session);
				}

			}

			public void remove(IoSession session) {
				if (!session.getCloseFuture().isClosed()) {
					session.getFilterChain().fireSessionClosed();
				}

			}

			public void updateTrafficControl(IoSession session) {
			}

			public void dispose() {
			}

			public boolean isDisposed() {
				return false;
			}

			public boolean isDisposing() {
				return false;
			}
		};
		this.service = super.getService();

		try {
			DefaultIoSessionDataStructureFactory e = new DefaultIoSessionDataStructureFactory();
			this.setAttributeMap(e.getAttributeMap(this));
			this.setWriteRequestQueue(e.getWriteRequestQueue(this));
		} catch (Exception arg1) {
			throw new InternalError();
		}
	}

	public IoSessionConfig getConfig() {
		return this.config;
	}

	public void setConfig(IoSessionConfig config) {
		if (config == null) {
			throw new IllegalArgumentException("config");
		} else {
			this.config = config;
		}
	}

	public IoFilterChain getFilterChain() {
		return this.filterChain;
	}

	public IoHandler getHandler() {
		return this.handler;
	}

	public void setHandler(IoHandler handler) {
		if (handler == null) {
			throw new IllegalArgumentException("handler");
		} else {
			this.handler = handler;
		}
	}

	public SocketAddress getLocalAddress() {
		return this.localAddress;
	}

	public SocketAddress getRemoteAddress() {
		return this.remoteAddress;
	}

	public void setLocalAddress(SocketAddress localAddress) {
		if (localAddress == null) {
			throw new IllegalArgumentException("localAddress");
		} else {
			this.localAddress = localAddress;
		}
	}

	public void setRemoteAddress(SocketAddress remoteAddress) {
		if (remoteAddress == null) {
			throw new IllegalArgumentException("remoteAddress");
		} else {
			this.remoteAddress = remoteAddress;
		}
	}

	public IoService getService() {
		return this.service;
	}

	public void setService(IoService service) {
		if (service == null) {
			throw new IllegalArgumentException("service");
		} else {
			this.service = service;
		}
	}

	public final IoProcessor<IoSession> getProcessor() {
		return this.processor;
	}

	public TransportMetadata getTransportMetadata() {
		return this.transportMetadata;
	}

	public void setTransportMetadata(TransportMetadata transportMetadata) {
		if (transportMetadata == null) {
			throw new IllegalArgumentException("transportMetadata");
		} else {
			this.transportMetadata = transportMetadata;
		}
	}

	public void setScheduledWriteBytes(int byteCount) {
		super.setScheduledWriteBytes(byteCount);
	}

	public void setScheduledWriteMessages(int messages) {
		super.setScheduledWriteMessages(messages);
	}

	public void updateThroughput(boolean force) {
		super.updateThroughput(System.currentTimeMillis(), force);
	}
}