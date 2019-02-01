/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.polling;

import java.net.ConnectException;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.DefaultConnectFuture;
import org.apache.mina.core.service.AbstractIoConnector;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.core.service.AbstractIoService.ServiceOperationFuture;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.util.ExceptionMonitor;

public abstract class AbstractPollingIoConnector<T extends AbstractIoSession, H> extends AbstractIoConnector {
	private final Queue<AbstractPollingIoConnector<T, H>.ConnectionRequest> connectQueue;
	private final Queue<AbstractPollingIoConnector<T, H>.ConnectionRequest> cancelQueue;
	private final IoProcessor<T> processor;
	private final boolean createdProcessor;
	private final ServiceOperationFuture disposalFuture;
	private volatile boolean selectable;
	private final AtomicReference<AbstractPollingIoConnector<T, H>.Connector> connectorRef;

	protected AbstractPollingIoConnector(IoSessionConfig sessionConfig,
			Class<? extends IoProcessor<T>> processorClass) {
		this(sessionConfig, (Executor) null, new SimpleIoProcessorPool(processorClass), true);
	}

	protected AbstractPollingIoConnector(IoSessionConfig sessionConfig, Class<? extends IoProcessor<T>> processorClass,
			int processorCount) {
		this(sessionConfig, (Executor) null, new SimpleIoProcessorPool(processorClass, processorCount), true);
	}

	protected AbstractPollingIoConnector(IoSessionConfig sessionConfig, IoProcessor<T> processor) {
		this(sessionConfig, (Executor) null, processor, false);
	}

	protected AbstractPollingIoConnector(IoSessionConfig sessionConfig, Executor executor, IoProcessor<T> processor) {
		this(sessionConfig, executor, processor, false);
	}

	private AbstractPollingIoConnector(IoSessionConfig sessionConfig, Executor executor, IoProcessor<T> processor,
			boolean createdProcessor) {
		super(sessionConfig, executor);
		this.connectQueue = new ConcurrentLinkedQueue();
		this.cancelQueue = new ConcurrentLinkedQueue();
		this.disposalFuture = new ServiceOperationFuture();
		this.connectorRef = new AtomicReference();
		if (processor == null) {
			throw new IllegalArgumentException("processor");
		} else {
			this.processor = processor;
			this.createdProcessor = createdProcessor;

			try {
				this.init();
				this.selectable = true;
			} catch (RuntimeException arg13) {
				throw arg13;
			} catch (Exception arg14) {
				throw new RuntimeIoException("Failed to initialize.", arg14);
			} finally {
				if (!this.selectable) {
					try {
						this.destroy();
					} catch (Exception arg12) {
						ExceptionMonitor.getInstance().exceptionCaught(arg12);
					}
				}

			}

		}
	}

	protected abstract void init() throws Exception;

	protected abstract void destroy() throws Exception;

	protected abstract H newHandle(SocketAddress arg0) throws Exception;

	protected abstract boolean connect(H arg0, SocketAddress arg1) throws Exception;

	protected abstract boolean finishConnect(H arg0) throws Exception;

	protected abstract T newSession(IoProcessor<T> arg0, H arg1) throws Exception;

	protected abstract void close(H arg0) throws Exception;

	protected abstract void wakeup();

	protected abstract int select(int arg0) throws Exception;

	protected abstract Iterator<H> selectedHandles();

	protected abstract Iterator<H> allHandles();

	protected abstract void register(H arg0, AbstractPollingIoConnector<T, H>.ConnectionRequest arg1) throws Exception;

	protected abstract AbstractPollingIoConnector<T, H>.ConnectionRequest getConnectionRequest(H arg0);

	protected final void dispose0() throws Exception {
		this.startupWorker();
		this.wakeup();
	}

