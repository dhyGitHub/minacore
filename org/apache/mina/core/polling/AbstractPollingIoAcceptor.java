/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.polling;

import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.service.AbstractIoAcceptor;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.core.service.AbstractIoAcceptor.AcceptorOperationFuture;
import org.apache.mina.core.service.AbstractIoService.ServiceOperationFuture;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.util.ExceptionMonitor;

public abstract class AbstractPollingIoAcceptor<S extends AbstractIoSession, H> extends AbstractIoAcceptor {
	private final Semaphore lock;
	private final IoProcessor<S> processor;
	private final boolean createdProcessor;
	private final Queue<AcceptorOperationFuture> registerQueue;
	private final Queue<AcceptorOperationFuture> cancelQueue;
	private final Map<SocketAddress, H> boundHandles;
	private final ServiceOperationFuture disposalFuture;
	private volatile boolean selectable;
	private AtomicReference<AbstractPollingIoAcceptor<S, H>.Acceptor> acceptorRef;
	protected boolean reuseAddress;
	protected int backlog;

	protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Class<? extends IoProcessor<S>> processorClass) {
		this(sessionConfig, (Executor) null, new SimpleIoProcessorPool(processorClass), true, (SelectorProvider) null);
	}

	protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Class<? extends IoProcessor<S>> processorClass,
			int processorCount) {
		this(sessionConfig, (Executor) null, new SimpleIoProcessorPool(processorClass, processorCount), true,
				(SelectorProvider) null);
	}

	protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Class<? extends IoProcessor<S>> processorClass,
			int processorCount, SelectorProvider selectorProvider) {
		this(sessionConfig, (Executor) null,
				new SimpleIoProcessorPool(processorClass, processorCount, selectorProvider), true, selectorProvider);
	}

	protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, IoProcessor<S> processor) {
		this(sessionConfig, (Executor) null, processor, false, (SelectorProvider) null);
	}

	protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Executor executor, IoProcessor<S> processor) {
		this(sessionConfig, executor, processor, false, (SelectorProvider) null);
	}

	private AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Executor executor, IoProcessor<S> processor,
			boolean createdProcessor, SelectorProvider selectorProvider) {
		super(sessionConfig, executor);
		this.lock = new Semaphore(1);
		this.registerQueue = new ConcurrentLinkedQueue();
		this.cancelQueue = new ConcurrentLinkedQueue();
		this.boundHandles = Collections.synchronizedMap(new HashMap());
		this.disposalFuture = new ServiceOperationFuture();
		this.acceptorRef = new AtomicReference();
		this.reuseAddress = false;
		this.backlog = 50;
		if (processor == null) {
			throw new IllegalArgumentException("processor");
		} else {
			this.processor = processor;
			this.createdProcessor = createdProcessor;

			try {
				this.init(selectorProvider);
				this.selectable = true;
			} catch (RuntimeException arg14) {
				throw arg14;
			} catch (Exception arg15) {
				throw new RuntimeIoException("Failed to initialize.", arg15);
			} finally {
				if (!this.selectable) {
					try {
						this.destroy();
					} catch (Exception arg13) {
						ExceptionMonitor.getInstance().exceptionCaught(arg13);
					}
				}

			}

		}
	}

	protected abstract void init() throws Exception;

	protected abstract void init(SelectorProvider arg0) throws Exception;

	protected abstract void destroy() throws Exception;

	protected abstract int select() throws Exception;

	protected abstract void wakeup();

	protected abstract Iterator<H> selectedHandles();

	protected abstract H open(SocketAddress arg0) throws Exception;

	protected abstract SocketAddress localAddress(H arg0) throws Exception;

	protected abstract S accept(IoProcessor<S> arg0, H arg1) throws Exception;

	protected abstract void close(H arg0) throws Exception;

	protected void dispose0() throws Exception {
		this.unbind();
		this.startupAcceptor();
		this.wakeup();
	}

	protected final Set<SocketAddress> bindInternal(List<? extends SocketAddress> localAddresses) throws Exception {
		AcceptorOperationFuture request = new AcceptorOperationFuture(localAddresses);
		this.registerQueue.add(request);
		this.startupAcceptor();

		try {
			this.lock.acquire();
			this.wakeup();
		} finally {
			this.lock.release();
		}

		request.awaitUninterruptibly();
		if (request.getException() != null) {
			throw request.getException();
		} else {
			HashSet newLocalAddresses = new HashSet();
			Iterator i$ = this.boundHandles.values().iterator();

			while (i$.hasNext()) {
				Object handle = i$.next();
				newLocalAddresses.add(this.localAddress(handle));
			}

			return newLocalAddresses;
		}
	}

	private void startupAcceptor() throws InterruptedException {
		if (!this.selectable) {
			this.registerQueue.clear();
			this.cancelQueue.clear();
		}

		AbstractPollingIoAcceptor.Acceptor acceptor = (AbstractPollingIoAcceptor.Acceptor) this.acceptorRef.get();
		if (acceptor == null) {
			this.lock.acquire();
			acceptor = new AbstractPollingIoAcceptor.Acceptor();
			if (this.acceptorRef.compareAndSet((Object) null, acceptor)) {
				this.executeWorker(acceptor);
			} else {
				this.lock.release();
			}
		}

	}

	protected final void unbind0(List<? extends SocketAddress> localAddresses) throws Exception {
		AcceptorOperationFuture future = new AcceptorOperationFuture(localAddresses);
		this.cancelQueue.add(future);
		this.startupAcceptor();
		this.wakeup();
		future.awaitUninterruptibly();
		if (future.getException() != null) {
			throw future.getException();
		}
	}

	private int registerHandles() {
		while (true) {
			AcceptorOperationFuture future = (AcceptorOperationFuture) this.registerQueue.poll();
			if (future == null) {
				return 0;
			}

			ConcurrentHashMap newHandles = new ConcurrentHashMap();
			List localAddresses = future.getLocalAddresses();
			boolean arg16 = false;

			Object e;
			int i$2;
			label186: {
				Iterator i$;
				try {
					arg16 = true;
					i$ = localAddresses.iterator();

					while (i$.hasNext()) {
						SocketAddress handle = (SocketAddress) i$.next();
						e = this.open(handle);
						newHandles.put(this.localAddress(e), e);
					}

					this.boundHandles.putAll(newHandles);
					future.setDone();
					i$2 = newHandles.size();
					arg16 = false;
					break label186;
				} catch (Exception arg20) {
					future.setException(arg20);
					arg16 = false;
				} finally {
					if (arg16) {
						if (future.getException() != null) {
							Iterator i$1 = newHandles.values().iterator();

							while (i$1.hasNext()) {
								Object handle1 = i$1.next();

								try {
									this.close(handle1);
								} catch (Exception arg17) {
									ExceptionMonitor.getInstance().exceptionCaught(arg17);
								}
							}

							this.wakeup();
						}

					}
				}

				if (future.getException() == null) {
					continue;
				}

				i$ = newHandles.values().iterator();

				while (i$.hasNext()) {
					Object handle2 = i$.next();

					try {
						this.close(handle2);
					} catch (Exception arg18) {
						ExceptionMonitor.getInstance().exceptionCaught(arg18);
					}
				}

				this.wakeup();
				continue;
			}

			if (future.getException() != null) {
				Iterator handle3 = newHandles.values().iterator();

				while (handle3.hasNext()) {
					e = handle3.next();

					try {
						this.close(e);
					} catch (Exception arg19) {
						ExceptionMonitor.getInstance().exceptionCaught(arg19);
					}
				}

				this.wakeup();
			}

			return i$2;
		}
	}

	private int unregisterHandles() {
		int cancelledHandles = 0;

		while (true) {
			AcceptorOperationFuture future = (AcceptorOperationFuture) this.cancelQueue.poll();
			if (future == null) {
				return cancelledHandles;
			}

			Iterator i$ = future.getLocalAddresses().iterator();

			while (i$.hasNext()) {
				SocketAddress a = (SocketAddress) i$.next();
				Object handle = this.boundHandles.remove(a);
				if (handle != null) {
					try {
						this.close(handle);
						this.wakeup();
					} catch (Exception arg9) {
						ExceptionMonitor.getInstance().exceptionCaught(arg9);
					} finally {
						++cancelledHandles;
					}
				}
			}

			future.setDone();
		}
	}

	public final IoSession newSession(SocketAddress remoteAddress, SocketAddress localAddress) {
		throw new UnsupportedOperationException();
	}

	public int getBacklog() {
		return this.backlog;
	}

	public void setBacklog(int backlog) {
		Object arg1 = this.bindLock;
		synchronized (this.bindLock) {
			if (this.isActive()) {
				throw new IllegalStateException("backlog can\'t be set while the acceptor is bound.");
			} else {
				this.backlog = backlog;
			}
		}
	}

	public boolean isReuseAddress() {
		return this.reuseAddress;
	}

	public void setReuseAddress(boolean reuseAddress) {
		Object arg1 = this.bindLock;
		synchronized (this.bindLock) {
			if (this.isActive()) {
				throw new IllegalStateException("backlog can\'t be set while the acceptor is bound.");
			} else {
				this.reuseAddress = reuseAddress;
			}
		}
	}

	public SocketSessionConfig getSessionConfig() {
		return (SocketSessionConfig) this.sessionConfig;
	}

	private class Acceptor implements Runnable {
		private Acceptor() {
		}

		public void run() {
			assert AbstractPollingIoAcceptor.this.acceptorRef.get() == this;

			int nHandles = 0;
			AbstractPollingIoAcceptor.this.lock.release();

			while (AbstractPollingIoAcceptor.this.selectable) {
				try {
					nHandles += AbstractPollingIoAcceptor.this.registerHandles();
					int e = AbstractPollingIoAcceptor.this.select();
					if (nHandles == 0) {
						AbstractPollingIoAcceptor.this.acceptorRef.set((Object) null);
						if (AbstractPollingIoAcceptor.this.registerQueue.isEmpty()
								&& AbstractPollingIoAcceptor.this.cancelQueue.isEmpty()) {
							assert AbstractPollingIoAcceptor.this.acceptorRef.get() != this;
							break;
						}

						if (!AbstractPollingIoAcceptor.this.acceptorRef.compareAndSet((Object) null, this)) {
							assert AbstractPollingIoAcceptor.this.acceptorRef.get() != this;
							break;
						}

						assert AbstractPollingIoAcceptor.this.acceptorRef.get() == this;
					}

					if (e > 0) {
						this.processHandles(AbstractPollingIoAcceptor.this.selectedHandles());
					}

					nHandles -= AbstractPollingIoAcceptor.this.unregisterHandles();
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

			if (AbstractPollingIoAcceptor.this.selectable && AbstractPollingIoAcceptor.this.isDisposing()) {
				AbstractPollingIoAcceptor.this.selectable = false;
				boolean arg39 = false;

				try {
					arg39 = true;
					if (AbstractPollingIoAcceptor.this.createdProcessor) {
						AbstractPollingIoAcceptor.this.processor.dispose();
						arg39 = false;
					} else {
						arg39 = false;
					}
				} finally {
					if (arg39) {
						try {
							synchronized (AbstractPollingIoAcceptor.this.disposalLock) {
								if (AbstractPollingIoAcceptor.this.isDisposing()) {
									AbstractPollingIoAcceptor.this.destroy();
								}
							}
						} catch (Exception arg41) {
							ExceptionMonitor.getInstance().exceptionCaught(arg41);
						} finally {
							AbstractPollingIoAcceptor.this.disposalFuture.setDone();
						}

					}
				}

				try {
					synchronized (AbstractPollingIoAcceptor.this.disposalLock) {
						if (AbstractPollingIoAcceptor.this.isDisposing()) {
							AbstractPollingIoAcceptor.this.destroy();
						}
					}
				} catch (Exception arg44) {
					ExceptionMonitor.getInstance().exceptionCaught(arg44);
				} finally {
					AbstractPollingIoAcceptor.this.disposalFuture.setDone();
				}
			}

		}

		private void processHandles(Iterator<H> handles) throws Exception {
			while (handles.hasNext()) {
				Object handle = handles.next();
				handles.remove();
				AbstractIoSession session = AbstractPollingIoAcceptor.this
						.accept(AbstractPollingIoAcceptor.this.processor, handle);
				if (session != null) {
					AbstractPollingIoAcceptor.this.initSession(session, (IoFuture) null, (IoSessionInitializer) null);
					session.getProcessor().add(session);
				}
			}

		}
	}
}