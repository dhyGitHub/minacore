/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.filterchain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterLifeCycleException;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.filterchain.IoFilterChain.Entry;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultIoFilterChain implements IoFilterChain {
	public static final AttributeKey SESSION_CREATED_FUTURE = new AttributeKey(DefaultIoFilterChain.class,
			"connectFuture");
	private final AbstractIoSession session;
	private final Map<String, Entry> name2entry = new ConcurrentHashMap();
	private final DefaultIoFilterChain.EntryImpl head;
	private final DefaultIoFilterChain.EntryImpl tail;
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIoFilterChain.class);

	public DefaultIoFilterChain(AbstractIoSession session) {
		if (session == null) {
			throw new IllegalArgumentException("session");
		} else {
			this.session = session;
			this.head = new DefaultIoFilterChain.EntryImpl((DefaultIoFilterChain.EntryImpl) null,
					(DefaultIoFilterChain.EntryImpl) null, "head", new DefaultIoFilterChain.HeadFilter());
			this.tail = new DefaultIoFilterChain.EntryImpl(this.head, (DefaultIoFilterChain.EntryImpl) null, "tail",
					new DefaultIoFilterChain.TailFilter());
			this.head.nextEntry = this.tail;
		}
	}

	public IoSession getSession() {
		return this.session;
	}

	public Entry getEntry(String name) {
		Entry e = (Entry) this.name2entry.get(name);
		return e == null ? null : e;
	}

	public Entry getEntry(IoFilter filter) {
		for (DefaultIoFilterChain.EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
			if (e.getFilter() == filter) {
				return e;
			}
		}

		return null;
	}

	public Entry getEntry(Class<? extends IoFilter> filterType) {
		for (DefaultIoFilterChain.EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
			if (filterType.isAssignableFrom(e.getFilter().getClass())) {
				return e;
			}
		}

		return null;
	}

	public IoFilter get(String name) {
		Entry e = this.getEntry(name);
		return e == null ? null : e.getFilter();
	}

	public IoFilter get(Class<? extends IoFilter> filterType) {
		Entry e = this.getEntry(filterType);
		return e == null ? null : e.getFilter();
	}

	public NextFilter getNextFilter(String name) {
		Entry e = this.getEntry(name);
		return e == null ? null : e.getNextFilter();
	}

	public NextFilter getNextFilter(IoFilter filter) {
		Entry e = this.getEntry(filter);
		return e == null ? null : e.getNextFilter();
	}

	public NextFilter getNextFilter(Class<? extends IoFilter> filterType) {
		Entry e = this.getEntry(filterType);
		return e == null ? null : e.getNextFilter();
	}

	public synchronized void addFirst(String name, IoFilter filter) {
		this.checkAddable(name);
		this.register(this.head, name, filter);
	}

	public synchronized void addLast(String name, IoFilter filter) {
		this.checkAddable(name);
		this.register(this.tail.prevEntry, name, filter);
	}

	public synchronized void addBefore(String baseName, String name, IoFilter filter) {
		DefaultIoFilterChain.EntryImpl baseEntry = this.checkOldName(baseName);
		this.checkAddable(name);
		this.register(baseEntry.prevEntry, name, filter);
	}

	public synchronized void addAfter(String baseName, String name, IoFilter filter) {
		DefaultIoFilterChain.EntryImpl baseEntry = this.checkOldName(baseName);
		this.checkAddable(name);
		this.register(baseEntry, name, filter);
	}

	public synchronized IoFilter remove(String name) {
		DefaultIoFilterChain.EntryImpl entry = this.checkOldName(name);
		this.deregister(entry);
		return entry.getFilter();
	}

	public synchronized void remove(IoFilter filter) {
		for (DefaultIoFilterChain.EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
			if (e.getFilter() == filter) {
				this.deregister(e);
				return;
			}
		}

		throw new IllegalArgumentException("Filter not found: " + filter.getClass().getName());
	}

	public synchronized IoFilter remove(Class<? extends IoFilter> filterType) {
		for (DefaultIoFilterChain.EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
			if (filterType.isAssignableFrom(e.getFilter().getClass())) {
				IoFilter oldFilter = e.getFilter();
				this.deregister(e);
				return oldFilter;
			}
		}

		throw new IllegalArgumentException("Filter not found: " + filterType.getName());
	}

	public synchronized IoFilter replace(String name, IoFilter newFilter) {
		DefaultIoFilterChain.EntryImpl entry = this.checkOldName(name);
		IoFilter oldFilter = entry.getFilter();

		try {
			newFilter.onPreAdd(this, name, entry.getNextFilter());
		} catch (Exception arg6) {
			throw new IoFilterLifeCycleException("onPreAdd(): " + name + ':' + newFilter + " in " + this.getSession(),
					arg6);
		}

		entry.setFilter(newFilter);

		try {
			newFilter.onPostAdd(this, name, entry.getNextFilter());
			return oldFilter;
		} catch (Exception arg5) {
			entry.setFilter(oldFilter);
			throw new IoFilterLifeCycleException("onPostAdd(): " + name + ':' + newFilter + " in " + this.getSession(),
					arg5);
		}
	}

	public synchronized void replace(IoFilter oldFilter, IoFilter newFilter) {
		for (DefaultIoFilterChain.EntryImpl entry = this.head.nextEntry; entry != this.tail; entry = entry.nextEntry) {
			if (entry.getFilter() == oldFilter) {
				String oldFilterName = null;
				Iterator e = this.name2entry.entrySet().iterator();

				while (e.hasNext()) {
					java.util.Map.Entry mapping = (java.util.Map.Entry) e.next();
					if (entry == mapping.getValue()) {
						oldFilterName = (String) mapping.getKey();
						break;
					}
				}

				try {
					newFilter.onPreAdd(this, oldFilterName, entry.getNextFilter());
				} catch (Exception arg7) {
					throw new IoFilterLifeCycleException(
							"onPreAdd(): " + oldFilterName + ':' + newFilter + " in " + this.getSession(), arg7);
				}

				entry.setFilter(newFilter);

				try {
					newFilter.onPostAdd(this, oldFilterName, entry.getNextFilter());
					return;
				} catch (Exception arg6) {
					entry.setFilter(oldFilter);
					throw new IoFilterLifeCycleException(
							"onPostAdd(): " + oldFilterName + ':' + newFilter + " in " + this.getSession(), arg6);
				}
			}
		}

		throw new IllegalArgumentException("Filter not found: " + oldFilter.getClass().getName());
	}

	public synchronized IoFilter replace(Class<? extends IoFilter> oldFilterType, IoFilter newFilter) {
		for (DefaultIoFilterChain.EntryImpl entry = this.head.nextEntry; entry != this.tail; entry = entry.nextEntry) {
			if (oldFilterType.isAssignableFrom(entry.getFilter().getClass())) {
				IoFilter oldFilter = entry.getFilter();
				String oldFilterName = null;
				Iterator e = this.name2entry.entrySet().iterator();

				while (e.hasNext()) {
					java.util.Map.Entry mapping = (java.util.Map.Entry) e.next();
					if (entry == mapping.getValue()) {
						oldFilterName = (String) mapping.getKey();
						break;
					}
				}

				try {
					newFilter.onPreAdd(this, oldFilterName, entry.getNextFilter());
				} catch (Exception arg8) {
					throw new IoFilterLifeCycleException(
							"onPreAdd(): " + oldFilterName + ':' + newFilter + " in " + this.getSession(), arg8);
				}

				entry.setFilter(newFilter);

				try {
					newFilter.onPostAdd(this, oldFilterName, entry.getNextFilter());
					return oldFilter;
				} catch (Exception arg7) {
					entry.setFilter(oldFilter);
					throw new IoFilterLifeCycleException(
							"onPostAdd(): " + oldFilterName + ':' + newFilter + " in " + this.getSession(), arg7);
				}
			}
		}

		throw new IllegalArgumentException("Filter not found: " + oldFilterType.getName());
	}

	public synchronized void clear() throws Exception {
		ArrayList l = new ArrayList(this.name2entry.values());
		Iterator i$ = l.iterator();

		while (i$.hasNext()) {
			Entry entry = (Entry) i$.next();

			try {
				this.deregister((DefaultIoFilterChain.EntryImpl) entry);
			} catch (Exception arg4) {
				throw new IoFilterLifeCycleException("clear(): " + entry.getName() + " in " + this.getSession(), arg4);
			}
		}

	}

	private void register(DefaultIoFilterChain.EntryImpl prevEntry, String name, IoFilter filter) {
		DefaultIoFilterChain.EntryImpl newEntry = new DefaultIoFilterChain.EntryImpl(prevEntry, prevEntry.nextEntry,
				name, filter);

		try {
			filter.onPreAdd(this, name, newEntry.getNextFilter());
		} catch (Exception arg6) {
			throw new IoFilterLifeCycleException("onPreAdd(): " + name + ':' + filter + " in " + this.getSession(),
					arg6);
		}

		prevEntry.nextEntry.prevEntry = newEntry;
		prevEntry.nextEntry = newEntry;
		this.name2entry.put(name, newEntry);

		try {
			filter.onPostAdd(this, name, newEntry.getNextFilter());
		} catch (Exception arg5) {
			this.deregister0(newEntry);
			throw new IoFilterLifeCycleException("onPostAdd(): " + name + ':' + filter + " in " + this.getSession(),
					arg5);
		}
	}

	private void deregister(DefaultIoFilterChain.EntryImpl entry) {
		IoFilter filter = entry.getFilter();

		try {
			filter.onPreRemove(this, entry.getName(), entry.getNextFilter());
		} catch (Exception arg4) {
			throw new IoFilterLifeCycleException(
					"onPreRemove(): " + entry.getName() + ':' + filter + " in " + this.getSession(), arg4);
		}

		this.deregister0(entry);

		try {
			filter.onPostRemove(this, entry.getName(), entry.getNextFilter());
		} catch (Exception arg3) {
			throw new IoFilterLifeCycleException(
					"onPostRemove(): " + entry.getName() + ':' + filter + " in " + this.getSession(), arg3);
		}
	}

	private void deregister0(DefaultIoFilterChain.EntryImpl entry) {
		DefaultIoFilterChain.EntryImpl prevEntry = entry.prevEntry;
		DefaultIoFilterChain.EntryImpl nextEntry = entry.nextEntry;
		prevEntry.nextEntry = nextEntry;
		nextEntry.prevEntry = prevEntry;
		this.name2entry.remove(entry.name);
	}

	private DefaultIoFilterChain.EntryImpl checkOldName(String baseName) {
		DefaultIoFilterChain.EntryImpl e = (DefaultIoFilterChain.EntryImpl) this.name2entry.get(baseName);
		if (e == null) {
			throw new IllegalArgumentException("Filter not found:" + baseName);
		} else {
			return e;
		}
	}

	private void checkAddable(String name) {
		if (this.name2entry.containsKey(name)) {
			throw new IllegalArgumentException("Other filter is using the same name \'" + name + "\'");
		}
	}

	public void fireSessionCreated() {
		this.callNextSessionCreated(this.head, this.session);
	}

	private void callNextSessionCreated(Entry entry, IoSession session) {
		try {
			IoFilter e = entry.getFilter();
			NextFilter nextFilter = entry.getNextFilter();
			e.sessionCreated(nextFilter, session);
		} catch (Exception arg4) {
			this.fireExceptionCaught(arg4);
		} catch (Error arg5) {
			this.fireExceptionCaught(arg5);
			throw arg5;
		}

	}

	public void fireSessionOpened() {
		this.callNextSessionOpened(this.head, this.session);
	}

	private void callNextSessionOpened(Entry entry, IoSession session) {
		try {
			IoFilter e = entry.getFilter();
			NextFilter nextFilter = entry.getNextFilter();
			e.sessionOpened(nextFilter, session);
		} catch (Exception arg4) {
			this.fireExceptionCaught(arg4);
		} catch (Error arg5) {
			this.fireExceptionCaught(arg5);
			throw arg5;
		}

	}

	public void fireSessionClosed() {
		try {
			this.session.getCloseFuture().setClosed();
		} catch (Exception arg1) {
			this.fireExceptionCaught(arg1);
		} catch (Error arg2) {
			this.fireExceptionCaught(arg2);
			throw arg2;
		}

		this.callNextSessionClosed(this.head, this.session);
	}

	private void callNextSessionClosed(Entry entry, IoSession session) {
		try {
			IoFilter e = entry.getFilter();
			NextFilter nextFilter = entry.getNextFilter();
			e.sessionClosed(nextFilter, session);
		} catch (Exception arg4) {
			this.fireExceptionCaught(arg4);
		} catch (Error arg5) {
			this.fireExceptionCaught(arg5);
		}

	}

	public void fireSessionIdle(IdleStatus status) {
		this.session.increaseIdleCount(status, System.currentTimeMillis());
		this.callNextSessionIdle(this.head, this.session, status);
	}

	private void callNextSessionIdle(Entry entry, IoSession session, IdleStatus status) {
		try {
			IoFilter e = entry.getFilter();
			NextFilter nextFilter = entry.getNextFilter();
			e.sessionIdle(nextFilter, session, status);
		} catch (Exception arg5) {
			this.fireExceptionCaught(arg5);
		} catch (Error arg6) {
			this.fireExceptionCaught(arg6);
			throw arg6;
		}

	}

	public void fireMessageReceived(Object message) {
		if (message instanceof IoBuffer) {
			this.session.increaseReadBytes((long) ((IoBuffer) message).remaining(), System.currentTimeMillis());
		}

		this.callNextMessageReceived(this.head, this.session, message);
	}

	private void callNextMessageReceived(Entry entry, IoSession session, Object message) {
		try {
			IoFilter e = entry.getFilter();
			NextFilter nextFilter = entry.getNextFilter();
			e.messageReceived(nextFilter, session, message);
		} catch (Exception arg5) {
			this.fireExceptionCaught(arg5);
		} catch (Error arg6) {
			this.fireExceptionCaught(arg6);
			throw arg6;
		}

	}

	public void fireMessageSent(WriteRequest request) {
		try {
			request.getFuture().setWritten();
		} catch (Exception arg2) {
			this.fireExceptionCaught(arg2);
		} catch (Error arg3) {
			this.fireExceptionCaught(arg3);
			throw arg3;
		}

		if (!request.isEncoded()) {
			this.callNextMessageSent(this.head, this.session, request);
		}

	}

	private void callNextMessageSent(Entry entry, IoSession session, WriteRequest writeRequest) {
		try {
			IoFilter e = entry.getFilter();
			NextFilter nextFilter = entry.getNextFilter();
			e.messageSent(nextFilter, session, writeRequest);
		} catch (Exception arg5) {
			this.fireExceptionCaught(arg5);
		} catch (Error arg6) {
			this.fireExceptionCaught(arg6);
			throw arg6;
		}

	}

	public void fireExceptionCaught(Throwable cause) {
		this.callNextExceptionCaught(this.head, this.session, cause);
	}

	private void callNextExceptionCaught(Entry entry, IoSession session, Throwable cause) {
		ConnectFuture future = (ConnectFuture) session.removeAttribute(SESSION_CREATED_FUTURE);
		if (future == null) {
			try {
				IoFilter e = entry.getFilter();
				NextFilter nextFilter = entry.getNextFilter();
				e.exceptionCaught(nextFilter, session, cause);
			} catch (Throwable arg6) {
				LOGGER.warn("Unexpected exception from exceptionCaught handler.", arg6);
			}
		} else {
			if (!session.isClosing()) {
				session.closeNow();
			}

			future.setException(cause);
		}

	}

	public void fireInputClosed() {
		DefaultIoFilterChain.EntryImpl head = this.head;
		this.callNextInputClosed(head, this.session);
	}

	private void callNextInputClosed(Entry entry, IoSession session) {
		try {
			IoFilter e = entry.getFilter();
			NextFilter nextFilter = entry.getNextFilter();
			e.inputClosed(nextFilter, session);
		} catch (Throwable arg4) {
			this.fireExceptionCaught(arg4);
		}

	}

	public void fireFilterWrite(WriteRequest writeRequest) {
		this.callPreviousFilterWrite(this.tail, this.session, writeRequest);
	}

	private void callPreviousFilterWrite(Entry entry, IoSession session, WriteRequest writeRequest) {
		try {
			IoFilter e = entry.getFilter();
			NextFilter nextFilter = entry.getNextFilter();
			e.filterWrite(nextFilter, session, writeRequest);
		} catch (Exception arg5) {
			writeRequest.getFuture().setException(arg5);
			this.fireExceptionCaught(arg5);
		} catch (Error arg6) {
			writeRequest.getFuture().setException(arg6);
			this.fireExceptionCaught(arg6);
			throw arg6;
		}

	}

	public void fireFilterClose() {
		this.callPreviousFilterClose(this.tail, this.session);
	}

	private void callPreviousFilterClose(Entry entry, IoSession session) {
		try {
			IoFilter e = entry.getFilter();
			NextFilter nextFilter = entry.getNextFilter();
			e.filterClose(nextFilter, session);
		} catch (Exception arg4) {
			this.fireExceptionCaught(arg4);
		} catch (Error arg5) {
			this.fireExceptionCaught(arg5);
			throw arg5;
		}

	}

	public List<Entry> getAll() {
		ArrayList list = new ArrayList();

		for (DefaultIoFilterChain.EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
			list.add(e);
		}

		return list;
	}

	public List<Entry> getAllReversed() {
		ArrayList list = new ArrayList();

		for (DefaultIoFilterChain.EntryImpl e = this.tail.prevEntry; e != this.head; e = e.prevEntry) {
			list.add(e);
		}

		return list;
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

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("{ ");
		boolean empty = true;

		for (DefaultIoFilterChain.EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
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

	private final class EntryImpl implements Entry {
		private DefaultIoFilterChain.EntryImpl prevEntry;
		private DefaultIoFilterChain.EntryImpl nextEntry;
		private final String name;
		private IoFilter filter;
		private final NextFilter nextFilter;

		private EntryImpl(DefaultIoFilterChain.EntryImpl prevEntry, DefaultIoFilterChain.EntryImpl nextEntry,
				String name, IoFilter filter) {
			if (filter == null) {
				throw new IllegalArgumentException("filter");
			} else if (name == null) {
				throw new IllegalArgumentException("name");
			} else {
				this.prevEntry = prevEntry;
				this.nextEntry = nextEntry;
				this.name = name;
				this.filter = filter;
				this.nextFilter = new NextFilter() {
					public void sessionCreated(IoSession session) {
						DefaultIoFilterChain.EntryImpl nextEntry = EntryImpl.this.nextEntry;
						DefaultIoFilterChain.this.callNextSessionCreated(nextEntry, session);
					}

					public void sessionOpened(IoSession session) {
						DefaultIoFilterChain.EntryImpl nextEntry = EntryImpl.this.nextEntry;
						DefaultIoFilterChain.this.callNextSessionOpened(nextEntry, session);
					}

					public void sessionClosed(IoSession session) {
						DefaultIoFilterChain.EntryImpl nextEntry = EntryImpl.this.nextEntry;
						DefaultIoFilterChain.this.callNextSessionClosed(nextEntry, session);
					}

					public void sessionIdle(IoSession session, IdleStatus status) {
						DefaultIoFilterChain.EntryImpl nextEntry = EntryImpl.this.nextEntry;
						DefaultIoFilterChain.this.callNextSessionIdle(nextEntry, session, status);
					}

					public void exceptionCaught(IoSession session, Throwable cause) {
						DefaultIoFilterChain.EntryImpl nextEntry = EntryImpl.this.nextEntry;
						DefaultIoFilterChain.this.callNextExceptionCaught(nextEntry, session, cause);
					}

					public void inputClosed(IoSession session) {
						DefaultIoFilterChain.EntryImpl nextEntry = EntryImpl.this.nextEntry;
						DefaultIoFilterChain.this.callNextInputClosed(nextEntry, session);
					}

					public void messageReceived(IoSession session, Object message) {
						DefaultIoFilterChain.EntryImpl nextEntry = EntryImpl.this.nextEntry;
						DefaultIoFilterChain.this.callNextMessageReceived(nextEntry, session, message);
					}

					public void messageSent(IoSession session, WriteRequest writeRequest) {
						DefaultIoFilterChain.EntryImpl nextEntry = EntryImpl.this.nextEntry;
						DefaultIoFilterChain.this.callNextMessageSent(nextEntry, session, writeRequest);
					}

					public void filterWrite(IoSession session, WriteRequest writeRequest) {
						DefaultIoFilterChain.EntryImpl nextEntry = EntryImpl.this.prevEntry;
						DefaultIoFilterChain.this.callPreviousFilterWrite(nextEntry, session, writeRequest);
					}

					public void filterClose(IoSession session) {
						DefaultIoFilterChain.EntryImpl nextEntry = EntryImpl.this.prevEntry;
						DefaultIoFilterChain.this.callPreviousFilterClose(nextEntry, session);
					}

					public String toString() {
						return EntryImpl.this.nextEntry.name;
					}
				};
			}
		}

		public String getName() {
			return this.name;
		}

		public IoFilter getFilter() {
			return this.filter;
		}

		private void setFilter(IoFilter filter) {
			if (filter == null) {
				throw new IllegalArgumentException("filter");
			} else {
				this.filter = filter;
			}
		}

		public NextFilter getNextFilter() {
			return this.nextFilter;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("(\'").append(this.getName()).append('\'');
			sb.append(", prev: \'");
			if (this.prevEntry != null) {
				sb.append(this.prevEntry.name);
				sb.append(':');
				sb.append(this.prevEntry.getFilter().getClass().getSimpleName());
			} else {
				sb.append("null");
			}

			sb.append("\', next: \'");
			if (this.nextEntry != null) {
				sb.append(this.nextEntry.name);
				sb.append(':');
				sb.append(this.nextEntry.getFilter().getClass().getSimpleName());
			} else {
				sb.append("null");
			}

			sb.append("\')");
			return sb.toString();
		}

		public void addAfter(String name, IoFilter filter) {
			DefaultIoFilterChain.this.addAfter(this.getName(), name, filter);
		}

		public void addBefore(String name, IoFilter filter) {
			DefaultIoFilterChain.this.addBefore(this.getName(), name, filter);
		}

		public void remove() {
			DefaultIoFilterChain.this.remove(this.getName());
		}

		public void replace(IoFilter newFilter) {
			DefaultIoFilterChain.this.replace(this.getName(), newFilter);
		}
	}

	private static class TailFilter extends IoFilterAdapter {
		private TailFilter() {
		}

		public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
			boolean arg6 = false;

			try {
				arg6 = true;
				session.getHandler().sessionCreated(session);
				arg6 = false;
			} finally {
				if (arg6) {
					ConnectFuture future1 = (ConnectFuture) session
							.removeAttribute(DefaultIoFilterChain.SESSION_CREATED_FUTURE);
					if (future1 != null) {
						future1.setSession(session);
					}

				}
			}

			ConnectFuture future = (ConnectFuture) session.removeAttribute(DefaultIoFilterChain.SESSION_CREATED_FUTURE);
			if (future != null) {
				future.setSession(session);
			}

		}

		public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
			session.getHandler().sessionOpened(session);
		}

		public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
			AbstractIoSession s = (AbstractIoSession) session;

			try {
				s.getHandler().sessionClosed(session);
			} finally {
				try {
					s.getWriteRequestQueue().dispose(session);
				} finally {
					try {
						s.getAttributeMap().dispose(session);
					} finally {
						try {
							session.getFilterChain().clear();
						} finally {
							if (s.getConfig().isUseReadOperation()) {
								s.offerClosedReadFuture();
							}

						}
					}
				}
			}

		}

		public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
			session.getHandler().sessionIdle(session, status);
		}

		public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
			AbstractIoSession s = (AbstractIoSession) session;

			try {
				s.getHandler().exceptionCaught(s, cause);
			} finally {
				if (s.getConfig().isUseReadOperation()) {
					s.offerFailedReadFuture(cause);
				}

			}

		}

		public void inputClosed(NextFilter nextFilter, IoSession session) throws Exception {
			session.getHandler().inputClosed(session);
		}

		public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
			AbstractIoSession s = (AbstractIoSession) session;
			if (!(message instanceof IoBuffer)) {
				s.increaseReadMessages(System.currentTimeMillis());
			} else if (!((IoBuffer) message).hasRemaining()) {
				s.increaseReadMessages(System.currentTimeMillis());
			}

			if (session.getService() instanceof AbstractIoService) {
				((AbstractIoService) session.getService()).getStatistics().updateThroughput(System.currentTimeMillis());
			}

			try {
				session.getHandler().messageReceived(s, message);
			} finally {
				if (s.getConfig().isUseReadOperation()) {
					s.offerReadFuture(message);
				}

			}

		}

		public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
			((AbstractIoSession) session).increaseWrittenMessages(writeRequest, System.currentTimeMillis());
			if (session.getService() instanceof AbstractIoService) {
				((AbstractIoService) session.getService()).getStatistics().updateThroughput(System.currentTimeMillis());
			}

			session.getHandler().messageSent(session, writeRequest.getMessage());
		}

		public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
			nextFilter.filterWrite(session, writeRequest);
		}

		public void filterClose(NextFilter nextFilter, IoSession session) throws Exception {
			nextFilter.filterClose(session);
		}
	}

	private class HeadFilter extends IoFilterAdapter {
		private HeadFilter() {
		}

		public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
			AbstractIoSession s = (AbstractIoSession) session;
			if (writeRequest.getMessage() instanceof IoBuffer) {
				IoBuffer writeRequestQueue = (IoBuffer) writeRequest.getMessage();
				writeRequestQueue.mark();
				int remaining = writeRequestQueue.remaining();
				if (remaining > 0) {
					s.increaseScheduledWriteBytes(remaining);
				}
			} else {
				s.increaseScheduledWriteMessages();
			}

			WriteRequestQueue writeRequestQueue1 = s.getWriteRequestQueue();
			if (!s.isWriteSuspended()) {
				if (writeRequestQueue1.isEmpty(session)) {
					s.getProcessor().write(s, writeRequest);
				} else {
					s.getWriteRequestQueue().offer(s, writeRequest);
					s.getProcessor().flush(s);
				}
			} else {
				s.getWriteRequestQueue().offer(s, writeRequest);
			}

		}

		public void filterClose(NextFilter nextFilter, IoSession session) throws Exception {
			((AbstractIoSession) session).getProcessor().remove(session);
		}
	}
}