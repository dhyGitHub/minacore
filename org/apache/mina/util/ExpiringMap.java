/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.mina.util.ExpirationListener;

public class ExpiringMap<K, V> implements Map<K, V> {
	public static final int DEFAULT_TIME_TO_LIVE = 60;
	public static final int DEFAULT_EXPIRATION_INTERVAL = 1;
	private static volatile int expirerCount = 1;
	private final ConcurrentHashMap<K, ExpiringMap<K, V>.ExpiringObject> delegate;
	private final CopyOnWriteArrayList<ExpirationListener<V>> expirationListeners;
	private final ExpiringMap<K, V>.Expirer expirer;

	public ExpiringMap() {
		this(60, 1);
	}

	public ExpiringMap(int timeToLive) {
		this(timeToLive, 1);
	}

	public ExpiringMap(int timeToLive, int expirationInterval) {
		this(new ConcurrentHashMap(), new CopyOnWriteArrayList(), timeToLive, expirationInterval);
	}

	private ExpiringMap(ConcurrentHashMap<K, ExpiringMap<K, V>.ExpiringObject> delegate,
			CopyOnWriteArrayList<ExpirationListener<V>> expirationListeners, int timeToLive, int expirationInterval) {
		this.delegate = delegate;
		this.expirationListeners = expirationListeners;
		this.expirer = new ExpiringMap.Expirer();
		this.expirer.setTimeToLive((long) timeToLive);
		this.expirer.setExpirationInterval((long) expirationInterval);
	}

	public V put(K key, V value) {
		ExpiringMap.ExpiringObject answer = (ExpiringMap.ExpiringObject) this.delegate.put(key,
				new ExpiringMap.ExpiringObject(key, value, System.currentTimeMillis()));
		return answer == null ? null : answer.getValue();
	}

	public V get(Object key) {
		ExpiringMap.ExpiringObject object = (ExpiringMap.ExpiringObject) this.delegate.get(key);
		if (object != null) {
			object.setLastAccessTime(System.currentTimeMillis());
			return object.getValue();
		} else {
			return null;
		}
	}

	public V remove(Object key) {
		ExpiringMap.ExpiringObject answer = (ExpiringMap.ExpiringObject) this.delegate.remove(key);
		return answer == null ? null : answer.getValue();
	}

	public boolean containsKey(Object key) {
		return this.delegate.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return this.delegate.containsValue(value);
	}

	public int size() {
		return this.delegate.size();
	}

	public boolean isEmpty() {
		return this.delegate.isEmpty();
	}

	public void clear() {
		this.delegate.clear();
	}

	public int hashCode() {
		return this.delegate.hashCode();
	}

	public Set<K> keySet() {
		return this.delegate.keySet();
	}

	public boolean equals(Object obj) {
		return this.delegate.equals(obj);
	}

	public void putAll(Map<? extends K, ? extends V> inMap) {
		Iterator i$ = inMap.entrySet().iterator();

		while (i$.hasNext()) {
			Entry e = (Entry) i$.next();
			this.put(e.getKey(), e.getValue());
		}

	}

	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	public Set<Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	public void addExpirationListener(ExpirationListener<V> listener) {
		this.expirationListeners.add(listener);
	}

	public void removeExpirationListener(ExpirationListener<V> listener) {
		this.expirationListeners.remove(listener);
	}

	public ExpiringMap<K, V>.Expirer getExpirer() {
		return this.expirer;
	}

	public int getExpirationInterval() {
		return this.expirer.getExpirationInterval();
	}

	public int getTimeToLive() {
		return this.expirer.getTimeToLive();
	}

	public void setExpirationInterval(int expirationInterval) {
		this.expirer.setExpirationInterval((long) expirationInterval);
	}

	public void setTimeToLive(int timeToLive) {
		this.expirer.setTimeToLive((long) timeToLive);
	}

	public class Expirer implements Runnable {
		private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
		private long timeToLiveMillis;
		private long expirationIntervalMillis;
		private boolean running = false;
		private final Thread expirerThread;

		public Expirer() {
			this.expirerThread = new Thread(this, "ExpiringMapExpirer-" + ExpiringMap.expirerCount++);
			this.expirerThread.setDaemon(true);
		}

		public void run() {
			while (this.running) {
				this.processExpires();

				try {
					Thread.sleep(this.expirationIntervalMillis);
				} catch (InterruptedException arg1) {
					;
				}
			}

		}

