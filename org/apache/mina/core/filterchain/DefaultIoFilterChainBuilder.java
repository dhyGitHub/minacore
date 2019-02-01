/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.filterchain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.filterchain.IoFilterChain.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultIoFilterChainBuilder implements IoFilterChainBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIoFilterChainBuilder.class);
	private final List<Entry> entries;

	public DefaultIoFilterChainBuilder() {
		this.entries = new CopyOnWriteArrayList();
	}

	public DefaultIoFilterChainBuilder(DefaultIoFilterChainBuilder filterChain) {
		if (filterChain == null) {
			throw new IllegalArgumentException("filterChain");
		} else {
			this.entries = new CopyOnWriteArrayList(filterChain.entries);
		}
	}

	public Entry getEntry(String name) {
		Iterator i$ = this.entries.iterator();

		Entry e;
		do {
			if (!i$.hasNext()) {
				return null;
			}

			e = (Entry) i$.next();
		} while (!e.getName().equals(name));

		return e;
	}

	public Entry getEntry(IoFilter filter) {
		Iterator i$ = this.entries.iterator();

		Entry e;
		do {
			if (!i$.hasNext()) {
				return null;
			}

			e = (Entry) i$.next();
		} while (e.getFilter() != filter);

		return e;
	}

	public Entry getEntry(Class<? extends IoFilter> filterType) {
		Iterator i$ = this.entries.iterator();

		Entry e;
		do {
			if (!i$.hasNext()) {
				return null;
			}

			e = (Entry) i$.next();
		} while (!filterType.isAssignableFrom(e.getFilter().getClass()));

		return e;
	}

	public IoFilter get(String name) {
		Entry e = this.getEntry(name);
		return e == null ? null : e.getFilter();
	}

	public IoFilter get(Class<? extends IoFilter> filterType) {
		Entry e = this.getEntry(filterType);
		return e == null ? null : e.getFilter();
	}

	public List<Entry> getAll() {
		return new ArrayList(this.entries);
	}

	public List<Entry> getAllReversed() {
		List result = this.getAll();
		Collections.reverse(result);
		return result;
	}

	public boolean contains(String name) {
		return this.getEntry(name) != null;
	}

	public boolean contains(IoFilter filter) {
		return this.getEntry(filter) != null;
	}

	public boolean contains(Class<? extends IoFilter> filterType) {
		return this.getEntry(filterType) != null;
	}

	public synchronized void addFirst(String name, IoFilter filter) {
		this.register(0, new DefaultIoFilterChainBuilder.EntryImpl(name, filter));
	}

	public synchronized void addLast(String name, IoFilter filter) {
		this.register(this.entries.size(), new DefaultIoFilterChainBuilder.EntryImpl(name, filter));
	}

	public synchronized void addBefore(String baseName, String name, IoFilter filter) {
		this.checkBaseName(baseName);
		ListIterator i = this.entries.listIterator();

		while (i.hasNext()) {
			Entry base = (Entry) i.next();
			if (base.getName().equals(baseName)) {
				this.register(i.previousIndex(), new DefaultIoFilterChainBuilder.EntryImpl(name, filter));
				break;
			}
		}

	}

	public synchronized void addAfter(String baseName, String name, IoFilter filter) {
		this.checkBaseName(baseName);
		ListIterator i = this.entries.listIterator();

		while (i.hasNext()) {
			Entry base = (Entry) i.next();
			if (base.getName().equals(baseName)) {
				this.register(i.nextIndex(), new DefaultIoFilterChainBuilder.EntryImpl(name, filter));
				break;
			}
		}

	}

	public synchronized IoFilter remove(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name");
		} else {
			ListIterator i = this.entries.listIterator();

			Entry e;
			do {
				if (!i.hasNext()) {
					throw new IllegalArgumentException("Unknown filter name: " + name);
				}

				e = (Entry) i.next();
			} while (!e.getName().equals(name));

			this.entries.remove(i.previousIndex());
			return e.getFilter();
		}
	}

	public synchronized IoFilter remove(IoFilter filter) {
		if (filter == null) {
			throw new IllegalArgumentException("filter");
		} else {
			ListIterator i = this.entries.listIterator();

			Entry e;
			do {
				if (!i.hasNext()) {
					throw new IllegalArgumentException("Filter not found: " + filter.getClass().getName());
				}

				e = (Entry) i.next();
			} while (e.getFilter() != filter);

			this.entries.remove(i.previousIndex());
			return e.getFilter();
		}
	}

	public synchronized IoFilter remove(Class<? extends IoFilter> filterType) {
		if (filterType == null) {
			throw new IllegalArgumentException("filterType");
		} else {
			ListIterator i = this.entries.listIterator();

			Entry e;
			do {
				if (!i.hasNext()) {
					throw new IllegalArgumentException("Filter not found: " + filterType.getName());
				}

				e = (Entry) i.next();
			} while (!filterType.isAssignableFrom(e.getFilter().getClass()));

			this.entries.remove(i.previousIndex());
			return e.getFilter();
		}
	}

	public synchronized IoFilter replace(String name, IoFilter newFilter) {
		this.checkBaseName(name);
		DefaultIoFilterChainBuilder.EntryImpl e = (DefaultIoFilterChainBuilder.EntryImpl) this.getEntry(name);
		IoFilter oldFilter = e.getFilter();
		e.setFilter(newFilter);
		return oldFilter;
	}

	public synchronized void replace(IoFilter oldFilter, IoFilter newFilter) {
		Iterator i$ = this.entries.iterator();

		Entry e;
		do {
			if (!i$.hasNext()) {
				throw new IllegalArgumentException("Filter not found: " + oldFilter.getClass().getName());
			}

			e = (Entry) i$.next();
		} while (e.getFilter() != oldFilter);

		((DefaultIoFilterChainBuilder.EntryImpl) e).setFilter(newFilter);
	}

	public synchronized void replace(Class<? extends IoFilter> oldFilterType, IoFilter newFilter) {
		Iterator i$ = this.entries.iterator();

		Entry e;
		do {
			if (!i$.hasNext()) {
				throw new IllegalArgumentException("Filter not found: " + oldFilterType.getName());
			}

			e = (Entry) i$.next();
		} while (!oldFilterType.isAssignableFrom(e.getFilter().getClass()));

		((DefaultIoFilterChainBuilder.EntryImpl) e).setFilter(newFilter);
	}

	public synchronized void clear() {
		this.entries.clear();
	}

	public void setFilters(Map<String, ? extends IoFilter> filters) {
		if (filters == null) {
			throw new IllegalArgumentException("filters");
		} else if (!this.isOrderedMap(filters)) {
			throw new IllegalArgumentException(
					"filters is not an ordered map. Please try " + LinkedHashMap.class.getName() + ".");
		} else {
			LinkedHashMap filters1 = new LinkedHashMap(filters);
			Iterator i$ = filters1.entrySet().iterator();

			while (i$.hasNext()) {
				java.util.Map.Entry i$1 = (java.util.Map.Entry) i$.next();
				if (i$1.getKey() == null) {
					throw new IllegalArgumentException("filters contains a null key.");
				}

				if (i$1.getValue() == null) {
					throw new IllegalArgumentException("filters contains a null value.");
				}
			}

			synchronized (this) {
				this.clear();
				Iterator i$2 = filters1.entrySet().iterator();

				while (i$2.hasNext()) {
					java.util.Map.Entry e = (java.util.Map.Entry) i$2.next();
					this.addLast((String) e.getKey(), (IoFilter) e.getValue());
				}

			}
		}
	}

	private boolean isOrderedMap(Map<String, ? extends IoFilter> map) {
		Class mapType = map.getClass();
		if (LinkedHashMap.class.isAssignableFrom(mapType)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("{} is an ordered map.", mapType.getSimpleName());
			}

			return true;
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("{} is not a {}", mapType.getName(), LinkedHashMap.class.getSimpleName());
			}

			for (Class type = mapType; type != null; type = type.getSuperclass()) {
				Class[] newMap = type.getInterfaces();
				int rand = newMap.length;

				for (int expectedNames = 0; expectedNames < rand; ++expectedNames) {
					Class dummyFilter = newMap[expectedNames];
					if (dummyFilter.getName().endsWith("OrderedMap")) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("{} is an ordered map (guessed from that it implements OrderedMap interface.)",
									mapType.getSimpleName());
						}

						return true;
					}
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("{} doesn\'t implement OrderedMap interface.", mapType.getName());
			}

			LOGGER.debug(
					"Last resort; trying to create a new map instance with a default constructor and test if insertion order is maintained.");

			Map arg13;
			try {
				arg13 = (Map) mapType.newInstance();
			} catch (Exception arg12) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Failed to create a new map instance of \'{}\'.", mapType.getName(), arg12);
				}

				return false;
			}

			Random arg14 = new Random();
			ArrayList arg15 = new ArrayList();
			IoFilterAdapter arg16 = new IoFilterAdapter();

			for (int i = 0; i < 65536; ++i) {
				String filterName;
				do {
					filterName = String.valueOf(arg14.nextInt());
				} while (arg13.containsKey(filterName));

				arg13.put(filterName, arg16);
				arg15.add(filterName);
				Iterator it = arg15.iterator();
				Iterator i$ = arg13.keySet().iterator();

				while (i$.hasNext()) {
					Object key = i$.next();
					if (!((String) it.next()).equals(key)) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("The specified map didn\'t pass the insertion order test after {} tries.",
									Integer.valueOf(i + 1));
						}

						return false;
					}
				}
			}

			LOGGER.debug("The specified map passed the insertion order test.");
			return true;
		}
	}

	public void buildFilterChain(IoFilterChain chain) throws Exception {
		Iterator i$ = this.entries.iterator();

		while (i$.hasNext()) {
			Entry e = (Entry) i$.next();
			chain.addLast(e.getName(), e.getFilter());
		}

	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("{ ");
		boolean empty = true;
		Iterator i$ = this.entries.iterator();

		while (i$.hasNext()) {
			Entry e = (Entry) i$.next();
			if (!empty) {
				buf.append(", ");
			} else {
				empty = false;
			}

			buf.append('(');
			buf.append(e.getName());
			buf.append(':');
			buf.append(e.getFilter());
			buf.append(')');
		}

		if (empty) {
			buf.append("empty");
		}

		buf.append(" }");
		return buf.toString();
	}

	private void checkBaseName(String baseName) {
		if (baseName == null) {
			throw new IllegalArgumentException("baseName");
		} else if (!this.contains(baseName)) {
			throw new IllegalArgumentException("Unknown filter name: " + baseName);
		}
	}

	private void register(int index, Entry e) {
		if (this.contains(e.getName())) {
			throw new IllegalArgumentException("Other filter is using the same name: " + e.getName());
		} else {
			this.entries.add(index, e);
		}
	}

	private final class EntryImpl implements Entry {
		private final String name;
		private volatile IoFilter filter;

		private EntryImpl(String name, IoFilter filter) {
			if (name == null) {
				throw new IllegalArgumentException("name");
			} else if (filter == null) {
				throw new IllegalArgumentException("filter");
			} else {
				this.name = name;
				this.filter = filter;
			}
		}

		public String getName() {
			return this.name;
		}

		public IoFilter getFilter() {
			return this.filter;
		}

		private void setFilter(IoFilter filter) {
			this.filter = filter;
		}

		public NextFilter getNextFilter() {
			throw new IllegalStateException();
		}

		public String toString() {
			return "(" + this.getName() + ':' + this.filter + ')';
		}

		public void addAfter(String name, IoFilter filter) {
			DefaultIoFilterChainBuilder.this.addAfter(this.getName(), name, filter);
		}

		public void addBefore(String name, IoFilter filter) {
			DefaultIoFilterChainBuilder.this.addBefore(this.getName(), name, filter);
		}

		public void remove() {
			DefaultIoFilterChainBuilder.this.remove(this.getName());
		}

		public void replace(IoFilter newFilter) {
			DefaultIoFilterChainBuilder.this.replace(this.getName(), newFilter);
		}
	}
}