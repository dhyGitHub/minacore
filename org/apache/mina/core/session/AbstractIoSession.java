/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.DefaultFileRegion;
import org.apache.mina.core.file.FilenameFileRegion;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.DefaultCloseFuture;
import org.apache.mina.core.future.DefaultReadFuture;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionAttributeMap;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteTimeoutException;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.mina.util.ExceptionMonitor;

public abstract class AbstractIoSession implements IoSession {
	private final IoHandler handler;
	protected IoSessionConfig config;
	private final IoService service;
	private static final AttributeKey READY_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class,
			"readyReadFutures");
	private static final AttributeKey WAITING_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class,
			"waitingReadFutures");
	private static final IoFutureListener<CloseFuture> SCHEDULED_COUNTER_RESETTER = new IoFutureListener() {
		public void operationComplete(CloseFuture future) {
			AbstractIoSession session = (AbstractIoSession) future.getSession();
			session.scheduledWriteBytes.set(0);
			session.scheduledWriteMessages.set(0);
			session.readBytesThroughput = 0.0D;
			session.readMessagesThroughput = 0.0D;
			session.writtenBytesThroughput = 0.0D;
			session.writtenMessagesThroughput = 0.0D;
		}
	};
	public static final WriteRequest CLOSE_REQUEST = new DefaultWriteRequest(new Object());
	public static final WriteRequest MESSAGE_SENT_REQUEST;
	private final Object lock = new Object();
	private IoSessionAttributeMap attributes;
	private WriteRequestQueue writeRequestQueue;
	private WriteRequest currentWriteRequest;
	private final long creationTime;
	private static AtomicLong idGenerator;
	private long sessionId;
	private final CloseFuture closeFuture = new DefaultCloseFuture(this);
	private volatile boolean closing;
	private boolean readSuspended = false;
	private boolean writeSuspended = false;
	private final AtomicBoolean scheduledForFlush = new AtomicBoolean();
	private final AtomicInteger scheduledWriteBytes = new AtomicInteger();
	private final AtomicInteger scheduledWriteMessages = new AtomicInteger();
	private long readBytes;
	private long writtenBytes;
	private long readMessages;
	private long writtenMessages;
	private long lastReadTime;
	private long lastWriteTime;
	private long lastThroughputCalculationTime;
	private long lastReadBytes;
	private long lastWrittenBytes;
	private long lastReadMessages;
	private long lastWrittenMessages;
	private double readBytesThroughput;
	private double writtenBytesThroughput;
	private double readMessagesThroughput;
	private double writtenMessagesThroughput;
	private AtomicInteger idleCountForBoth = new AtomicInteger();
	private AtomicInteger idleCountForRead = new AtomicInteger();
	private AtomicInteger idleCountForWrite = new AtomicInteger();
	private long lastIdleTimeForBoth;
	private long lastIdleTimeForRead;
	private long lastIdleTimeForWrite;
	private boolean deferDecreaseReadBuffer = true;

	protected AbstractIoSession(IoService service) {
		this.service = service;
		this.handler = service.getHandler();
		long currentTime = System.currentTimeMillis();
		this.creationTime = currentTime;
		this.lastThroughputCalculationTime = currentTime;
		this.lastReadTime = currentTime;
		this.lastWriteTime = currentTime;
		this.lastIdleTimeForBoth = currentTime;
		this.lastIdleTimeForRead = currentTime;
		this.lastIdleTimeForWrite = currentTime;
		this.closeFuture.addListener(SCHEDULED_COUNTER_RESETTER);
		this.sessionId = idGenerator.incrementAndGet();
	}

	public final long getId() {
		return this.sessionId;
	}

	public abstract IoProcessor getProcessor();

	public final boolean isConnected() {
		return !this.closeFuture.isClosed();
	}

	public boolean isActive() {
		return true;
	}

	public final boolean isClosing() {
		return this.closing || this.closeFuture.isClosed();
	}

	public boolean isSecured() {
		return false;
	}

	public final CloseFuture getCloseFuture() {
		return this.closeFuture;
	}

	public final boolean isScheduledForFlush() {
		return this.scheduledForFlush.get();
	}

	public final void scheduledForFlush() {
		this.scheduledForFlush.set(true);
	}

	public final void unscheduledForFlush() {
		this.scheduledForFlush.set(false);
	}

	public final boolean setScheduledForFlush(boolean schedule) {
		if (schedule) {
			return this.scheduledForFlush.compareAndSet(false, schedule);
		} else {
			this.scheduledForFlush.set(schedule);
			return true;
		}
	}

	public final CloseFuture close(boolean rightNow) {
		return rightNow ? this.closeNow() : this.closeOnFlush();
	}

	public final CloseFuture close() {
		return this.closeNow();
	}

	public final CloseFuture closeOnFlush() {
		if (!this.isClosing()) {
			this.getWriteRequestQueue().offer(this, CLOSE_REQUEST);
			this.getProcessor().flush(this);
		}

		return this.closeFuture;
	}

	public final CloseFuture closeNow() {
		Object arg0 = this.lock;
		synchronized (this.lock) {
			if (this.isClosing()) {
				return this.closeFuture;
			}

			this.closing = true;

			try {
				this.destroy();
			} catch (Exception arg4) {
				IoFilterChain filterChain = this.getFilterChain();
				filterChain.fireExceptionCaught(arg4);
			}
		}

		this.getFilterChain().fireFilterClose();
		return this.closeFuture;
	}

	protected void destroy() {
		if (this.writeRequestQueue != null) {
			while (!this.writeRequestQueue.isEmpty(this)) {
				WriteRequest writeRequest = this.writeRequestQueue.poll(this);
				if (writeRequest != null) {
					WriteFuture writeFuture = writeRequest.getFuture();
					if (writeFuture != null) {
						writeFuture.setWritten();
					}
				}
			}
		}

	}

	public IoHandler getHandler() {
		return this.handler;
	}

	public IoSessionConfig getConfig() {
		return this.config;
	}

	public final ReadFuture read() {
		if (!this.getConfig().isUseReadOperation()) {
			throw new IllegalStateException("useReadOperation is not enabled.");
		} else {
			Queue readyReadFutures = this.getReadyReadFutures();
			synchronized (readyReadFutures) {
				Object future = (ReadFuture) readyReadFutures.poll();
				if (future != null) {
					if (((ReadFuture) future).isClosed()) {
						readyReadFutures.offer(future);
					}
				} else {
					future = new DefaultReadFuture(this);
					this.getWaitingReadFutures().offer(future);
				}

				return (ReadFuture) future;
			}
		}
	}

	public final void offerReadFuture(Object message) {
		this.newReadFuture().setRead(message);
	}

	public final void offerFailedReadFuture(Throwable exception) {
		this.newReadFuture().setException(exception);
	}

	public final void offerClosedReadFuture() {
		Queue readyReadFutures = this.getReadyReadFutures();
		synchronized (readyReadFutures) {
			this.newReadFuture().setClosed();
		}
	}

	private ReadFuture newReadFuture() {
		Queue readyReadFutures = this.getReadyReadFutures();
		Queue waitingReadFutures = this.getWaitingReadFutures();
		synchronized (readyReadFutures) {
			Object future = (ReadFuture) waitingReadFutures.poll();
			if (future == null) {
				future = new DefaultReadFuture(this);
				readyReadFutures.offer(future);
			}

			return (ReadFuture) future;
		}
	}

	private Queue<ReadFuture> getReadyReadFutures() {
		Object readyReadFutures = (Queue) this.getAttribute(READY_READ_FUTURES_KEY);
		if (readyReadFutures == null) {
			readyReadFutures = new ConcurrentLinkedQueue();
			Queue oldReadyReadFutures = (Queue) this.setAttributeIfAbsent(READY_READ_FUTURES_KEY, readyReadFutures);
			if (oldReadyReadFutures != null) {
				readyReadFutures = oldReadyReadFutures;
			}
		}

		return (Queue) readyReadFutures;
	}

	private Queue<ReadFuture> getWaitingReadFutures() {
		Object waitingReadyReadFutures = (Queue) this.getAttribute(WAITING_READ_FUTURES_KEY);
		if (waitingReadyReadFutures == null) {
			waitingReadyReadFutures = new ConcurrentLinkedQueue();
			Queue oldWaitingReadyReadFutures = (Queue) this.setAttributeIfAbsent(WAITING_READ_FUTURES_KEY,
					waitingReadyReadFutures);
			if (oldWaitingReadyReadFutures != null) {
				waitingReadyReadFutures = oldWaitingReadyReadFutures;
			}
		}

		return (Queue) waitingReadyReadFutures;
	}

	public WriteFuture write(Object message) {
		return this.write(message, (SocketAddress) null);
	}

	public WriteFuture write(Object message, SocketAddress remoteAddress) {
		if (message == null) {
			throw new IllegalArgumentException("Trying to write a null message : not allowed");
		} else if (!this.getTransportMetadata().isConnectionless() && remoteAddress != null) {
			throw new UnsupportedOperationException();
		} else if (!this.isClosing() && this.isConnected()) {
			final FileChannel openedFileChannel1 = null;

			try {
				if (message instanceof IoBuffer && !((IoBuffer) message).hasRemaining()) {
					throw new IllegalArgumentException("message is empty. Forgot to call flip()?");
				}

				if (message instanceof FileChannel) {
					FileChannel writeFuture1 = (FileChannel) message;
					message = new DefaultFileRegion(writeFuture1, 0L, writeFuture1.size());
				} else if (message instanceof File) {
					File writeFuture2 = (File) message;
					openedFileChannel1 = (new FileInputStream(writeFuture2)).getChannel();
					message = new FilenameFileRegion(writeFuture2, openedFileChannel1, 0L, openedFileChannel1.size());
				}
			} catch (IOException arg7) {
				ExceptionMonitor.getInstance().exceptionCaught(arg7);
				return DefaultWriteFuture.newNotWrittenFuture(this, arg7);
			}

			DefaultWriteFuture writeFuture3 = new DefaultWriteFuture(this);
			DefaultWriteRequest writeRequest1 = new DefaultWriteRequest(message, writeFuture3, remoteAddress);
			IoFilterChain filterChain = this.getFilterChain();
			filterChain.fireFilterWrite(writeRequest1);
			if (openedFileChannel1 != null) {
				writeFuture3.addListener(new IoFutureListener() {
					public void operationComplete(WriteFuture future) {
						try {
							openedFileChannel1.close();
						} catch (IOException arg2) {
							ExceptionMonitor.getInstance().exceptionCaught(arg2);
						}

					}
				});
			}

			return writeFuture3;
		} else {
			DefaultWriteFuture openedFileChannel = new DefaultWriteFuture(this);
			DefaultWriteRequest writeFuture = new DefaultWriteRequest(message, openedFileChannel, remoteAddress);
			WriteToClosedSessionException writeRequest = new WriteToClosedSessionException(writeFuture);
			openedFileChannel.setException(writeRequest);
			return openedFileChannel;
		}
	}

	public final Object getAttachment() {
		return this.getAttribute("");
	}

	public final Object setAttachment(Object attachment) {
		return this.setAttribute("", attachment);
	}

	public final Object getAttribute(Object key) {
		return this.getAttribute(key, (Object) null);
	}

	public final Object getAttribute(Object key, Object defaultValue) {
		return this.attributes.getAttribute(this, key, defaultValue);
	}

	public final Object setAttribute(Object key, Object value) {
		return this.attributes.setAttribute(this, key, value);
	}

	public final Object setAttribute(Object key) {
		return this.setAttribute(key, Boolean.TRUE);
	}

	public final Object setAttributeIfAbsent(Object key, Object value) {
		return this.attributes.setAttributeIfAbsent(this, key, value);
	}

	public final Object setAttributeIfAbsent(Object key) {
		return this.setAttributeIfAbsent(key, Boolean.TRUE);
	}

	public final Object removeAttribute(Object key) {
		return this.attributes.removeAttribute(this, key);
	}

	public final boolean removeAttribute(Object key, Object value) {
		return this.attributes.removeAttribute(this, key, value);
	}

	public final boolean replaceAttribute(Object key, Object oldValue, Object newValue) {
		return this.attributes.replaceAttribute(this, key, oldValue, newValue);
	}

	public final boolean containsAttribute(Object key) {
		return this.attributes.containsAttribute(this, key);
	}

	public final Set<Object> getAttributeKeys() {
		return this.attributes.getAttributeKeys(this);
	}

	public final IoSessionAttributeMap getAttributeMap() {
		return this.attributes;
	}

	public final void setAttributeMap(IoSessionAttributeMap attributes) {
		this.attributes = attributes;
	}

	public final void setWriteRequestQueue(WriteRequestQueue writeRequestQueue) {
		this.writeRequestQueue = writeRequestQueue;
	}

	public final void suspendRead() {
		this.readSuspended = true;
		if (!this.isClosing() && this.isConnected()) {
			this.getProcessor().updateTrafficControl(this);
		}
	}

	public final void suspendWrite() {
		this.writeSuspended = true;
		if (!this.isClosing() && this.isConnected()) {
			this.getProcessor().updateTrafficControl(this);
		}
	}

	public final void resumeRead() {
		this.readSuspended = false;
		if (!this.isClosing() && this.isConnected()) {
			this.getProcessor().updateTrafficControl(this);
		}
	}

	public final void resumeWrite() {
		this.writeSuspended = false;
		if (!this.isClosing() && this.isConnected()) {
			this.getProcessor().updateTrafficControl(this);
		}
	}

	public boolean isReadSuspended() {
		return this.readSuspended;
	}

	public boolean isWriteSuspended() {
		return this.writeSuspended;
	}

	public final long getReadBytes() {
		return this.readBytes;
	}

	public final long getWrittenBytes() {
		return this.writtenBytes;
	}

	public final long getReadMessages() {
		return this.readMessages;
	}

	public final long getWrittenMessages() {
		return this.writtenMessages;
	}

	public final double getReadBytesThroughput() {
		return this.readBytesThroughput;
	}

	public final double getWrittenBytesThroughput() {
		return this.writtenBytesThroughput;
	}

	public final double getReadMessagesThroughput() {
		return this.readMessagesThroughput;
	}

	public final double getWrittenMessagesThroughput() {
		return this.writtenMessagesThroughput;
	}

	public final void updateThroughput(long currentTime, boolean force) {
		int interval = (int) (currentTime - this.lastThroughputCalculationTime);
		long minInterval = this.getConfig().getThroughputCalculationIntervalInMillis();
		if (minInterval != 0L && (long) interval >= minInterval || force) {
			this.readBytesThroughput = (double) (this.readBytes - this.lastReadBytes) * 1000.0D / (double) interval;
			this.writtenBytesThroughput = (double) (this.writtenBytes - this.lastWrittenBytes) * 1000.0D
					/ (double) interval;
			this.readMessagesThroughput = (double) (this.readMessages - this.lastReadMessages) * 1000.0D
					/ (double) interval;
			this.writtenMessagesThroughput = (double) (this.writtenMessages - this.lastWrittenMessages) * 1000.0D
					/ (double) interval;
			this.lastReadBytes = this.readBytes;
			this.lastWrittenBytes = this.writtenBytes;
			this.lastReadMessages = this.readMessages;
			this.lastWrittenMessages = this.writtenMessages;
			this.lastThroughputCalculationTime = currentTime;
		}
	}

	public final long getScheduledWriteBytes() {
		return (long) this.scheduledWriteBytes.get();
	}

	public final int getScheduledWriteMessages() {
		return this.scheduledWriteMessages.get();
	}

	protected void setScheduledWriteBytes(int byteCount) {
		this.scheduledWriteBytes.set(byteCount);
	}

	protected void setScheduledWriteMessages(int messages) {
		this.scheduledWriteMessages.set(messages);
	}

	public final void increaseReadBytes(long increment, long currentTime) {
		if (increment > 0L) {
			this.readBytes += increment;
			this.lastReadTime = currentTime;
			this.idleCountForBoth.set(0);
			this.idleCountForRead.set(0);
			if (this.getService() instanceof AbstractIoService) {
				((AbstractIoService) this.getService()).getStatistics().increaseReadBytes(increment, currentTime);
			}

		}
	}

	public final void increaseReadMessages(long currentTime) {
		++this.readMessages;
		this.lastReadTime = currentTime;
		this.idleCountForBoth.set(0);
		this.idleCountForRead.set(0);
		if (this.getService() instanceof AbstractIoService) {
			((AbstractIoService) this.getService()).getStatistics().increaseReadMessages(currentTime);
		}

	}

	public final void increaseWrittenBytes(int increment, long currentTime) {
		if (increment > 0) {
			this.writtenBytes += (long) increment;
			this.lastWriteTime = currentTime;
			this.idleCountForBoth.set(0);
			this.idleCountForWrite.set(0);
			if (this.getService() instanceof AbstractIoService) {
				((AbstractIoService) this.getService()).getStatistics().increaseWrittenBytes(increment, currentTime);
			}

			this.increaseScheduledWriteBytes(-increment);
		}
	}

	public final void increaseWrittenMessages(WriteRequest request, long currentTime) {
		Object message = request.getMessage();
		if (message instanceof IoBuffer) {
			IoBuffer b = (IoBuffer) message;
			if (b.hasRemaining()) {
				return;
			}
		}

		++this.writtenMessages;
		this.lastWriteTime = currentTime;
		if (this.getService() instanceof AbstractIoService) {
			((AbstractIoService) this.getService()).getStatistics().increaseWrittenMessages(currentTime);
		}

		this.decreaseScheduledWriteMessages();
	}

	public final void increaseScheduledWriteBytes(int increment) {
		this.scheduledWriteBytes.addAndGet(increment);
		if (this.getService() instanceof AbstractIoService) {
			((AbstractIoService) this.getService()).getStatistics().increaseScheduledWriteBytes(increment);
		}

	}

	public final void increaseScheduledWriteMessages() {
		this.scheduledWriteMessages.incrementAndGet();
		if (this.getService() instanceof AbstractIoService) {
			((AbstractIoService) this.getService()).getStatistics().increaseScheduledWriteMessages();
		}

	}

	private void decreaseScheduledWriteMessages() {
		this.scheduledWriteMessages.decrementAndGet();
		if (this.getService() instanceof AbstractIoService) {
			((AbstractIoService) this.getService()).getStatistics().decreaseScheduledWriteMessages();
		}

	}

	public final void decreaseScheduledBytesAndMessages(WriteRequest request) {
		Object message = request.getMessage();
		if (message instanceof IoBuffer) {
			IoBuffer b = (IoBuffer) message;
			if (b.hasRemaining()) {
				this.increaseScheduledWriteBytes(-((IoBuffer) message).remaining());
			} else {
				this.decreaseScheduledWriteMessages();
			}
		} else {
			this.decreaseScheduledWriteMessages();
		}

	}

	public final WriteRequestQueue getWriteRequestQueue() {
		if (this.writeRequestQueue == null) {
			throw new IllegalStateException();
		} else {
			return this.writeRequestQueue;
		}
	}

	public final WriteRequest getCurrentWriteRequest() {
		return this.currentWriteRequest;
	}

	public final Object getCurrentWriteMessage() {
		WriteRequest req = this.getCurrentWriteRequest();
		return req == null ? null : req.getMessage();
	}

	public final void setCurrentWriteRequest(WriteRequest currentWriteRequest) {
		this.currentWriteRequest = currentWriteRequest;
	}

	public final void increaseReadBufferSize() {
		int newReadBufferSize = this.getConfig().getReadBufferSize() << 1;
		if (newReadBufferSize <= this.getConfig().getMaxReadBufferSize()) {
			this.getConfig().setReadBufferSize(newReadBufferSize);
		} else {
			this.getConfig().setReadBufferSize(this.getConfig().getMaxReadBufferSize());
		}

		this.deferDecreaseReadBuffer = true;
	}

	public final void decreaseReadBufferSize() {
		if (this.deferDecreaseReadBuffer) {
			this.deferDecreaseReadBuffer = false;
		} else {
			if (this.getConfig().getReadBufferSize() > this.getConfig().getMinReadBufferSize()) {
				this.getConfig().setReadBufferSize(this.getConfig().getReadBufferSize() >>> 1);
			}

			this.deferDecreaseReadBuffer = true;
		}
	}

	public final long getCreationTime() {
		return this.creationTime;
	}

	public final long getLastIoTime() {
		return Math.max(this.lastReadTime, this.lastWriteTime);
	}

	public final long getLastReadTime() {
		return this.lastReadTime;
	}

	public final long getLastWriteTime() {
		return this.lastWriteTime;
	}

	public final boolean isIdle(IdleStatus status) {
		if (status == IdleStatus.BOTH_IDLE) {
			return this.idleCountForBoth.get() > 0;
		} else if (status == IdleStatus.READER_IDLE) {
			return this.idleCountForRead.get() > 0;
		} else if (status == IdleStatus.WRITER_IDLE) {
			return this.idleCountForWrite.get() > 0;
		} else {
			throw new IllegalArgumentException("Unknown idle status: " + status);
		}
	}

	public final boolean isBothIdle() {
		return this.isIdle(IdleStatus.BOTH_IDLE);
	}

	public final boolean isReaderIdle() {
		return this.isIdle(IdleStatus.READER_IDLE);
	}

	public final boolean isWriterIdle() {
		return this.isIdle(IdleStatus.WRITER_IDLE);
	}

	public final int getIdleCount(IdleStatus status) {
		if (this.getConfig().getIdleTime(status) == 0) {
			if (status == IdleStatus.BOTH_IDLE) {
				this.idleCountForBoth.set(0);
			}

			if (status == IdleStatus.READER_IDLE) {
				this.idleCountForRead.set(0);
			}

			if (status == IdleStatus.WRITER_IDLE) {
				this.idleCountForWrite.set(0);
			}
		}

		if (status == IdleStatus.BOTH_IDLE) {
			return this.idleCountForBoth.get();
		} else if (status == IdleStatus.READER_IDLE) {
			return this.idleCountForRead.get();
		} else if (status == IdleStatus.WRITER_IDLE) {
			return this.idleCountForWrite.get();
		} else {
			throw new IllegalArgumentException("Unknown idle status: " + status);
		}
	}

	public final long getLastIdleTime(IdleStatus status) {
		if (status == IdleStatus.BOTH_IDLE) {
			return this.lastIdleTimeForBoth;
		} else if (status == IdleStatus.READER_IDLE) {
			return this.lastIdleTimeForRead;
		} else if (status == IdleStatus.WRITER_IDLE) {
			return this.lastIdleTimeForWrite;
		} else {
			throw new IllegalArgumentException("Unknown idle status: " + status);
		}
	}

	public final void increaseIdleCount(IdleStatus status, long currentTime) {
		if (status == IdleStatus.BOTH_IDLE) {
			this.idleCountForBoth.incrementAndGet();
			this.lastIdleTimeForBoth = currentTime;
		} else if (status == IdleStatus.READER_IDLE) {
			this.idleCountForRead.incrementAndGet();
			this.lastIdleTimeForRead = currentTime;
		} else {
			if (status != IdleStatus.WRITER_IDLE) {
				throw new IllegalArgumentException("Unknown idle status: " + status);
			}

			this.idleCountForWrite.incrementAndGet();
			this.lastIdleTimeForWrite = currentTime;
		}

	}

	public final int getBothIdleCount() {
		return this.getIdleCount(IdleStatus.BOTH_IDLE);
	}

	public final long getLastBothIdleTime() {
		return this.getLastIdleTime(IdleStatus.BOTH_IDLE);
	}

	public final long getLastReaderIdleTime() {
		return this.getLastIdleTime(IdleStatus.READER_IDLE);
	}

	public final long getLastWriterIdleTime() {
		return this.getLastIdleTime(IdleStatus.WRITER_IDLE);
	}

	public final int getReaderIdleCount() {
		return this.getIdleCount(IdleStatus.READER_IDLE);
	}

	public final int getWriterIdleCount() {
		return this.getIdleCount(IdleStatus.WRITER_IDLE);
	}

	public SocketAddress getServiceAddress() {
		IoService service = this.getService();
		return service instanceof IoAcceptor ? ((IoAcceptor) service).getLocalAddress() : this.getRemoteAddress();
	}

	public final int hashCode() {
		return super.hashCode();
	}

	public final boolean equals(Object o) {
		return super.equals(o);
	}

	public String toString() {
		if (!this.isConnected() && !this.isClosing()) {
			return "(" + this.getIdAsString() + ") Session disconnected ...";
		} else {
			String remote = null;
			String local = null;

			try {
				remote = String.valueOf(this.getRemoteAddress());
			} catch (Exception arg4) {
				remote = "Cannot get the remote address informations: " + arg4.getMessage();
			}

			try {
				local = String.valueOf(this.getLocalAddress());
			} catch (Exception arg3) {
				;
			}

			return this.getService() instanceof IoAcceptor
					? "(" + this.getIdAsString() + ": " + this.getServiceName() + ", server, " + remote + " => " + local
							+ ')'
					: "(" + this.getIdAsString() + ": " + this.getServiceName() + ", client, " + local + " => " + remote
							+ ')';
		}
	}

	private String getIdAsString() {
		String id = Long.toHexString(this.getId()).toUpperCase();
		return id.length() <= 8 ? "0x00000000".substring(0, 10 - id.length()) + id : "0x" + id;
	}

	private String getServiceName() {
		TransportMetadata tm = this.getTransportMetadata();
		return tm == null ? "null" : tm.getProviderName() + ' ' + tm.getName();
	}

	public IoService getService() {
		return this.service;
	}

	public static void notifyIdleness(Iterator<? extends IoSession> sessions, long currentTime) {
		while (sessions.hasNext()) {
			IoSession session = (IoSession) sessions.next();
			if (!session.getCloseFuture().isClosed()) {
				notifyIdleSession(session, currentTime);
			}
		}

	}

	public static void notifyIdleSession(IoSession session, long currentTime) {
		notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.BOTH_IDLE),
				IdleStatus.BOTH_IDLE, Math.max(session.getLastIoTime(), session.getLastIdleTime(IdleStatus.BOTH_IDLE)));
		notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.READER_IDLE),
				IdleStatus.READER_IDLE,
				Math.max(session.getLastReadTime(), session.getLastIdleTime(IdleStatus.READER_IDLE)));
		notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.WRITER_IDLE),
				IdleStatus.WRITER_IDLE,
				Math.max(session.getLastWriteTime(), session.getLastIdleTime(IdleStatus.WRITER_IDLE)));
		notifyWriteTimeout(session, currentTime);
	}

	private static void notifyIdleSession0(IoSession session, long currentTime, long idleTime, IdleStatus status,
			long lastIoTime) {
		if (idleTime > 0L && lastIoTime != 0L && currentTime - lastIoTime >= idleTime) {
			session.getFilterChain().fireSessionIdle(status);
		}

	}

	private static void notifyWriteTimeout(IoSession session, long currentTime) {
		long writeTimeout = session.getConfig().getWriteTimeoutInMillis();
		if (writeTimeout > 0L && currentTime - session.getLastWriteTime() >= writeTimeout
				&& !session.getWriteRequestQueue().isEmpty(session)) {
			WriteRequest request = session.getCurrentWriteRequest();
			if (request != null) {
				session.setCurrentWriteRequest((WriteRequest) null);
				WriteTimeoutException cause = new WriteTimeoutException(request);
				request.getFuture().setException(cause);
				session.getFilterChain().fireExceptionCaught(cause);
				session.closeNow();
			}
		}

	}

	static {
		MESSAGE_SENT_REQUEST = new DefaultWriteRequest(DefaultWriteRequest.EMPTY_MESSAGE);
		idGenerator = new AtomicLong(0L);
	}
}