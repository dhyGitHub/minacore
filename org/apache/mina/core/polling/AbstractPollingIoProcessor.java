/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.polling;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.nio.channels.ClosedSelectorException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoServiceListenerSupport;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.SessionState;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.mina.transport.socket.AbstractDatagramSessionConfig;
import org.apache.mina.util.ExceptionMonitor;
import org.apache.mina.util.NamePreservingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPollingIoProcessor<S extends AbstractIoSession> implements IoProcessor<S> {
	private static final Logger LOG = LoggerFactory.getLogger(IoProcessor.class);
	private static final long SELECT_TIMEOUT = 1000L;
	private static final ConcurrentHashMap<Class<?>, AtomicInteger> threadIds = new ConcurrentHashMap();
	private final String threadName;
	private final Executor executor;
	private final Queue<S> newSessions = new ConcurrentLinkedQueue();
	private final Queue<S> removingSessions = new ConcurrentLinkedQueue();
	private final Queue<S> flushingSessions = new ConcurrentLinkedQueue();
	private final Queue<S> trafficControllingSessions = new ConcurrentLinkedQueue();
	private final AtomicReference<AbstractPollingIoProcessor<S>.Processor> processorRef = new AtomicReference();
	private long lastIdleCheckTime;
	private final Object disposalLock = new Object();
	private volatile boolean disposing;
	private volatile boolean disposed;
	private final DefaultIoFuture disposalFuture = new DefaultIoFuture((IoSession) null);
	protected AtomicBoolean wakeupCalled = new AtomicBoolean(false);

	protected AbstractPollingIoProcessor(Executor executor) {
		if (executor == null) {
			throw new IllegalArgumentException("executor");
		} else {
			this.threadName = this.nextThreadName();
			this.executor = executor;
		}
	}

	private String nextThreadName() {
		Class cls = this.getClass();
		AtomicInteger threadId = (AtomicInteger) threadIds.putIfAbsent(cls, new AtomicInteger(1));
		int newThreadId;
		if (threadId == null) {
			newThreadId = 1;
		} else {
			newThreadId = threadId.incrementAndGet();
		}

		return cls.getSimpleName() + '-' + newThreadId;
	}

	public final boolean isDisposing() {
		return this.disposing;
	}

	public final boolean isDisposed() {
		return this.disposed;
	}

	public final void dispose() {
		if (!this.disposed && !this.disposing) {
			Object arg0 = this.disposalLock;
			synchronized (this.disposalLock) {
				this.disposing = true;
				this.startupProcessor();
			}

			this.disposalFuture.awaitUninterruptibly();
			this.disposed = true;
		}
	}

	protected abstract void doDispose() throws Exception;

	protected abstract int select(long arg0) throws Exception;

	protected abstract int select() throws Exception;

	protected abstract boolean isSelectorEmpty();

	protected abstract void wakeup();

	protected abstract Iterator<S> allSessions();

	protected abstract Iterator<S> selectedSessions();

	protected abstract SessionState getState(S arg0);

	protected abstract boolean isWritable(S arg0);

	protected abstract boolean isReadable(S arg0);

	protected abstract void setInterestedInWrite(S arg0, boolean arg1) throws Exception;

	protected abstract void setInterestedInRead(S arg0, boolean arg1) throws Exception;

	protected abstract boolean isInterestedInRead(S arg0);

	protected abstract boolean isInterestedInWrite(S arg0);

	protected abstract void init(S arg0) throws Exception;

	protected abstract void destroy(S arg0) throws Exception;

	protected abstract int read(S arg0, IoBuffer arg1) throws Exception;

	protected abstract int write(S arg0, IoBuffer arg1, int arg2) throws IOException;

	protected abstract int transferFile(S arg0, FileRegion arg1, int arg2) throws Exception;

	public final void add(S session) {
		if (!this.disposed && !this.disposing) {
			this.newSessions.add(session);
			this.startupProcessor();
		} else {
			throw new IllegalStateException("Already disposed.");
		}
	}

	public final void remove(S session) {
		this.scheduleRemove(session);
		this.startupProcessor();
	}

	private void scheduleRemove(S session) {
		if (!this.removingSessions.contains(session)) {
			this.removingSessions.add(session);
		}

	}

	public void write(S session, WriteRequest writeRequest) {
		WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
		writeRequestQueue.offer(session, writeRequest);
		if (!session.isWriteSuspended()) {
			this.flush(session);
		}

	}

	public final void flush(S session) {
		if (session.setScheduledForFlush(true)) {
			this.flushingSessions.add(session);
			this.wakeup();
		}

	}

	private void scheduleFlush(S session) {
		if (session.setScheduledForFlush(true)) {
			this.flushingSessions.add(session);
		}

	}

	public final void updateTrafficMask(S session) {
		this.trafficControllingSessions.add(session);
		this.wakeup();
	}

	private void startupProcessor() {
		AbstractPollingIoProcessor.Processor processor = (AbstractPollingIoProcessor.Processor) this.processorRef.get();
		if (processor == null) {
			processor = new AbstractPollingIoProcessor.Processor((AbstractPollingIoProcessor.Processor) null);
			if (this.processorRef.compareAndSet((Object) null, processor)) {
				this.executor.execute(new NamePreservingRunnable(processor, this.threadName));
			}
		}

		this.wakeup();
	}

	protected abstract void registerNewSelector() throws IOException;

	protected abstract boolean isBrokenConnection() throws IOException;

	private int handleNewSessions() {
		int addedSessions = 0;

		for (AbstractIoSession session = (AbstractIoSession) this.newSessions
				.poll(); session != null; session = (AbstractIoSession) this.newSessions.poll()) {
			if (this.addNow(session)) {
				++addedSessions;
			}
		}

		return addedSessions;
	}

	private boolean addNow(S session) {
		boolean registered = false;

		try {
			this.init(session);
			registered = true;
			IoFilterChainBuilder e = session.getService().getFilterChainBuilder();
			e.buildFilterChain(session.getFilterChain());
			IoServiceListenerSupport e1 = ((AbstractIoService) session.getService()).getListeners();
			e1.fireSessionCreated(session);
		} catch (Exception arg10) {
			ExceptionMonitor.getInstance().exceptionCaught(arg10);

			try {
				this.destroy(session);
			} catch (Exception arg8) {
				ExceptionMonitor.getInstance().exceptionCaught(arg8);
			} finally {
				registered = false;
			}
		}

		return registered;
	}

	private int removeSessions() {
		int removedSessions = 0;

		for (AbstractIoSession session = (AbstractIoSession) this.removingSessions
				.poll(); session != null; session = (AbstractIoSession) this.removingSessions.poll()) {
			SessionState state = this.getState(session);
			switch ($SWITCH_TABLE$org$apache$mina$core$session$SessionState()[state.ordinal()]) {
			case 1:
				this.newSessions.remove(session);
				if (this.removeNow(session)) {
					++removedSessions;
				}
				break;
			case 2:
				if (this.removeNow(session)) {
					++removedSessions;
				}
				break;
			case 3:
				++removedSessions;
				break;
			default:
				throw new IllegalStateException(String.valueOf(state));
			}
		}

		return removedSessions;
	}

	private boolean removeNow(S session) {
		this.clearWriteRequestQueue(session);

		try {
			this.destroy(session);
			return true;
		} catch (Exception arg12) {
			IoFilterChain filterChain = session.getFilterChain();
			filterChain.fireExceptionCaught(arg12);
		} finally {
			try {
				this.clearWriteRequestQueue(session);
				((AbstractIoService) session.getService()).getListeners().fireSessionDestroyed(session);
			} catch (Exception arg11) {
				IoFilterChain filterChain1 = session.getFilterChain();
				filterChain1.fireExceptionCaught(arg11);
			}

		}

		return false;
	}

	private void clearWriteRequestQueue(S session) {
		WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
		ArrayList failedRequests = new ArrayList();
		WriteRequest req;
		if ((req = writeRequestQueue.poll(session)) != null) {
			Object cause = req.getMessage();
			if (cause instanceof IoBuffer) {
				IoBuffer filterChain = (IoBuffer) cause;
				if (filterChain.hasRemaining()) {
					filterChain.reset();
					failedRequests.add(req);
				} else {
					IoFilterChain filterChain1 = session.getFilterChain();
					filterChain1.fireMessageSent(req);
				}
			} else {
				failedRequests.add(req);
			}

			while ((req = writeRequestQueue.poll(session)) != null) {
				failedRequests.add(req);
			}
		}

		if (!failedRequests.isEmpty()) {
			WriteToClosedSessionException cause1 = new WriteToClosedSessionException(failedRequests);
			Iterator filterChain4 = failedRequests.iterator();

			while (filterChain4.hasNext()) {
				WriteRequest filterChain2 = (WriteRequest) filterChain4.next();
				session.decreaseScheduledBytesAndMessages(filterChain2);
				filterChain2.getFuture().setException(cause1);
			}

			IoFilterChain filterChain3 = session.getFilterChain();
			filterChain3.fireExceptionCaught(cause1);
		}

	}

	private void process() throws Exception {
		Iterator i = this.selectedSessions();

		while (i.hasNext()) {
			AbstractIoSession session = (AbstractIoSession) i.next();
			this.process(session);
			i.remove();
		}

	}

	private void process(S session) {
		if (this.isReadable(session) && !session.isReadSuspended()) {
			this.read(session);
		}

		if (this.isWritable(session) && !session.isWriteSuspended() && session.setScheduledForFlush(true)) {
			this.flushingSessions.add(session);
		}

	}

	private void read(S session) {
		IoSessionConfig config = session.getConfig();
		int bufferSize = config.getReadBufferSize();
		IoBuffer buf = IoBuffer.allocate(bufferSize);
		boolean hasFragmentation = session.getTransportMetadata().hasFragmentation();

		try {
			int e = 0;

			int filterChain2;
			try {
				if (hasFragmentation) {
					while ((filterChain2 = this.read(session, buf)) > 0) {
						e += filterChain2;
						if (!buf.hasRemaining()) {
							break;
						}
					}
				} else {
					filterChain2 = this.read(session, buf);
					if (filterChain2 > 0) {
						e = filterChain2;
					}
				}
			} finally {
				buf.flip();
			}

			IoFilterChain filterChain1;
			if (e > 0) {
				filterChain1 = session.getFilterChain();
				filterChain1.fireMessageReceived(buf);
				buf = null;
				if (hasFragmentation) {
					if (e << 1 < config.getReadBufferSize()) {
						session.decreaseReadBufferSize();
					} else if (e == config.getReadBufferSize()) {
						session.increaseReadBufferSize();
					}
				}
			}

			if (filterChain2 < 0) {
				filterChain1 = session.getFilterChain();
				filterChain1.fireInputClosed();
			}
		} catch (Exception arg11) {
			if (arg11 instanceof IOException && (!(arg11 instanceof PortUnreachableException)
					|| !AbstractDatagramSessionConfig.class.isAssignableFrom(config.getClass())
					|| ((AbstractDatagramSessionConfig) config).isCloseOnPortUnreachable())) {
				this.scheduleRemove(session);
			}

			IoFilterChain filterChain = session.getFilterChain();
			filterChain.fireExceptionCaught(arg11);
		}

	}

	private void notifyIdleSessions(long currentTime) throws Exception {
		if (currentTime - this.lastIdleCheckTime >= 1000L) {
			this.lastIdleCheckTime = currentTime;
			AbstractIoSession.notifyIdleness(this.allSessions(), currentTime);
		}

	}

	private void flush(long currentTime) {
		if (!this.flushingSessions.isEmpty()) {
			do {
				AbstractIoSession session = (AbstractIoSession) this.flushingSessions.poll();
				if (session == null) {
					break;
				}

				session.unscheduledForFlush();
				SessionState state = this.getState(session);
				switch ($SWITCH_TABLE$org$apache$mina$core$session$SessionState()[state.ordinal()]) {
				case 1:
					this.scheduleFlush(session);
					return;
				case 2:
					try {
						boolean e = this.flushNow(session, currentTime);
						if (e && !session.getWriteRequestQueue().isEmpty(session) && !session.isScheduledForFlush()) {
							this.scheduleFlush(session);
						}
					} catch (Exception arg6) {
						this.scheduleRemove(session);
						session.closeNow();
						IoFilterChain filterChain = session.getFilterChain();
						filterChain.fireExceptionCaught(arg6);
					}
				case 3:
					break;
				default:
					throw new IllegalStateException(String.valueOf(state));
				}
			} while (!this.flushingSessions.isEmpty());

		}
	}

	private boolean flushNow(S session, long currentTime) {
		if (!session.isConnected()) {
			this.scheduleRemove(session);
			return false;
		} else {
			boolean hasFragmentation = session.getTransportMetadata().hasFragmentation();
			WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
			int maxWrittenBytes = session.getConfig().getMaxReadBufferSize()
					+ (session.getConfig().getMaxReadBufferSize() >>> 1);
			int writtenBytes = 0;
			WriteRequest req = null;

			try {
				this.setInterestedInWrite(session, false);

				do {
					req = session.getCurrentWriteRequest();
					if (req == null) {
						req = writeRequestQueue.poll(session);
						if (req == null) {
							break;
						}

						session.setCurrentWriteRequest(req);
					}

					Object filterChain1 = req.getMessage();
					int e;
					int arg9999;
					if (filterChain1 instanceof IoBuffer) {
						e = this.writeBuffer(session, req, hasFragmentation, maxWrittenBytes - writtenBytes,
								currentTime);
						if (e > 0 && ((IoBuffer) filterChain1).hasRemaining()) {
							arg9999 = writtenBytes + e;
							this.setInterestedInWrite(session, true);
							return false;
						}
					} else {
						if (!(filterChain1 instanceof FileRegion)) {
							throw new IllegalStateException("Don\'t know how to handle message of type \'"
									+ filterChain1.getClass().getName() + "\'.  Are you missing a protocol encoder?");
						}

						e = this.writeFile(session, req, hasFragmentation, maxWrittenBytes - writtenBytes, currentTime);
						if (e > 0 && ((FileRegion) filterChain1).getRemainingBytes() > 0L) {
							arg9999 = writtenBytes + e;
							this.setInterestedInWrite(session, true);
							return false;
						}
					}

					if (e == 0) {
						if (!req.equals(AbstractIoSession.MESSAGE_SENT_REQUEST)) {
							this.setInterestedInWrite(session, true);
							return false;
						}
					} else {
						writtenBytes += e;
						if (writtenBytes >= maxWrittenBytes) {
							this.scheduleFlush(session);
							return false;
						}
					}

					if (filterChain1 instanceof IoBuffer) {
						((IoBuffer) filterChain1).free();
					}
				} while (writtenBytes < maxWrittenBytes);

				return true;
			} catch (Exception arg10) {
				if (req != null) {
					req.getFuture().setException(arg10);
				}

				IoFilterChain filterChain = session.getFilterChain();
				filterChain.fireExceptionCaught(arg10);
				return false;
			}
		}
	}

	private int writeBuffer(S session, WriteRequest req, boolean hasFragmentation, int maxLength, long currentTime)
			throws Exception {
		IoBuffer buf = (IoBuffer) req.getMessage();
		int localWrittenBytes = 0;
		if (buf.hasRemaining()) {
			int originalMessage;
			if (hasFragmentation) {
				originalMessage = Math.min(buf.remaining(), maxLength);
			} else {
				originalMessage = buf.remaining();
			}

			try {
				localWrittenBytes = this.write(session, buf, originalMessage);
			} catch (IOException arg10) {
				buf.free();
				session.closeNow();
				this.removeNow(session);
				return 0;
			}
		}

		session.increaseWrittenBytes(localWrittenBytes, currentTime);
		if (!buf.hasRemaining() || !hasFragmentation && localWrittenBytes != 0) {
			Object originalMessage1 = req.getOriginalRequest().getMessage();
			if (originalMessage1 instanceof IoBuffer) {
				buf = (IoBuffer) req.getOriginalRequest().getMessage();
				int pos = buf.position();
				buf.reset();
				this.fireMessageSent(session, req);
				buf.position(pos);
			} else {
				this.fireMessageSent(session, req);
			}
		}

		return localWrittenBytes;
	}

	private int writeFile(S session, WriteRequest req, boolean hasFragmentation, int maxLength, long currentTime)
			throws Exception {
		FileRegion region = (FileRegion) req.getMessage();
		int localWrittenBytes;
		if (region.getRemainingBytes() > 0L) {
			int length;
			if (hasFragmentation) {
				length = (int) Math.min(region.getRemainingBytes(), (long) maxLength);
			} else {
				length = (int) Math.min(2147483647L, region.getRemainingBytes());
			}

			localWrittenBytes = this.transferFile(session, region, length);
			region.update((long) localWrittenBytes);
		} else {
			localWrittenBytes = 0;
		}

		session.increaseWrittenBytes(localWrittenBytes, currentTime);
		if (region.getRemainingBytes() <= 0L || !hasFragmentation && localWrittenBytes != 0) {
			this.fireMessageSent(session, req);
		}

		return localWrittenBytes;
	}

	private void fireMessageSent(S session, WriteRequest req) {
		session.setCurrentWriteRequest((WriteRequest) null);
		IoFilterChain filterChain = session.getFilterChain();
		filterChain.fireMessageSent(req);
	}

	private void updateTrafficMask() {
		for (int queueSize = this.trafficControllingSessions.size(); queueSize > 0; --queueSize) {
			AbstractIoSession session = (AbstractIoSession) this.trafficControllingSessions.poll();
			if (session == null) {
				return;
			}

			SessionState state = this.getState(session);
			switch ($SWITCH_TABLE$org$apache$mina$core$session$SessionState()[state.ordinal()]) {
			case 1:
				this.trafficControllingSessions.add(session);
				break;
			case 2:
				this.updateTrafficControl(session);
			case 3:
				break;
			default:
				throw new IllegalStateException(String.valueOf(state));
			}
		}

	}

	public void updateTrafficControl(S session) {
		IoFilterChain filterChain;
		try {
			this.setInterestedInRead(session, !session.isReadSuspended());
		} catch (Exception arg3) {
			filterChain = session.getFilterChain();
			filterChain.fireExceptionCaught(arg3);
		}

		try {
			this.setInterestedInWrite(session,
					!session.getWriteRequestQueue().isEmpty(session) && !session.isWriteSuspended());
		} catch (Exception arg4) {
			filterChain = session.getFilterChain();
			filterChain.fireExceptionCaught(arg4);
		}

	}

	private class Processor implements Runnable {
		private Processor() {
		}

		public void run() {
			assert AbstractPollingIoProcessor.this.processorRef.get() == this;

			int nSessions = 0;
			AbstractPollingIoProcessor.this.lastIdleCheckTime = System.currentTimeMillis();
			int nbTries = 10;

			while (true) {
				try {
					long e = System.currentTimeMillis();
					int selected = AbstractPollingIoProcessor.this.select(1000L);
					long t1 = System.currentTimeMillis();
					long delta = t1 - e;
					if (!AbstractPollingIoProcessor.this.wakeupCalled.getAndSet(false) && selected == 0
							&& delta < 100L) {
						if (AbstractPollingIoProcessor.this.isBrokenConnection()) {
							AbstractPollingIoProcessor.LOG.warn("Broken connection");
						} else if (nbTries == 0) {
							AbstractPollingIoProcessor.LOG
									.warn("Create a new selector. Selected is 0, delta = " + delta);
							AbstractPollingIoProcessor.this.registerNewSelector();
							nbTries = 10;
						} else {
							--nbTries;
						}
					} else {
						nbTries = 10;
					}

					nSessions += AbstractPollingIoProcessor.this.handleNewSessions();
					AbstractPollingIoProcessor.this.updateTrafficMask();
					if (selected > 0) {
						AbstractPollingIoProcessor.this.process();
					}

					long currentTime = System.currentTimeMillis();
					AbstractPollingIoProcessor.this.flush(currentTime);
					nSessions -= AbstractPollingIoProcessor.this.removeSessions();
					AbstractPollingIoProcessor.this.notifyIdleSessions(currentTime);
					if (nSessions == 0) {
						AbstractPollingIoProcessor.this.processorRef.set((Object) null);
						if (AbstractPollingIoProcessor.this.newSessions.isEmpty()
								&& AbstractPollingIoProcessor.this.isSelectorEmpty()) {
							assert AbstractPollingIoProcessor.this.processorRef.get() != this;
							break;
						}

						assert AbstractPollingIoProcessor.this.processorRef.get() != this;

						if (!AbstractPollingIoProcessor.this.processorRef.compareAndSet((Object) null, this)) {
							assert AbstractPollingIoProcessor.this.processorRef.get() != this;
							break;
						}

						assert AbstractPollingIoProcessor.this.processorRef.get() == this;
					}

					if (AbstractPollingIoProcessor.this.isDisposing()) {
						boolean hasKeys = false;
						Iterator i = AbstractPollingIoProcessor.this.allSessions();

						while (i.hasNext()) {
							IoSession session = (IoSession) i.next();
							if (session.isActive()) {
								AbstractPollingIoProcessor.this.scheduleRemove((AbstractIoSession) session);
								hasKeys = true;
							}
						}

						if (hasKeys) {
							AbstractPollingIoProcessor.this.wakeup();
						}
					}
				} catch (ClosedSelectorException arg24) {
					ExceptionMonitor.getInstance().exceptionCaught(arg24);
					break;
				} catch (Throwable arg25) {
					ExceptionMonitor.getInstance().exceptionCaught(arg25);

					try {
						Thread.sleep(1000L);
					} catch (InterruptedException arg23) {
						ExceptionMonitor.getInstance().exceptionCaught(arg23);
					}
				}
			}

			if (!AbstractPollingIoProcessor.this.newSessions.isEmpty()) {
				AbstractPollingIoProcessor.LOG
						.info(AbstractPollingIoProcessor.this.threadName + " unknown reason end, newSessions size:"
								+ AbstractPollingIoProcessor.this.newSessions.size());
			}

			try {
				synchronized (AbstractPollingIoProcessor.this.disposalLock) {
					if (AbstractPollingIoProcessor.this.disposing) {
						AbstractPollingIoProcessor.this.doDispose();
					}
				}
			} catch (Exception arg21) {
				ExceptionMonitor.getInstance().exceptionCaught(arg21);
			} finally {
				AbstractPollingIoProcessor.this.disposalFuture.setValue(Boolean.valueOf(true));
			}

		}
	}
}