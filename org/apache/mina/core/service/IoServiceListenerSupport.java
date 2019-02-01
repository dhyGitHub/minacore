/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.service;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.util.ExceptionMonitor;

public class IoServiceListenerSupport {
	private final IoService service;
	private final List<IoServiceListener> listeners = new CopyOnWriteArrayList();
	private final ConcurrentMap<Long, IoSession> managedSessions = new ConcurrentHashMap();
	private final Map<Long, IoSession> readOnlyManagedSessions;
	private final AtomicBoolean activated;
	private volatile long activationTime;
	private volatile int largestManagedSessionCount;
	private AtomicLong cumulativeManagedSessionCount;

	public IoServiceListenerSupport(IoService service) {
		this.readOnlyManagedSessions = Collections.unmodifiableMap(this.managedSessions);
		this.activated = new AtomicBoolean();
		this.largestManagedSessionCount = 0;
		this.cumulativeManagedSessionCount = new AtomicLong(0L);
		if (service == null) {
			throw new IllegalArgumentException("service");
		} else {
			this.service = service;
		}
	}

	public void add(IoServiceListener listener) {
		if (listener != null) {
			this.listeners.add(listener);
		}

	}

	public void remove(IoServiceListener listener) {
		if (listener != null) {
			this.listeners.remove(listener);
		}

	}

	public long getActivationTime() {
		return this.activationTime;
	}

	public Map<Long, IoSession> getManagedSessions() {
		return this.readOnlyManagedSessions;
	}

	public int getManagedSessionCount() {
		return this.managedSessions.size();
	}

	public int getLargestManagedSessionCount() {
		return this.largestManagedSessionCount;
	}

	public long getCumulativeManagedSessionCount() {
		return this.cumulativeManagedSessionCount.get();
	}

	public boolean isActive() {
		return this.activated.get();
	}

	public void fireServiceActivated() {
		if (this.activated.compareAndSet(false, true)) {
			this.activationTime = System.currentTimeMillis();
			Iterator i$ = this.listeners.iterator();

			while (i$.hasNext()) {
				IoServiceListener listener = (IoServiceListener) i$.next();

				try {
					listener.serviceActivated(this.service);
				} catch (Exception arg3) {
					ExceptionMonitor.getInstance().exceptionCaught(arg3);
				}
			}

		}
	}

	public void fireServiceDeactivated() {
		if (this.activated.compareAndSet(true, false)) {
			try {
				Iterator i$ = this.listeners.iterator();

				while (i$.hasNext()) {
					IoServiceListener listener = (IoServiceListener) i$.next();

					try {
						listener.serviceDeactivated(this.service);
					} catch (Exception arg6) {
						ExceptionMonitor.getInstance().exceptionCaught(arg6);
					}
				}
			} finally {
				this.disconnectSessions();
			}

		}
	}

	public void fireSessionCreated(IoSession session) {
		boolean firstSession = false;
		if (session.getService() instanceof IoConnector) {
			ConcurrentMap filterChain = this.managedSessions;
			synchronized (this.managedSessions) {
				firstSession = this.managedSessions.isEmpty();
			}
		}

		if (this.managedSessions.putIfAbsent(Long.valueOf(session.getId()), session) == null) {
			if (firstSession) {
				this.fireServiceActivated();
			}

			IoFilterChain filterChain1 = session.getFilterChain();
			filterChain1.fireSessionCreated();
			filterChain1.fireSessionOpened();
			int managedSessionCount = this.managedSessions.size();
			if (managedSessionCount > this.largestManagedSessionCount) {
				this.largestManagedSessionCount = managedSessionCount;
			}

			this.cumulativeManagedSessionCount.incrementAndGet();
			Iterator i$ = this.listeners.iterator();

			while (i$.hasNext()) {
				IoServiceListener l = (IoServiceListener) i$.next();

				try {
					l.sessionCreated(session);
				} catch (Exception arg7) {
					ExceptionMonitor.getInstance().exceptionCaught(arg7);
				}
			}

		}
	}

	public void fireSessionDestroyed(IoSession session) {
		if (this.managedSessions.remove(Long.valueOf(session.getId())) != null) {
			session.getFilterChain().fireSessionClosed();
			boolean arg13 = false;

			try {
				arg13 = true;
				Iterator lastSession = this.listeners.iterator();

				while (lastSession.hasNext()) {
					IoServiceListener l = (IoServiceListener) lastSession.next();

					try {
						l.sessionDestroyed(session);
					} catch (Exception arg16) {
						ExceptionMonitor.getInstance().exceptionCaught(arg16);
					}
				}

				arg13 = false;
			} finally {
				if (arg13) {
					if (session.getService() instanceof IoConnector) {
						boolean lastSession1 = false;
						ConcurrentMap arg7 = this.managedSessions;
						synchronized (this.managedSessions) {
							lastSession1 = this.managedSessions.isEmpty();
						}

						if (lastSession1) {
							this.fireServiceDeactivated();
						}
					}

				}
			}

			if (session.getService() instanceof IoConnector) {
				boolean lastSession2 = false;
				ConcurrentMap l1 = this.managedSessions;
				synchronized (this.managedSessions) {
					lastSession2 = this.managedSessions.isEmpty();
				}

				if (lastSession2) {
					this.fireServiceDeactivated();
				}
			}

		}
	}

	private void disconnectSessions() {
		if (this.service instanceof IoAcceptor) {
			if (((IoAcceptor) this.service).isCloseOnDeactivation()) {
				Object lock = new Object();
				IoServiceListenerSupport.LockNotifyingListener listener = new IoServiceListenerSupport.LockNotifyingListener(
						lock);
				Iterator ie = this.managedSessions.values().iterator();

				while (ie.hasNext()) {
					IoSession s = (IoSession) ie.next();
					s.closeNow().addListener(listener);
				}

				try {
					synchronized (lock) {
						while (!this.managedSessions.isEmpty()) {
							lock.wait(500L);
						}
					}
				} catch (InterruptedException arg6) {
					;
				}

			}
		}
	}

	private static class LockNotifyingListener implements IoFutureListener<IoFuture> {
		private final Object lock;

		public LockNotifyingListener(Object lock) {
			this.lock = lock;
		}

		public void operationComplete(IoFuture future) {
			Object arg1 = this.lock;
			synchronized (this.lock) {
				this.lock.notifyAll();
			}
		}
	}
}