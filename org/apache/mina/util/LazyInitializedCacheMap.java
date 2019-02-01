/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.mina.util.LazyInitializer;

public class LazyInitializedCacheMap<K, V> implements Map<K, V> {
	private ConcurrentMap<K, LazyInitializer<V>> cache;

	public LazyInitializedCacheMap() {
		this.cache = new ConcurrentHashMap();
	}

	public LazyInitializedCacheMap(ConcurrentHashMap<K, LazyInitializer<V>> map) {
		this.cache = map;
	}

	public V get(Object key) {
		LazyInitializer c = (LazyInitializer) this.cache.get(key);
		return c != null ? c.get() : null;
	}

	public V remove(Object key) {
		LazyInitializer c = (LazyInitializer) this.cache.remove(key);
		return c != null ? c.get() : null;
	}

	public V putIfAbsent(K key, LazyInitializer<V> value) {
		LazyInitializer v = (LazyInitializer) this.cache.get(key);
		if (v == null) {
			v = (LazyInitializer) this.cache.putIfAbsent(key, value);
			if (v == null) {
				return value.get();
			}
		}

		return v.get();
	}

	public V put(K key, V value) {
		LazyInitializer c = (LazyInitializer) this.cache.put(key, new LazyInitializedCacheMap.NoopInitializer(value));
		return c != null ? c.get() : null;
	}

	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	public Set<Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		Iterator i$ = m.entrySet().iterator();

		while (i$.hasNext()) {
			Entry e = (Entry) i$.next();
			this.cache.put(e.getKey(), new LazyInitializedCacheMap.NoopInitializer(e.getValue()));
		}

	}

	public Collection<LazyInitializer<V>> getValues() {
		return this.cache.values();
	}

	public void clear() {
		this.cache.clear();
	}

	public boolean containsKey(Object key) {
		return this.cache.containsKey(key);
	}

	public boolean isEmpty() {
		return this.cache.isEmpty();
	}

	public Set<K> keySet() {
		return this.cache.keySet();
	}

	public int size() {
		return this.cache.size();
	}

	public class NoopInitializer extends LazyInitializer<V> {
		private V value;

		public NoopInitializer(V arg0) {
			this.value = value;
		}

		public V init() {
			return this.value;
		}
	}
}