/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket.nio;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.service.AbstractIoAcceptor;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.service.AbstractIoAcceptor.AcceptorOperationFuture;
import org.apache.mina.core.service.AbstractIoService.ServiceOperationFuture;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.ExpiringSessionRecycler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.core.session.IoSessionRecycler;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.transport.socket.DatagramAcceptor;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.DefaultDatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramSession;
import org.apache.mina.transport.socket.nio.NioDatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioSession;
import org.apache.mina.util.ExceptionMonitor;

public final class NioDatagramAcceptor extends AbstractIoAcceptor implements DatagramAcceptor, IoProcessor<NioSession> {
	private static final IoSessionRecycler DEFAULT_RECYCLER = new ExpiringSessionRecycler();
	private static final long SELECT_TIMEOUT = 1000L;
	private final Semaphore lock;
	private final Queue<AcceptorOperationFuture> registerQueue;
	private final Queue<AcceptorOperationFuture> cancelQueue;
	private final Queue<NioSession> flushingSessions;
	private final Map<SocketAddress, DatagramChannel> boundHandles;
	private IoSessionRecycler sessionRecycler;
	private final ServiceOperationFuture disposalFuture;
	private volatile boolean selectable;
	private NioDatagramAcceptor.Acceptor acceptor;
	private long lastIdleCheckTime;
	private volatile Selector selector;

	public NioDatagramAcceptor() {
		this(new DefaultDatagramSessionConfig(), (Executor) null);
	}

	public NioDatagramAcceptor(Executor executor) {
		this(new DefaultDatagramSessionConfig(), executor);
	}

	private NioDatagramAcceptor(IoSessionConfig sessionConfig, Executor executor) {
		super(sessionConfig, executor);
		this.lock = new Semaphore(1);
		this.registerQueue = new ConcurrentLinkedQueue();
		this.cancelQueue = new ConcurrentLinkedQueue();
		this.flushingSessions = new ConcurrentLinkedQueue();
		this.boundHandles = Collections.synchronizedMap(new HashMap());
		this.sessionRecycler = DEFAULT_RECYCLER;
		this.disposalFuture = new ServiceOperationFuture();

		try {
			this.init();
			this.selectable = true;
		} catch (RuntimeException arg11) {
			throw arg11;
		} catch (Exception arg12) {
			throw new RuntimeIoException("Failed to initialize.", arg12);
		} finally {
			if (!this.selectable) {
				try {
					this.destroy();
				} catch (Exception arg10) {
					ExceptionMonitor.getInstance().exceptionCaught(arg10);
				}
			}

		}

	}

