/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.ssl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestWrapper;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.mina.filter.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslFilter extends IoFilterAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SslFilter.class);
	public static final AttributeKey SSL_SESSION = new AttributeKey(SslFilter.class, "session");
	public static final AttributeKey DISABLE_ENCRYPTION_ONCE = new AttributeKey(SslFilter.class, "disableOnce");
	public static final AttributeKey USE_NOTIFICATION = new AttributeKey(SslFilter.class, "useNotification");
	public static final AttributeKey PEER_ADDRESS = new AttributeKey(SslFilter.class, "peerAddress");
	public static final SslFilter.SslFilterMessage SESSION_SECURED = new SslFilter.SslFilterMessage("SESSION_SECURED",
			null);
	public static final SslFilter.SslFilterMessage SESSION_UNSECURED = new SslFilter.SslFilterMessage(
			"SESSION_UNSECURED", null);
	private static final AttributeKey NEXT_FILTER = new AttributeKey(SslFilter.class, "nextFilter");
	private static final AttributeKey SSL_HANDLER = new AttributeKey(SslFilter.class, "handler");
	final SSLContext sslContext;
	private final boolean autoStart;
	private static final boolean START_HANDSHAKE = true;
	private boolean client;
	private boolean needClientAuth;
	private boolean wantClientAuth;
	private String[] enabledCipherSuites;
	private String[] enabledProtocols;

	public SslFilter(SSLContext sslContext) {
		this(sslContext, true);
	}

	public SslFilter(SSLContext sslContext, boolean autoStart) {
		if (sslContext == null) {
			throw new IllegalArgumentException("sslContext");
		} else {
			this.sslContext = sslContext;
			this.autoStart = autoStart;
		}
	}

	public SSLSession getSslSession(IoSession session) {
		return (SSLSession) session.getAttribute(SSL_SESSION);
	}

	public boolean startSsl(IoSession session) throws SSLException {
		SslHandler sslHandler = this.getSslSessionHandler(session);

		try {
			boolean started;
			synchronized (sslHandler) {
				if (sslHandler.isOutboundDone()) {
					NextFilter nextFilter = (NextFilter) session.getAttribute(NEXT_FILTER);
					sslHandler.destroy();
					sslHandler.init();
					sslHandler.handshake(nextFilter);
					started = true;
				} else {
					started = false;
				}
			}

			sslHandler.flushScheduledEvents();
			return started;
		} catch (SSLException arg7) {
			sslHandler.release();
			throw arg7;
		}
	}

	String getSessionInfo(IoSession session) {
		StringBuilder sb = new StringBuilder();
		if (session.getService() instanceof IoAcceptor) {
			sb.append("Session Server");
		} else {
			sb.append("Session Client");
		}

		sb.append('[').append(session.getId()).append(']');
		SslHandler sslHandler = (SslHandler) session.getAttribute(SSL_HANDLER);
		if (sslHandler == null) {
			sb.append("(no sslEngine)");
		} else if (this.isSslStarted(session)) {
			if (sslHandler.isHandshakeComplete()) {
				sb.append("(SSL)");
			} else {
				sb.append("(ssl...)");
			}
		}

		return sb.toString();
	}

	public boolean isSslStarted(IoSession session) {
		SslHandler sslHandler = (SslHandler) session.getAttribute(SSL_HANDLER);
		if (sslHandler == null) {
			return false;
		} else {
			synchronized (sslHandler) {
				return !sslHandler.isOutboundDone();
			}
		}
	}

	public WriteFuture stopSsl(IoSession session) throws SSLException {
		SslHandler sslHandler = this.getSslSessionHandler(session);
		NextFilter nextFilter = (NextFilter) session.getAttribute(NEXT_FILTER);

		try {
			WriteFuture future;
			synchronized (sslHandler) {
				future = this.initiateClosure(nextFilter, session);
			}

			sslHandler.flushScheduledEvents();
			return future;
		} catch (SSLException arg7) {
			sslHandler.release();
			throw arg7;
		}
	}

	public boolean isUseClientMode() {
		return this.client;
	}

	public void setUseClientMode(boolean clientMode) {
		this.client = clientMode;
	}

	public boolean isNeedClientAuth() {
		return this.needClientAuth;
	}

	public void setNeedClientAuth(boolean needClientAuth) {
		this.needClientAuth = needClientAuth;
	}

	public boolean isWantClientAuth() {
		return this.wantClientAuth;
	}

	public void setWantClientAuth(boolean wantClientAuth) {
		this.wantClientAuth = wantClientAuth;
	}

	public String[] getEnabledCipherSuites() {
		return this.enabledCipherSuites;
	}

	public void setEnabledCipherSuites(String[] cipherSuites) {
		this.enabledCipherSuites = cipherSuites;
	}

	public String[] getEnabledProtocols() {
		return this.enabledProtocols;
	}

	public void setEnabledProtocols(String[] protocols) {
		this.enabledProtocols = protocols;
	}

	public void onPreAdd(IoFilterChain parent, String name, NextFilter nextFilter) throws SSLException {
		if (parent.contains(SslFilter.class)) {
			String session1 = "Only one SSL filter is permitted in a chain.";
			LOGGER.error(session1);
			throw new IllegalStateException(session1);
		} else {
			LOGGER.debug("Adding the SSL Filter {} to the chain", name);
			IoSession session = parent.getSession();
			session.setAttribute(NEXT_FILTER, nextFilter);
			SslHandler sslHandler = new SslHandler(this, session);
			if (this.enabledCipherSuites == null || this.enabledCipherSuites.length == 0) {
				this.enabledCipherSuites = this.sslContext.getServerSocketFactory().getSupportedCipherSuites();
			}

			sslHandler.init();
			session.setAttribute(SSL_HANDLER, sslHandler);
		}
	}

	public void onPostAdd(IoFilterChain parent, String name, NextFilter nextFilter) throws SSLException {
		if (this.autoStart) {
			this.initiateHandshake(nextFilter, parent.getSession());
		}

	}

	public void onPreRemove(IoFilterChain parent, String name, NextFilter nextFilter) throws SSLException {
		IoSession session = parent.getSession();
		this.stopSsl(session);
		session.removeAttribute(NEXT_FILTER);
		session.removeAttribute(SSL_HANDLER);
	}

	public void sessionClosed(NextFilter nextFilter, IoSession session) throws SSLException {
		SslHandler sslHandler = this.getSslSessionHandler(session);

		try {
			synchronized (sslHandler) {
				sslHandler.destroy();
			}
		} finally {
			nextFilter.sessionClosed(session);
		}

	}

	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws SSLException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("{}: Message received : {}", this.getSessionInfo(session), message);
		}

		SslHandler sslHandler = this.getSslSessionHandler(session);
		synchronized (sslHandler) {
			if (!this.isSslStarted(session) && sslHandler.isInboundDone()) {
				sslHandler.scheduleMessageReceived(nextFilter, message);
			} else {
				IoBuffer buf = (IoBuffer) message;

				try {
					sslHandler.messageReceived(nextFilter, buf.buf());
					this.handleSslData(nextFilter, sslHandler);
					if (sslHandler.isInboundDone()) {
						if (sslHandler.isOutboundDone()) {
							sslHandler.destroy();
						} else {
							this.initiateClosure(nextFilter, session);
						}

						if (buf.hasRemaining()) {
							sslHandler.scheduleMessageReceived(nextFilter, buf);
						}
					}
				} catch (SSLException arg9) {
					Object ssle = arg9;
					if (!sslHandler.isHandshakeComplete()) {
						SSLHandshakeException newSsle = new SSLHandshakeException("SSL handshake failed.");
						newSsle.initCause(arg9);
						ssle = newSsle;
						session.closeNow();
					} else {
						sslHandler.release();
					}

					throw ssle;
				}
			}
		}

		sslHandler.flushScheduledEvents();
	}

	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) {
		if (writeRequest instanceof SslFilter.EncryptedWriteRequest) {
			SslFilter.EncryptedWriteRequest wrappedRequest = (SslFilter.EncryptedWriteRequest) writeRequest;
			nextFilter.messageSent(session, wrappedRequest.getParentRequest());
		}

	}

	public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
		if (cause instanceof WriteToClosedSessionException) {
			WriteToClosedSessionException e = (WriteToClosedSessionException) cause;
			List failedRequests = e.getRequests();
			boolean containsCloseNotify = false;
			Iterator newFailedRequests = failedRequests.iterator();

			while (newFailedRequests.hasNext()) {
				WriteRequest i$ = (WriteRequest) newFailedRequests.next();
				if (this.isCloseNotify(i$.getMessage())) {
					containsCloseNotify = true;
					break;
				}
			}

			if (containsCloseNotify) {
				if (failedRequests.size() == 1) {
					return;
				}

				ArrayList newFailedRequests1 = new ArrayList(failedRequests.size() - 1);
				Iterator i$1 = failedRequests.iterator();

				while (i$1.hasNext()) {
					WriteRequest r = (WriteRequest) i$1.next();
					if (!this.isCloseNotify(r.getMessage())) {
						newFailedRequests1.add(r);
					}
				}

				if (newFailedRequests1.isEmpty()) {
					return;
				}

				cause = new WriteToClosedSessionException(newFailedRequests1, ((Throwable) cause).getMessage(),
						((Throwable) cause).getCause());
			}
		}

		nextFilter.exceptionCaught(session, (Throwable) cause);
	}

	private boolean isCloseNotify(Object message) {
		if (!(message instanceof IoBuffer)) {
			return false;
		} else {
			IoBuffer buf = (IoBuffer) message;
			int offset = buf.position();
			return buf.get(offset + 0) == 21 && buf.get(offset + 1) == 3 && (buf.get(offset + 2) == 0
					|| buf.get(offset + 2) == 1 || buf.get(offset + 2) == 2 || buf.get(offset + 2) == 3)
					&& buf.get(offset + 3) == 0;
		}
	}

	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws SSLException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("{}: Writing Message : {}", this.getSessionInfo(session), writeRequest);
		}

		boolean needsFlush = true;
		SslHandler sslHandler = this.getSslSessionHandler(session);

		try {
			synchronized (sslHandler) {
				if (!this.isSslStarted(session)) {
					sslHandler.scheduleFilterWrite(nextFilter, writeRequest);
				} else if (session.containsAttribute(DISABLE_ENCRYPTION_ONCE)) {
					session.removeAttribute(DISABLE_ENCRYPTION_ONCE);
					sslHandler.scheduleFilterWrite(nextFilter, writeRequest);
				} else {
					IoBuffer buf = (IoBuffer) writeRequest.getMessage();
					if (sslHandler.isWritingEncryptedData()) {
						sslHandler.scheduleFilterWrite(nextFilter, writeRequest);
					} else if (sslHandler.isHandshakeComplete()) {
						buf.mark();
						sslHandler.encrypt(buf.buf());
						IoBuffer encryptedBuffer = sslHandler.fetchOutNetBuffer();
						sslHandler.scheduleFilterWrite(nextFilter,
								new SslFilter.EncryptedWriteRequest(writeRequest, encryptedBuffer, null));
					} else {
						if (session.isConnected()) {
							sslHandler.schedulePreHandshakeWriteRequest(nextFilter, writeRequest);
						}

						needsFlush = false;
					}
				}
			}

			if (needsFlush) {
				sslHandler.flushScheduledEvents();
			}

		} catch (SSLException arg10) {
			sslHandler.release();
			throw arg10;
		}
	}

	public void filterClose(final NextFilter nextFilter, final IoSession session) throws SSLException {
		SslHandler sslHandler = (SslHandler) session.getAttribute(SSL_HANDLER);
		if (sslHandler == null) {
			nextFilter.filterClose(session);
		} else {
			WriteFuture future = null;

			try {
				synchronized (sslHandler) {
					if (this.isSslStarted(session)) {
						future = this.initiateClosure(nextFilter, session);
						future.addListener(new IoFutureListener() {
							public void operationComplete(IoFuture future) {
								nextFilter.filterClose(session);
							}
						});
					}
				}

				sslHandler.flushScheduledEvents();
			} catch (SSLException arg11) {
				sslHandler.release();
				throw arg11;
			} finally {
				if (future == null) {
					nextFilter.filterClose(session);
				}

			}

		}
	}

	public void initiateHandshake(IoSession session) throws SSLException {
		IoFilterChain filterChain = session.getFilterChain();
		if (filterChain == null) {
			throw new SSLException("No filter chain");
		} else {
			NextFilter nextFilter = filterChain.getNextFilter(SslFilter.class);
			if (nextFilter == null) {
				throw new SSLException("No SSL next filter in the chain");
			} else {
				this.initiateHandshake(nextFilter, session);
			}
		}
	}

	private void initiateHandshake(NextFilter nextFilter, IoSession session) throws SSLException {
		LOGGER.debug("{} : Starting the first handshake", this.getSessionInfo(session));
		SslHandler sslHandler = this.getSslSessionHandler(session);

		try {
			synchronized (sslHandler) {
				sslHandler.handshake(nextFilter);
			}

			sslHandler.flushScheduledEvents();
		} catch (SSLException arg6) {
			sslHandler.release();
			throw arg6;
		}
	}

	private WriteFuture initiateClosure(NextFilter nextFilter, IoSession session) throws SSLException {
		SslHandler sslHandler = this.getSslSessionHandler(session);
		WriteFuture future = null;

		try {
			if (!sslHandler.closeOutbound()) {
				return DefaultWriteFuture.newNotWrittenFuture(session,
						new IllegalStateException("SSL session is shut down already."));
			} else {
				future = sslHandler.writeNetBuffer(nextFilter);
				if (future == null) {
					future = DefaultWriteFuture.newWrittenFuture(session);
				}

				if (sslHandler.isInboundDone()) {
					sslHandler.destroy();
				}

				if (session.containsAttribute(USE_NOTIFICATION)) {
					sslHandler.scheduleMessageReceived(nextFilter, SESSION_UNSECURED);
				}

				return future;
			}
		} catch (SSLException arg5) {
			sslHandler.release();
			throw arg5;
		}
	}

	private void handleSslData(NextFilter nextFilter, SslHandler sslHandler) throws SSLException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("{}: Processing the SSL Data ", this.getSessionInfo(sslHandler.getSession()));
		}

		if (sslHandler.isHandshakeComplete()) {
			sslHandler.flushPreHandshakeEvents();
		}

		sslHandler.writeNetBuffer(nextFilter);
		this.handleAppDataRead(nextFilter, sslHandler);
	}

	private void handleAppDataRead(NextFilter nextFilter, SslHandler sslHandler) {
		IoBuffer readBuffer = sslHandler.fetchAppBuffer();
		if (readBuffer.hasRemaining()) {
			sslHandler.scheduleMessageReceived(nextFilter, readBuffer);
		}

	}

	private SslHandler getSslSessionHandler(IoSession session) {
		SslHandler sslHandler = (SslHandler) session.getAttribute(SSL_HANDLER);
		if (sslHandler == null) {
			throw new IllegalStateException();
		} else if (sslHandler.getSslFilter() != this) {
			throw new IllegalArgumentException("Not managed by this filter.");
		} else {
			return sslHandler;
		}
	}

	private static class EncryptedWriteRequest extends WriteRequestWrapper {
		private final IoBuffer encryptedMessage;

		private EncryptedWriteRequest(WriteRequest writeRequest, IoBuffer encryptedMessage) {
			super(writeRequest);
			this.encryptedMessage = encryptedMessage;
		}

		public Object getMessage() {
			return this.encryptedMessage;
		}
	}

	public static class SslFilterMessage {
		private final String name;

		private SslFilterMessage(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}
	}
}