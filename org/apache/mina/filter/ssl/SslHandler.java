/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.ssl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterEvent;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.ssl.SslFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SslHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SslHandler.class);
	private final SslFilter sslFilter;
	private final IoSession session;
	private final Queue<IoFilterEvent> preHandshakeEventQueue = new ConcurrentLinkedQueue();
	private final Queue<IoFilterEvent> filterWriteEventQueue = new ConcurrentLinkedQueue();
	private final Queue<IoFilterEvent> messageReceivedEventQueue = new ConcurrentLinkedQueue();
	private SSLEngine sslEngine;
	private IoBuffer inNetBuffer;
	private IoBuffer outNetBuffer;
	private IoBuffer appBuffer;
	private final IoBuffer emptyBuffer = IoBuffer.allocate(0);
	private HandshakeStatus handshakeStatus;
	private boolean firstSSLNegociation;
	private boolean handshakeComplete;
	private boolean writingEncryptedData;
	private ReentrantLock sslLock = new ReentrantLock();
	private final AtomicInteger scheduled_events = new AtomicInteger(0);

	SslHandler(SslFilter sslFilter, IoSession session) throws SSLException {
		this.sslFilter = sslFilter;
		this.session = session;
	}

	void init() throws SSLException {
		if (this.sslEngine == null) {
			LOGGER.debug("{} Initializing the SSL Handler", this.sslFilter.getSessionInfo(this.session));
			InetSocketAddress peer = (InetSocketAddress) this.session.getAttribute(SslFilter.PEER_ADDRESS);
			if (peer == null) {
				this.sslEngine = this.sslFilter.sslContext.createSSLEngine();
			} else {
				this.sslEngine = this.sslFilter.sslContext.createSSLEngine(peer.getHostName(), peer.getPort());
			}

			this.sslEngine.setUseClientMode(this.sslFilter.isUseClientMode());
			if (!this.sslEngine.getUseClientMode()) {
				if (this.sslFilter.isWantClientAuth()) {
					this.sslEngine.setWantClientAuth(true);
				}

				if (this.sslFilter.isNeedClientAuth()) {
					this.sslEngine.setNeedClientAuth(true);
				}
			}

			if (this.sslFilter.getEnabledCipherSuites() != null) {
				this.sslEngine.setEnabledCipherSuites(this.sslFilter.getEnabledCipherSuites());
			}

			if (this.sslFilter.getEnabledProtocols() != null) {
				this.sslEngine.setEnabledProtocols(this.sslFilter.getEnabledProtocols());
			}

			this.sslEngine.beginHandshake();
			this.handshakeStatus = this.sslEngine.getHandshakeStatus();
			this.writingEncryptedData = false;
			this.firstSSLNegociation = true;
			this.handshakeComplete = false;
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("{} SSL Handler Initialization done.", this.sslFilter.getSessionInfo(this.session));
			}

		}
	}

	void destroy() {
		if (this.sslEngine != null) {
			try {
				this.sslEngine.closeInbound();
			} catch (SSLException arg5) {
				LOGGER.debug("Unexpected exception from SSLEngine.closeInbound().", arg5);
			}

			if (this.outNetBuffer != null) {
				this.outNetBuffer.capacity(this.sslEngine.getSession().getPacketBufferSize());
			} else {
				this.createOutNetBuffer(0);
			}

			try {
				do {
					this.outNetBuffer.clear();
				} while (this.sslEngine.wrap(this.emptyBuffer.buf(), this.outNetBuffer.buf()).bytesProduced() > 0);
			} catch (SSLException arg6) {
				;
			} finally {
				this.outNetBuffer.free();
				this.outNetBuffer = null;
			}

			this.sslEngine.closeOutbound();
			this.sslEngine = null;
			this.preHandshakeEventQueue.clear();
		}
	}

	SslFilter getSslFilter() {
		return this.sslFilter;
	}

	IoSession getSession() {
		return this.session;
	}

	boolean isWritingEncryptedData() {
		return this.writingEncryptedData;
	}

	boolean isHandshakeComplete() {
		return this.handshakeComplete;
	}

	boolean isInboundDone() {
		return this.sslEngine == null || this.sslEngine.isInboundDone();
	}

	boolean isOutboundDone() {
		return this.sslEngine == null || this.sslEngine.isOutboundDone();
	}

	boolean needToCompleteHandshake() {
		return this.handshakeStatus == HandshakeStatus.NEED_WRAP && !this.isInboundDone();
	}

	void schedulePreHandshakeWriteRequest(NextFilter nextFilter, WriteRequest writeRequest) {
		this.preHandshakeEventQueue.add(new IoFilterEvent(nextFilter, IoEventType.WRITE, this.session, writeRequest));
	}

	void flushPreHandshakeEvents() throws SSLException {
		IoFilterEvent scheduledWrite;
		while ((scheduledWrite = (IoFilterEvent) this.preHandshakeEventQueue.poll()) != null) {
			this.sslFilter.filterWrite(scheduledWrite.getNextFilter(), this.session,
					(WriteRequest) scheduledWrite.getParameter());
		}

	}

	void scheduleFilterWrite(NextFilter nextFilter, WriteRequest writeRequest) {
		this.filterWriteEventQueue.add(new IoFilterEvent(nextFilter, IoEventType.WRITE, this.session, writeRequest));
	}

	void scheduleMessageReceived(NextFilter nextFilter, Object message) {
		this.messageReceivedEventQueue
				.add(new IoFilterEvent(nextFilter, IoEventType.MESSAGE_RECEIVED, this.session, message));
	}

	void flushScheduledEvents() {
		this.scheduled_events.incrementAndGet();
		if (this.sslLock.tryLock()) {
			try {
				while (true) {
					IoFilterEvent event;
					NextFilter nextFilter;
					while ((event = (IoFilterEvent) this.filterWriteEventQueue.poll()) == null) {
						while ((event = (IoFilterEvent) this.messageReceivedEventQueue.poll()) != null) {
							nextFilter = event.getNextFilter();
							nextFilter.messageReceived(this.session, event.getParameter());
						}

						if (this.scheduled_events.decrementAndGet() <= 0) {
							return;
						}
					}

					nextFilter = event.getNextFilter();
					nextFilter.filterWrite(this.session, (WriteRequest) event.getParameter());
				}
			} finally {
				this.sslLock.unlock();
			}
		}
	}

	void messageReceived(NextFilter nextFilter, ByteBuffer buf) throws SSLException {
		if (LOGGER.isDebugEnabled()) {
			if (!this.isOutboundDone()) {
				LOGGER.debug("{} Processing the received message", this.sslFilter.getSessionInfo(this.session));
			} else {
				LOGGER.debug("{} Processing the received message", this.sslFilter.getSessionInfo(this.session));
			}
		}

		if (this.inNetBuffer == null) {
			this.inNetBuffer = IoBuffer.allocate(buf.remaining()).setAutoExpand(true);
		}

		this.inNetBuffer.put(buf);
		if (!this.handshakeComplete) {
			this.handshake(nextFilter);
		} else {
			this.inNetBuffer.flip();
			if (!this.inNetBuffer.hasRemaining()) {
				return;
			}

			SSLEngineResult inNetBufferPosition = this.unwrap();
			if (this.inNetBuffer.hasRemaining()) {
				this.inNetBuffer.compact();
			} else {
				this.inNetBuffer.free();
				this.inNetBuffer = null;
			}

			this.checkStatus(inNetBufferPosition);
			this.renegotiateIfNeeded(nextFilter, inNetBufferPosition);
		}

		if (this.isInboundDone()) {
			int inNetBufferPosition1 = this.inNetBuffer == null ? 0 : this.inNetBuffer.position();
			buf.position(buf.position() - inNetBufferPosition1);
			if (this.inNetBuffer != null) {
				this.inNetBuffer.free();
				this.inNetBuffer = null;
			}
		}

	}

	IoBuffer fetchAppBuffer() {
		if (this.appBuffer == null) {
			return IoBuffer.allocate(0);
		} else {
			IoBuffer appBuffer = this.appBuffer.flip();
			this.appBuffer = null;
			return appBuffer.shrink();
		}
	}

	IoBuffer fetchOutNetBuffer() {
		IoBuffer answer = this.outNetBuffer;
		if (answer == null) {
			return this.emptyBuffer;
		} else {
			this.outNetBuffer = null;
			return answer.shrink();
		}
	}

	void encrypt(ByteBuffer src) throws SSLException {
		if (!this.handshakeComplete) {
			throw new IllegalStateException();
		} else if (!src.hasRemaining()) {
			if (this.outNetBuffer == null) {
				this.outNetBuffer = this.emptyBuffer;
			}

		} else {
			this.createOutNetBuffer(src.remaining());

			while (src.hasRemaining()) {
				SSLEngineResult result = this.sslEngine.wrap(src, this.outNetBuffer.buf());
				if (result.getStatus() == Status.OK) {
					if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
						this.doTasks();
					}
				} else {
					if (result.getStatus() != Status.BUFFER_OVERFLOW) {
						throw new SSLException("SSLEngine error during encrypt: " + result.getStatus() + " src: " + src
								+ "outNetBuffer: " + this.outNetBuffer);
					}

					this.outNetBuffer.capacity(this.outNetBuffer.capacity() << 1);
					this.outNetBuffer.limit(this.outNetBuffer.capacity());
				}
			}

			this.outNetBuffer.flip();
		}
	}

	boolean closeOutbound() throws SSLException {
		if (this.sslEngine != null && !this.sslEngine.isOutboundDone()) {
			this.sslEngine.closeOutbound();
			this.createOutNetBuffer(0);

			while (true) {
				SSLEngineResult result = this.sslEngine.wrap(this.emptyBuffer.buf(), this.outNetBuffer.buf());
				if (result.getStatus() != Status.BUFFER_OVERFLOW) {
					if (result.getStatus() != Status.CLOSED) {
						throw new SSLException("Improper close state: " + result);
					} else {
						this.outNetBuffer.flip();
						return true;
					}
				}

				this.outNetBuffer.capacity(this.outNetBuffer.capacity() << 1);
				this.outNetBuffer.limit(this.outNetBuffer.capacity());
			}
		} else {
			return false;
		}
	}

	private void checkStatus(SSLEngineResult res) throws SSLException {
		Status status = res.getStatus();
		if (status == Status.BUFFER_OVERFLOW) {
			throw new SSLException("SSLEngine error during decrypt: " + status + " inNetBuffer: " + this.inNetBuffer
					+ "appBuffer: " + this.appBuffer);
		}
	}

	void handshake(NextFilter nextFilter) throws SSLException {
		label64: while (true) {
			switch (SslHandler.SyntheticClass_1.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[this.handshakeStatus
					.ordinal()]) {
			case 1:
			case 2:
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("{} processing the FINISHED state", this.sslFilter.getSessionInfo(this.session));
				}

				this.session.setAttribute(SslFilter.SSL_SESSION, this.sslEngine.getSession());
				this.handshakeComplete = true;
				if (this.firstSSLNegociation && this.session.containsAttribute(SslFilter.USE_NOTIFICATION)) {
					this.firstSSLNegociation = false;
					this.scheduleMessageReceived(nextFilter, SslFilter.SESSION_SECURED);
				}

				if (LOGGER.isDebugEnabled()) {
					if (!this.isOutboundDone()) {
						LOGGER.debug("{} is now secured", this.sslFilter.getSessionInfo(this.session));
					} else {
						LOGGER.debug("{} is not secured yet", this.sslFilter.getSessionInfo(this.session));
					}
				}

				return;
			case 3:
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("{} processing the NEED_TASK state", this.sslFilter.getSessionInfo(this.session));
				}

				this.handshakeStatus = this.doTasks();
				break;
			case 4:
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("{} processing the NEED_UNWRAP state", this.sslFilter.getSessionInfo(this.session));
				}

				Status status = this.unwrapHandshake(nextFilter);
				if ((status != Status.BUFFER_UNDERFLOW || this.handshakeStatus == HandshakeStatus.FINISHED)
						&& !this.isInboundDone()) {
					break;
				}

				return;
			case 5:
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("{} processing the NEED_WRAP state", this.sslFilter.getSessionInfo(this.session));
				}

				if (this.outNetBuffer != null && this.outNetBuffer.hasRemaining()) {
					return;
				}

				this.createOutNetBuffer(0);

				while (true) {
					SSLEngineResult result = this.sslEngine.wrap(this.emptyBuffer.buf(), this.outNetBuffer.buf());
					if (result.getStatus() != Status.BUFFER_OVERFLOW) {
						this.outNetBuffer.flip();
						this.handshakeStatus = result.getHandshakeStatus();
						this.writeNetBuffer(nextFilter);
						continue label64;
					}

					this.outNetBuffer.capacity(this.outNetBuffer.capacity() << 1);
					this.outNetBuffer.limit(this.outNetBuffer.capacity());
				}
			default:
				String msg = "Invalid Handshaking State" + this.handshakeStatus
						+ " while processing the Handshake for session " + this.session.getId();
				LOGGER.error(msg);
				throw new IllegalStateException(msg);
			}
		}
	}

	private void createOutNetBuffer(int expectedRemaining) {
		int capacity = Math.max(expectedRemaining, this.sslEngine.getSession().getPacketBufferSize());
		if (this.outNetBuffer != null) {
			this.outNetBuffer.capacity(capacity);
		} else {
			this.outNetBuffer = IoBuffer.allocate(capacity).minimumCapacity(0);
		}

	}

	WriteFuture writeNetBuffer(NextFilter nextFilter) throws SSLException {
		if (this.outNetBuffer != null && this.outNetBuffer.hasRemaining()) {
			this.writingEncryptedData = true;
			DefaultWriteFuture writeFuture = null;

			try {
				IoBuffer writeBuffer = this.fetchOutNetBuffer();
				writeFuture = new DefaultWriteFuture(this.session);
				this.sslFilter.filterWrite(nextFilter, this.session, new DefaultWriteRequest(writeBuffer, writeFuture));

				while (this.needToCompleteHandshake()) {
					try {
						this.handshake(nextFilter);
					} catch (SSLException arg8) {
						SSLHandshakeException newSsle = new SSLHandshakeException("SSL handshake failed.");
						newSsle.initCause(arg8);
						throw newSsle;
					}

					IoBuffer outNetBuffer = this.fetchOutNetBuffer();
					if (outNetBuffer != null && outNetBuffer.hasRemaining()) {
						writeFuture = new DefaultWriteFuture(this.session);
						this.sslFilter.filterWrite(nextFilter, this.session,
								new DefaultWriteRequest(outNetBuffer, writeFuture));
					}
				}
			} finally {
				this.writingEncryptedData = false;
			}

			return writeFuture;
		} else {
			return null;
		}
	}

	private Status unwrapHandshake(NextFilter nextFilter) throws SSLException {
		if (this.inNetBuffer != null) {
			this.inNetBuffer.flip();
		}

		if (this.inNetBuffer != null && this.inNetBuffer.hasRemaining()) {
			SSLEngineResult res = this.unwrap();
			this.handshakeStatus = res.getHandshakeStatus();
			this.checkStatus(res);
			if (this.handshakeStatus == HandshakeStatus.FINISHED && res.getStatus() == Status.OK
					&& this.inNetBuffer.hasRemaining()) {
				res = this.unwrap();
				if (this.inNetBuffer.hasRemaining()) {
					this.inNetBuffer.compact();
				} else {
					this.inNetBuffer.free();
					this.inNetBuffer = null;
				}

				this.renegotiateIfNeeded(nextFilter, res);
			} else if (this.inNetBuffer.hasRemaining()) {
				this.inNetBuffer.compact();
			} else {
				this.inNetBuffer.free();
				this.inNetBuffer = null;
			}

			return res.getStatus();
		} else {
			return Status.BUFFER_UNDERFLOW;
		}
	}

	private void renegotiateIfNeeded(NextFilter nextFilter, SSLEngineResult res) throws SSLException {
		if (res.getStatus() != Status.CLOSED && res.getStatus() != Status.BUFFER_UNDERFLOW
				&& res.getHandshakeStatus() != HandshakeStatus.NOT_HANDSHAKING) {
			this.handshakeComplete = false;
			this.handshakeStatus = res.getHandshakeStatus();
			this.handshake(nextFilter);
		}

	}

	private SSLEngineResult unwrap() throws SSLException {
		if (this.appBuffer == null) {
			this.appBuffer = IoBuffer.allocate(this.inNetBuffer.remaining());
		} else {
			this.appBuffer.expand(this.inNetBuffer.remaining());
		}

		Status status = null;
		HandshakeStatus handshakeStatus = null;

		SSLEngineResult res;
		do {
			res = this.sslEngine.unwrap(this.inNetBuffer.buf(), this.appBuffer.buf());
			status = res.getStatus();
			handshakeStatus = res.getHandshakeStatus();
			if (status == Status.BUFFER_OVERFLOW) {
				int newCapacity = this.sslEngine.getSession().getApplicationBufferSize();
				if (this.appBuffer.remaining() >= newCapacity) {
					throw new SSLException("SSL buffer overflow");
				}

				this.appBuffer.expand(newCapacity);
			}
		} while ((status == Status.OK || status == Status.BUFFER_OVERFLOW)
				&& (handshakeStatus == HandshakeStatus.NOT_HANDSHAKING
						|| handshakeStatus == HandshakeStatus.NEED_UNWRAP));

		return res;
	}

	private HandshakeStatus doTasks() {
		Runnable runnable;
		while ((runnable = this.sslEngine.getDelegatedTask()) != null) {
			runnable.run();
		}

		return this.sslEngine.getHandshakeStatus();
	}

	static IoBuffer copy(ByteBuffer src) {
		IoBuffer copy = IoBuffer.allocate(src.remaining());
		copy.put(src);
		copy.flip();
		return copy;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SSLStatus <");
		if (this.handshakeComplete) {
			sb.append("SSL established");
		} else {
			sb.append("Processing Handshake").append("; ");
			sb.append("Status : ").append(this.handshakeStatus).append("; ");
		}

		sb.append(", ");
		sb.append("HandshakeComplete :").append(this.handshakeComplete).append(", ");
		sb.append(">");
		return sb.toString();
	}

	void release() {
		if (this.inNetBuffer != null) {
			this.inNetBuffer.free();
			this.inNetBuffer = null;
		}

		if (this.outNetBuffer != null) {
			this.outNetBuffer.free();
			this.outNetBuffer = null;
		}

	}
}