	protected final ConnectFuture connect0(SocketAddress remoteAddress, SocketAddress localAddress,
			IoSessionInitializer<? extends ConnectFuture> sessionInitializer) {
		Object handle = null;
		boolean success = false;

		try {
			handle = this.newHandle(localAddress);
			if (this.connect(handle, remoteAddress)) {
				DefaultConnectFuture request = new DefaultConnectFuture();
				AbstractIoSession session1 = this.newSession(this.processor, handle);
				this.initSession(session1, request, sessionInitializer);
				session1.getProcessor().add(session1);
				success = true;
				DefaultConnectFuture e = request;
				return e;
			}

			success = true;
		} catch (Exception arg18) {
			ConnectFuture session = DefaultConnectFuture.newFailedFuture(arg18);
			return session;
		} finally {
			if (!success && handle != null) {
				try {
					this.close(handle);
				} catch (Exception arg17) {
					ExceptionMonitor.getInstance().exceptionCaught(arg17);
				}
			}

		}

		AbstractPollingIoConnector.ConnectionRequest request1 = new AbstractPollingIoConnector.ConnectionRequest(handle,
				sessionInitializer);
		this.connectQueue.add(request1);
		this.startupWorker();
		this.wakeup();
		return request1;
	}

	private void startupWorker() {
		if (!this.selectable) {
			this.connectQueue.clear();
			this.cancelQueue.clear();
		}

		AbstractPollingIoConnector.Connector connector = (AbstractPollingIoConnector.Connector) this.connectorRef.get();
		if (connector == null) {
			connector = new AbstractPollingIoConnector.Connector();
			if (this.connectorRef.compareAndSet((Object) null, connector)) {
				this.executeWorker(connector);
			}
		}

	}

	private int registerNew() {
		int nHandles = 0;

		while (true) {
			AbstractPollingIoConnector.ConnectionRequest req = (AbstractPollingIoConnector.ConnectionRequest) this.connectQueue
					.poll();
			if (req == null) {
				return nHandles;
			}

			Object handle = req.handle;

			try {
				this.register(handle, req);
				++nHandles;
			} catch (Exception arg6) {
				req.setException(arg6);

				try {
					this.close(handle);
				} catch (Exception arg5) {
					ExceptionMonitor.getInstance().exceptionCaught(arg5);
				}
			}
		}
	}

	private int cancelKeys() {
		int nHandles = 0;

		while (true) {
			AbstractPollingIoConnector.ConnectionRequest req = (AbstractPollingIoConnector.ConnectionRequest) this.cancelQueue
					.poll();
			if (req == null) {
				if (nHandles > 0) {
					this.wakeup();
				}

				return nHandles;
			}

			Object handle = req.handle;

			try {
				this.close(handle);
			} catch (Exception arg7) {
				ExceptionMonitor.getInstance().exceptionCaught(arg7);
			} finally {
				++nHandles;
			}
		}
	}

	private int processConnections(Iterator<H> handlers) {
		int nHandles = 0;

		while (handlers.hasNext()) {
			Object handle = handlers.next();
			handlers.remove();
			AbstractPollingIoConnector.ConnectionRequest connectionRequest = this.getConnectionRequest(handle);
			if (connectionRequest != null) {
				boolean success = false;

				try {
					if (this.finishConnect(handle)) {
						AbstractIoSession e = this.newSession(this.processor, handle);
						this.initSession(e, connectionRequest, connectionRequest.getSessionInitializer());
						e.getProcessor().add(e);
						++nHandles;
					}

					success = true;
				} catch (Exception arg9) {
					connectionRequest.setException(arg9);
				} finally {
					if (!success) {
						this.cancelQueue.offer(connectionRequest);
					}

				}
			}
		}

		return nHandles;
	}

	private void processTimedOutSessions(Iterator<H> handles) {
		long currentTime = System.currentTimeMillis();

		while (handles.hasNext()) {
			Object handle = handles.next();
			AbstractPollingIoConnector.ConnectionRequest connectionRequest = this.getConnectionRequest(handle);
			if (connectionRequest != null && currentTime >= connectionRequest.deadline) {
				connectionRequest.setException(new ConnectException("Connection timed out."));
				this.cancelQueue.offer(connectionRequest);
			}
		}

	}

	public final class ConnectionRequest extends DefaultConnectFuture {
		private final H handle;
		private final long deadline;
		private final IoSessionInitializer<? extends ConnectFuture> sessionInitializer;