	private int registerHandles() {
		while (true) {
			AcceptorOperationFuture req = (AcceptorOperationFuture) this.registerQueue.poll();
			if (req == null) {
				return 0;
			}

			HashMap newHandles = new HashMap();
			List localAddresses = req.getLocalAddresses();
			boolean arg16 = false;

			DatagramChannel e;
			int i$2;
			label187: {
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
					this.getListeners().fireServiceActivated();
					req.setDone();
					i$2 = newHandles.size();
					arg16 = false;
					break label187;
				} catch (Exception arg20) {
					req.setException(arg20);
					arg16 = false;
				} finally {
					if (arg16) {
						if (req.getException() != null) {
							Iterator i$1 = newHandles.values().iterator();

							while (i$1.hasNext()) {
								DatagramChannel handle1 = (DatagramChannel) i$1.next();

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

				if (req.getException() == null) {
					continue;
				}

				i$ = newHandles.values().iterator();

				while (i$.hasNext()) {
					DatagramChannel handle2 = (DatagramChannel) i$.next();

					try {
						this.close(handle2);
					} catch (Exception arg18) {
						ExceptionMonitor.getInstance().exceptionCaught(arg18);
					}
				}

				this.wakeup();
				continue;
			}

			if (req.getException() != null) {
				Iterator handle3 = newHandles.values().iterator();

				while (handle3.hasNext()) {
					e = (DatagramChannel) handle3.next();

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

	private void processReadySessions(Set<SelectionKey> handles) {
		Iterator iterator = handles.iterator();

		while (iterator.hasNext()) {
			SelectionKey key = (SelectionKey) iterator.next();
			DatagramChannel handle = (DatagramChannel) key.channel();
			iterator.remove();

			try {
				if (key.isValid() && key.isReadable()) {
					this.readHandle(handle);
				}

				if (key.isValid() && key.isWritable()) {
					Iterator e = this.getManagedSessions().values().iterator();

					while (e.hasNext()) {
						IoSession session = (IoSession) e.next();
						this.scheduleFlush((NioSession) session);
					}
				}
			} catch (Exception arg6) {
				ExceptionMonitor.getInstance().exceptionCaught(arg6);
			}
		}

	}

	private boolean scheduleFlush(NioSession session) {
		if (session.setScheduledForFlush(true)) {
			this.flushingSessions.add(session);
			return true;
		} else {
			return false;
		}
	}

	private void readHandle(DatagramChannel handle) throws Exception {
		IoBuffer readBuf = IoBuffer.allocate(this.getSessionConfig().getReadBufferSize());
		SocketAddress remoteAddress = this.receive(handle, readBuf);
		if (remoteAddress != null) {
			IoSession session = this.newSessionWithoutLock(remoteAddress, this.localAddress(handle));
			readBuf.flip();
			session.getFilterChain().fireMessageReceived(readBuf);
		}

	}

	private IoSession newSessionWithoutLock(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
		DatagramChannel handle = (DatagramChannel) this.boundHandles.get(localAddress);
		if (handle == null) {
			throw new IllegalArgumentException("Unknown local address: " + localAddress);
		} else {
			IoSessionRecycler e = this.sessionRecycler;
			NioSession session1;
			synchronized (this.sessionRecycler) {
				IoSession session = this.sessionRecycler.recycle(remoteAddress);
				if (session != null) {
					return session;
				}

				NioSession newSession = this.newSession(this, handle, remoteAddress);
				this.getSessionRecycler().put(newSession);
				session1 = newSession;
			}

			this.initSession(session1, (IoFuture) null, (IoSessionInitializer) null);

			try {
				this.getFilterChainBuilder().buildFilterChain(session1.getFilterChain());
				this.getListeners().fireSessionCreated(session1);
			} catch (Exception arg7) {
				ExceptionMonitor.getInstance().exceptionCaught(arg7);
			}

			return session1;
		}
	}

	private void flushSessions(long currentTime) {
		while (true) {
			NioSession session = (NioSession) this.flushingSessions.poll();
			if (session == null) {
				return;
			}

			session.unscheduledForFlush();

			try {
				boolean e = this.flush(session, currentTime);
				if (e && !session.getWriteRequestQueue().isEmpty(session) && !session.isScheduledForFlush()) {
					this.scheduleFlush(session);
				}
			} catch (Exception arg4) {
				session.getFilterChain().fireExceptionCaught(arg4);
			}
		}
	}

	private boolean flush(NioSession session, long currentTime) throws Exception {
		WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
		int maxWrittenBytes = session.getConfig().getMaxReadBufferSize()
				+ (session.getConfig().getMaxReadBufferSize() >>> 1);
		int writtenBytes = 0;

		while (true) {
			boolean arg10;
			try {
				WriteRequest req = session.getCurrentWriteRequest();
				if (req == null) {
					req = writeRequestQueue.poll(session);
					if (req == null) {
						this.setInterestedInWrite(session, false);
						return true;
					}

					session.setCurrentWriteRequest(req);
				}

				IoBuffer buf = (IoBuffer) req.getMessage();
				if (buf.remaining() == 0) {
					session.setCurrentWriteRequest((WriteRequest) null);
					buf.reset();
					session.getFilterChain().fireMessageSent(req);
					continue;
				}

				SocketAddress destination = req.getDestination();
				if (destination == null) {
					destination = session.getRemoteAddress();
				}

				int localWrittenBytes = this.send(session, buf, destination);
				if (localWrittenBytes != 0 && writtenBytes < maxWrittenBytes) {
					this.setInterestedInWrite(session, false);
					session.setCurrentWriteRequest((WriteRequest) null);
					writtenBytes += localWrittenBytes;
					buf.reset();
					session.getFilterChain().fireMessageSent(req);
					continue;
				}

				this.setInterestedInWrite(session, true);
				arg10 = false;
			} finally {
				session.increaseWrittenBytes(writtenBytes, currentTime);
			}

			return arg10;
		}
	}

	private int unregisterHandles() {
		int nHandles = 0;

		while (true) {
			AcceptorOperationFuture request = (AcceptorOperationFuture) this.cancelQueue.poll();
			if (request == null) {
				return nHandles;
			}

			Iterator i$ = request.getLocalAddresses().iterator();

			while (i$.hasNext()) {
				SocketAddress socketAddress = (SocketAddress) i$.next();
				DatagramChannel handle = (DatagramChannel) this.boundHandles.remove(socketAddress);
				if (handle != null) {
					try {
						this.close(handle);
						this.wakeup();
					} catch (Exception arg9) {
						ExceptionMonitor.getInstance().exceptionCaught(arg9);
					} finally {
						++nHandles;
					}
				}
			}

			request.setDone();
		}
	}

	private void notifyIdleSessions(long currentTime) {
		if (currentTime - this.lastIdleCheckTime >= 1000L) {
			this.lastIdleCheckTime = currentTime;
			AbstractIoSession.notifyIdleness(this.getListeners().getManagedSessions().values().iterator(), currentTime);
		}

	}

	private void startupAcceptor() throws InterruptedException {
		if (!this.selectable) {
			this.registerQueue.clear();
			this.cancelQueue.clear();
			this.flushingSessions.clear();
		}

		this.lock.acquire();
		if (this.acceptor == null) {
			this.acceptor = new NioDatagramAcceptor.Acceptor();
			this.executeWorker(this.acceptor);
		} else {
			this.lock.release();
		}

	}

	protected void init() throws Exception {
		this.selector = Selector.open();
	}

	public void add(NioSession session) {
	}

	protected final Set<SocketAddress> bindInternal(List<? extends SocketAddress> localAddresses) throws Exception {
		AcceptorOperationFuture request = new AcceptorOperationFuture(localAddresses);
		this.registerQueue.add(request);
		this.startupAcceptor();

		try {
			this.lock.acquire();
			Thread.sleep(10L);
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
				DatagramChannel handle = (DatagramChannel) i$.next();
				newLocalAddresses.add(this.localAddress(handle));
			}

			return newLocalAddresses;
		}
	}

	protected void close(DatagramChannel handle) throws Exception {
		SelectionKey key = handle.keyFor(this.selector);
		if (key != null) {
			key.cancel();
		}

		handle.disconnect();
		handle.close();
	}

	protected void destroy() throws Exception {
		if (this.selector != null) {
			this.selector.close();
		}

	}

	protected void dispose0() throws Exception {
		this.unbind();
		this.startupAcceptor();
		this.wakeup();
	}

	public void flush(NioSession session) {
		if (this.scheduleFlush(session)) {
			this.wakeup();
		}

	}

	public InetSocketAddress getDefaultLocalAddress() {
		return (InetSocketAddress) super.getDefaultLocalAddress();
	}

	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) super.getLocalAddress();
	}

	public DatagramSessionConfig getSessionConfig() {
		return (DatagramSessionConfig) this.sessionConfig;
	}

	public final IoSessionRecycler getSessionRecycler() {
		return this.sessionRecycler;
	}

	public TransportMetadata getTransportMetadata() {
		return NioDatagramSession.METADATA;
	}

	protected boolean isReadable(DatagramChannel handle) {
		SelectionKey key = handle.keyFor(this.selector);
		return key != null && key.isValid() ? key.isReadable() : false;
	}

	protected boolean isWritable(DatagramChannel handle) {
		SelectionKey key = handle.keyFor(this.selector);
		return key != null && key.isValid() ? key.isWritable() : false;
	}

	protected SocketAddress localAddress(DatagramChannel handle) throws Exception {
		InetSocketAddress inetSocketAddress = (InetSocketAddress) handle.socket().getLocalSocketAddress();
		InetAddress inetAddress = inetSocketAddress.getAddress();
		if (inetAddress instanceof Inet6Address && ((Inet6Address) inetAddress).isIPv4CompatibleAddress()) {
			byte[] ipV6Address = ((Inet6Address) inetAddress).getAddress();
			byte[] ipV4Address = new byte[4];
			System.arraycopy(ipV6Address, 12, ipV4Address, 0, 4);
			InetAddress inet4Adress = Inet4Address.getByAddress(ipV4Address);
			return new InetSocketAddress(inet4Adress, inetSocketAddress.getPort());
		} else {
			return inetSocketAddress;
		}
	}

	protected NioSession newSession(IoProcessor<NioSession> processor, DatagramChannel handle,
			SocketAddress remoteAddress) {
		SelectionKey key = handle.keyFor(this.selector);
		if (key != null && key.isValid()) {
			NioDatagramSession newSession = new NioDatagramSession(this, handle, processor, remoteAddress);
			newSession.setSelectionKey(key);
			return newSession;
		} else {
			return null;
		}
	}

	public final IoSession newSession(SocketAddress remoteAddress, SocketAddress localAddress) {
		if (this.isDisposing()) {
			throw new IllegalStateException("The Acceptor is being disposed.");
		} else if (remoteAddress == null) {
			throw new IllegalArgumentException("remoteAddress");
		} else {
			Object arg2 = this.bindLock;
			synchronized (this.bindLock) {
				if (!this.isActive()) {
					throw new IllegalStateException("Can\'t create a session from a unbound service.");
				} else {
					IoSession arg9999;
					try {
						arg9999 = this.newSessionWithoutLock(remoteAddress, localAddress);
					} catch (RuntimeException arg5) {
						throw arg5;
					} catch (Error arg6) {
						throw arg6;
					} catch (Exception arg7) {
						throw new RuntimeIoException("Failed to create a session.", arg7);
					}

					return arg9999;
				}
			}
		}
	}

	protected DatagramChannel open(SocketAddress localAddress) throws Exception {
		DatagramChannel ch = DatagramChannel.open();
		boolean success = false;

		try {
			(new NioDatagramSessionConfig(ch)).setAll(this.getSessionConfig());
			ch.configureBlocking(false);

			try {
				ch.socket().bind(localAddress);
			} catch (IOException arg9) {
				String newMessage = "Error while binding on " + localAddress + "\n" + "original message : "
						+ arg9.getMessage();
				IOException e = new IOException(newMessage);
				e.initCause(arg9.getCause());
				ch.close();
				throw e;
			}

			ch.register(this.selector, 1);
			success = true;
		} finally {
			if (!success) {
				this.close(ch);
			}

		}

		return ch;
	}

	protected SocketAddress receive(DatagramChannel handle, IoBuffer buffer) throws Exception {
		return handle.receive(buffer.buf());
	}

	public void remove(NioSession session) {
		this.getSessionRecycler().remove(session);
		this.getListeners().fireSessionDestroyed(session);
	}

	protected int select() throws Exception {
		return this.selector.select();
	}

	protected int select(long timeout) throws Exception {
		return this.selector.select(timeout);
	}

	protected Set<SelectionKey> selectedHandles() {
		return this.selector.selectedKeys();
	}

	protected int send(NioSession session, IoBuffer buffer, SocketAddress remoteAddress) throws Exception {
		return ((DatagramChannel) session.getChannel()).send(buffer.buf(), remoteAddress);
	}

	public void setDefaultLocalAddress(InetSocketAddress localAddress) {
		this.setDefaultLocalAddress(localAddress);
	}

	protected void setInterestedInWrite(NioSession session, boolean isInterested) throws Exception {
		SelectionKey key = session.getSelectionKey();
		if (key != null) {
			int newInterestOps = key.interestOps();
			if (isInterested) {
				newInterestOps |= 4;
			} else {
				newInterestOps &= -5;
			}

			key.interestOps(newInterestOps);
		}
	}

	public final void setSessionRecycler(IoSessionRecycler sessionRecycler) {
		Object arg1 = this.bindLock;
		synchronized (this.bindLock) {
			if (this.isActive()) {
				throw new IllegalStateException("sessionRecycler can\'t be set while the acceptor is bound.");
			} else {
				if (sessionRecycler == null) {
					sessionRecycler = DEFAULT_RECYCLER;
				}

				this.sessionRecycler = sessionRecycler;
			}
		}
	}

	protected final void unbind0(List<? extends SocketAddress> localAddresses) throws Exception {
		AcceptorOperationFuture request = new AcceptorOperationFuture(localAddresses);
		this.cancelQueue.add(request);
		this.startupAcceptor();
		this.wakeup();
		request.awaitUninterruptibly();
		if (request.getException() != null) {
			throw request.getException();
		}
	}

	public void updateTrafficControl(NioSession session) {
		throw new UnsupportedOperationException();
	}

	protected void wakeup() {
		this.selector.wakeup();
	}

	public void write(NioSession session, WriteRequest writeRequest) {
		long currentTime = System.currentTimeMillis();
		WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
		int maxWrittenBytes = session.getConfig().getMaxReadBufferSize()
				+ (session.getConfig().getMaxReadBufferSize() >>> 1);
		int writtenBytes = 0;
		IoBuffer buf = (IoBuffer) writeRequest.getMessage();
		if (buf.remaining() == 0) {
			session.setCurrentWriteRequest((WriteRequest) null);
			buf.reset();
			session.getFilterChain().fireMessageSent(writeRequest);
		} else {
			try {
				while (true) {
					if (writeRequest == null) {
						writeRequest = writeRequestQueue.poll(session);
						if (writeRequest == null) {
							this.setInterestedInWrite(session, false);
							break;
						}

						session.setCurrentWriteRequest(writeRequest);
					}

					buf = (IoBuffer) writeRequest.getMessage();
					if (buf.remaining() == 0) {
						session.setCurrentWriteRequest((WriteRequest) null);
						buf.reset();
						session.getFilterChain().fireMessageSent(writeRequest);
					} else {
						SocketAddress e = writeRequest.getDestination();
						if (e == null) {
							e = session.getRemoteAddress();
						}

						int localWrittenBytes = this.send(session, buf, e);
						if (localWrittenBytes != 0 && writtenBytes < maxWrittenBytes) {
							this.setInterestedInWrite(session, false);
							session.setCurrentWriteRequest((WriteRequest) null);
							writtenBytes += localWrittenBytes;
							buf.reset();
							session.getFilterChain().fireMessageSent(writeRequest);
							break;
						}

						this.setInterestedInWrite(session, true);
						session.getWriteRequestQueue().offer(session, writeRequest);
						this.scheduleFlush(session);
					}
				}
			} catch (Exception arg13) {
				session.getFilterChain().fireExceptionCaught(arg13);
			} finally {
				session.increaseWrittenBytes(writtenBytes, currentTime);
			}

		}
	}

	private class Acceptor implements Runnable {
		private Acceptor() {
		}

		public void run() {
			int nHandles = 0;
			NioDatagramAcceptor.this.lastIdleCheckTime = System.currentTimeMillis();
			NioDatagramAcceptor.this.lock.release();

			while (NioDatagramAcceptor.this.selectable) {
				try {
					int e = NioDatagramAcceptor.this.select(1000L);
					nHandles += NioDatagramAcceptor.this.registerHandles();
					if (nHandles == 0) {
						try {
							NioDatagramAcceptor.this.lock.acquire();
							if (NioDatagramAcceptor.this.registerQueue.isEmpty()
									&& NioDatagramAcceptor.this.cancelQueue.isEmpty()) {
								NioDatagramAcceptor.this.acceptor = null;
								break;
							}
						} finally {
							NioDatagramAcceptor.this.lock.release();
						}
					}

					if (e > 0) {
						NioDatagramAcceptor.this.processReadySessions(NioDatagramAcceptor.this.selectedHandles());
					}

					long e1 = System.currentTimeMillis();
					NioDatagramAcceptor.this.flushSessions(e1);
					nHandles -= NioDatagramAcceptor.this.unregisterHandles();
					NioDatagramAcceptor.this.notifyIdleSessions(e1);
				} catch (ClosedSelectorException arg21) {
					ExceptionMonitor.getInstance().exceptionCaught(arg21);
					break;
				} catch (Exception arg22) {
					ExceptionMonitor.getInstance().exceptionCaught(arg22);

					try {
						Thread.sleep(1000L);
					} catch (InterruptedException arg19) {
						;
					}
				}
			}

			if (NioDatagramAcceptor.this.selectable && NioDatagramAcceptor.this.isDisposing()) {
				NioDatagramAcceptor.this.selectable = false;

				try {
					NioDatagramAcceptor.this.destroy();
				} catch (Exception arg17) {
					ExceptionMonitor.getInstance().exceptionCaught(arg17);
				} finally {
					NioDatagramAcceptor.this.disposalFuture.setValue(Boolean.valueOf(true));
				}
			}

		}
	}
}