		private void processExpires() {
			long timeNow = System.currentTimeMillis();
			Iterator i$ = ExpiringMap.this.delegate.values().iterator();

			while (true) {
				ExpiringMap.ExpiringObject o;
				long timeIdle;
				do {
					do {
						if (!i$.hasNext()) {
							return;
						}

						o = (ExpiringMap.ExpiringObject) i$.next();
					} while (this.timeToLiveMillis <= 0L);

					timeIdle = timeNow - o.getLastAccessTime();
				} while (timeIdle < this.timeToLiveMillis);

				ExpiringMap.this.delegate.remove(o.getKey());
				Iterator i$1 = ExpiringMap.this.expirationListeners.iterator();

				while (i$1.hasNext()) {
					ExpirationListener listener = (ExpirationListener) i$1.next();
					listener.expired(o.getValue());
				}
			}
		}

		public void startExpiring() {
			this.stateLock.writeLock().lock();

			try {
				if (!this.running) {
					this.running = true;
					this.expirerThread.start();
				}
			} finally {
				this.stateLock.writeLock().unlock();
			}

		}

		public void startExpiringIfNotStarted() {
			this.stateLock.readLock().lock();

			try {
				if (this.running) {
					return;
				}
			} finally {
				this.stateLock.readLock().unlock();
			}

			this.stateLock.writeLock().lock();

			try {
				if (!this.running) {
					this.running = true;
					this.expirerThread.start();
				}
			} finally {
				this.stateLock.writeLock().unlock();
			}

		}

		public void stopExpiring() {
			this.stateLock.writeLock().lock();

			try {
				if (this.running) {
					this.running = false;
					this.expirerThread.interrupt();
				}
			} finally {
				this.stateLock.writeLock().unlock();
			}

		}

		public boolean isRunning() {
			this.stateLock.readLock().lock();

			boolean arg0;
			try {
				arg0 = this.running;
			} finally {
				this.stateLock.readLock().unlock();
			}

			return arg0;
		}

		public int getTimeToLive() {
			this.stateLock.readLock().lock();

			int arg0;
			try {
				arg0 = (int) this.timeToLiveMillis / 1000;
			} finally {
				this.stateLock.readLock().unlock();
			}

			return arg0;
		}

		public void setTimeToLive(long timeToLive) {
			this.stateLock.writeLock().lock();

			try {
				this.timeToLiveMillis = timeToLive * 1000L;
			} finally {
				this.stateLock.writeLock().unlock();
			}

		}

		public int getExpirationInterval() {
			this.stateLock.readLock().lock();

			int arg0;
			try {
				arg0 = (int) this.expirationIntervalMillis / 1000;
			} finally {
				this.stateLock.readLock().unlock();
			}

			return arg0;
		}

		public void setExpirationInterval(long expirationInterval) {
			this.stateLock.writeLock().lock();

			try {
				this.expirationIntervalMillis = expirationInterval * 1000L;
			} finally {
				this.stateLock.writeLock().unlock();
			}

		}
	}

	private class ExpiringObject {
		private K key;
		private V value;
		private long lastAccessTime;
		private final ReadWriteLock lastAccessTimeLock = new ReentrantReadWriteLock();

		ExpiringObject(K arg0, V key, long value) {
			if (value == null) {
				throw new IllegalArgumentException("An expiring object cannot be null.");
			} else {
				this.key = key;
				this.value = value;
				this.lastAccessTime = lastAccessTime;
			}
		}

		public long getLastAccessTime() {
			this.lastAccessTimeLock.readLock().lock();

			long arg0;
			try {
				arg0 = this.lastAccessTime;
			} finally {
				this.lastAccessTimeLock.readLock().unlock();
			}

			return arg0;
		}

		public void setLastAccessTime(long lastAccessTime) {
			this.lastAccessTimeLock.writeLock().lock();

			try {
				this.lastAccessTime = lastAccessTime;
			} finally {
				this.lastAccessTimeLock.writeLock().unlock();
			}

		}

		public K getKey() {
			return this.key;
		}

		public V getValue() {
			return this.value;
		}

		public boolean equals(Object obj) {
			return this.value.equals(obj);
		}

		public int hashCode() {
			return this.value.hashCode();
		}
	}
}