		public ConnectionRequest(H arg0, IoSessionInitializer<? extends ConnectFuture> handle) {
			this.handle = handle;
			long timeout = AbstractPollingIoConnector.this.getConnectTimeoutMillis();
			if (timeout <= 0L) {
				this.deadline = Long.MAX_VALUE;
			} else {
				this.deadline = System.currentTimeMillis() + timeout;
			}

			this.sessionInitializer = callback;
		}

		public H getHandle() {
			return this.handle;
		}

		public long getDeadline() {
			return this.deadline;
		}

		public IoSessionInitializer<? extends ConnectFuture> getSessionInitializer() {
			return this.sessionInitializer;
		}

		public boolean cancel() {
			if (!this.isDone()) {
				boolean justCancelled = super.cancel();
				if (justCancelled) {
					AbstractPollingIoConnector.this.cancelQueue.add(this);
					AbstractPollingIoConnector.this.startupWorker();
					AbstractPollingIoConnector.this.wakeup();
				}
			}

			return true;
		}
	}

	private class Connector implements Runnable {
		private Connector() {
		}

		public void run() {
			assert AbstractPollingIoConnector.this.connectorRef.get() == this;

			int nHandles = 0;

			while (AbstractPollingIoConnector.this.selectable) {
				try {
					int e = (int) Math.min(AbstractPollingIoConnector.this.getConnectTimeoutMillis(), 1000L);
					int e1 = AbstractPollingIoConnector.this.select(e);
					nHandles += AbstractPollingIoConnector.this.registerNew();
					if (nHandles == 0) {
						AbstractPollingIoConnector.this.connectorRef.set((Object) null);
						if (AbstractPollingIoConnector.this.connectQueue.isEmpty()) {
							assert AbstractPollingIoConnector.this.connectorRef.get() != this;
							break;
						}

						if (!AbstractPollingIoConnector.this.connectorRef.compareAndSet((Object) null, this)) {
							assert AbstractPollingIoConnector.this.connectorRef.get() != this;
							break;
						}

						assert AbstractPollingIoConnector.this.connectorRef.get() == this;
					}

					if (e1 > 0) {
						nHandles -= AbstractPollingIoConnector.this
								.processConnections(AbstractPollingIoConnector.this.selectedHandles());
					}

					AbstractPollingIoConnector.this
							.processTimedOutSessions(AbstractPollingIoConnector.this.allHandles());
					nHandles -= AbstractPollingIoConnector.this.cancelKeys();
				} catch (ClosedSelectorException arg48) {
					ExceptionMonitor.getInstance().exceptionCaught(arg48);
					break;
				} catch (Exception arg49) {
					ExceptionMonitor.getInstance().exceptionCaught(arg49);

					try {
						Thread.sleep(1000L);
					} catch (InterruptedException arg47) {
						ExceptionMonitor.getInstance().exceptionCaught(arg47);
					}
				}
			}

			if (AbstractPollingIoConnector.this.selectable && AbstractPollingIoConnector.this.isDisposing()) {
				AbstractPollingIoConnector.this.selectable = false;
				boolean arg39 = false;

				try {
					arg39 = true;
					if (AbstractPollingIoConnector.this.createdProcessor) {
						AbstractPollingIoConnector.this.processor.dispose();
						arg39 = false;
					} else {
						arg39 = false;
					}
				} finally {
					if (arg39) {
						try {
							synchronized (AbstractPollingIoConnector.this.disposalLock) {
								if (AbstractPollingIoConnector.this.isDisposing()) {
									AbstractPollingIoConnector.this.destroy();
								}
							}
						} catch (Exception arg41) {
							ExceptionMonitor.getInstance().exceptionCaught(arg41);
						} finally {
							AbstractPollingIoConnector.this.disposalFuture.setDone();
						}

					}
				}

				try {
					synchronized (AbstractPollingIoConnector.this.disposalLock) {
						if (AbstractPollingIoConnector.this.isDisposing()) {
							AbstractPollingIoConnector.this.destroy();
						}
					}
				} catch (Exception arg44) {
					ExceptionMonitor.getInstance().exceptionCaught(arg44);
				} finally {
					AbstractPollingIoConnector.this.disposalFuture.setDone();
				}
			}

		}
	}
}