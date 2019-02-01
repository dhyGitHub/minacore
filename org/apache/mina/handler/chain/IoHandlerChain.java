/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.chain.IoHandlerCommand;
import org.apache.mina.handler.chain.IoHandlerCommand.NextCommand;

public class IoHandlerChain implements IoHandlerCommand {
	private static volatile int nextId = 0;
	private final int id;
	private final String NEXT_COMMAND;
	private final Map<String, IoHandlerChain.Entry> name2entry;
	private final IoHandlerChain.Entry head;
	private final IoHandlerChain.Entry tail;

	public IoHandlerChain() {
		this.id = nextId++;
		this.NEXT_COMMAND = IoHandlerChain.class.getName() + '.' + this.id + ".nextCommand";
		this.name2entry = new ConcurrentHashMap();
		this.head = new IoHandlerChain.Entry((IoHandlerChain.Entry) null, (IoHandlerChain.Entry) null, "head",
				this.createHeadCommand(), null);
		this.tail = new IoHandlerChain.Entry(this.head, (IoHandlerChain.Entry) null, "tail", this.createTailCommand(),
				null);
		this.head.nextEntry = this.tail;
	}

	private IoHandlerCommand createHeadCommand() {
		return new IoHandlerCommand() {
			public void execute(NextCommand next, IoSession session, Object message) throws Exception {
				next.execute(session, message);
			}
		};
	}

	private IoHandlerCommand createTailCommand() {
		return new IoHandlerCommand() {
			public void execute(NextCommand next, IoSession session, Object message) throws Exception {
				next = (NextCommand) session.getAttribute(IoHandlerChain.this.NEXT_COMMAND);
				if (next != null) {
					next.execute(session, message);
				}

			}
		};
	}

	public IoHandlerChain.Entry getEntry(String name) {
		IoHandlerChain.Entry e = (IoHandlerChain.Entry) this.name2entry.get(name);
		return e == null ? null : e;
	}

	public IoHandlerCommand get(String name) {
		IoHandlerChain.Entry e = this.getEntry(name);
		return e == null ? null : e.getCommand();
	}

	public NextCommand getNextCommand(String name) {
		IoHandlerChain.Entry e = this.getEntry(name);
		return e == null ? null : e.getNextCommand();
	}

	public synchronized void addFirst(String name, IoHandlerCommand command) {
		this.checkAddable(name);
		this.register(this.head, name, command);
	}

	public synchronized void addLast(String name, IoHandlerCommand command) {
		this.checkAddable(name);
		this.register(this.tail.prevEntry, name, command);
	}

	public synchronized void addBefore(String baseName, String name, IoHandlerCommand command) {
		IoHandlerChain.Entry baseEntry = this.checkOldName(baseName);
		this.checkAddable(name);
		this.register(baseEntry.prevEntry, name, command);
	}

	public synchronized void addAfter(String baseName, String name, IoHandlerCommand command) {
		IoHandlerChain.Entry baseEntry = this.checkOldName(baseName);
		this.checkAddable(name);
		this.register(baseEntry, name, command);
	}

	public synchronized IoHandlerCommand remove(String name) {
		IoHandlerChain.Entry entry = this.checkOldName(name);
		this.deregister(entry);
		return entry.getCommand();
	}

	public synchronized void clear() throws Exception {
		Iterator it = (new ArrayList(this.name2entry.keySet())).iterator();

		while (it.hasNext()) {
			this.remove((String) it.next());
		}

	}

	private void register(IoHandlerChain.Entry prevEntry, String name, IoHandlerCommand command) {
		IoHandlerChain.Entry newEntry = new IoHandlerChain.Entry(prevEntry, prevEntry.nextEntry, name, command, null);
		prevEntry.nextEntry.prevEntry = newEntry;
		prevEntry.nextEntry = newEntry;
		this.name2entry.put(name, newEntry);
	}

	private void deregister(IoHandlerChain.Entry entry) {
		IoHandlerChain.Entry prevEntry = entry.prevEntry;
		IoHandlerChain.Entry nextEntry = entry.nextEntry;
		prevEntry.nextEntry = nextEntry;
		nextEntry.prevEntry = prevEntry;
		this.name2entry.remove(entry.name);
	}

	private IoHandlerChain.Entry checkOldName(String baseName) {
		IoHandlerChain.Entry e = (IoHandlerChain.Entry) this.name2entry.get(baseName);
		if (e == null) {
			throw new IllegalArgumentException("Unknown filter name:" + baseName);
		} else {
			return e;
		}
	}

	private void checkAddable(String name) {
		if (this.name2entry.containsKey(name)) {
			throw new IllegalArgumentException("Other filter is using the same name \'" + name + "\'");
		}
	}

	public void execute(NextCommand next, IoSession session, Object message) throws Exception {
		if (next != null) {
			session.setAttribute(this.NEXT_COMMAND, next);
		}

		try {
			this.callNextCommand(this.head, session, message);
		} finally {
			session.removeAttribute(this.NEXT_COMMAND);
		}

	}

	private void callNextCommand(IoHandlerChain.Entry entry, IoSession session, Object message) throws Exception {
		entry.getCommand().execute(entry.getNextCommand(), session, message);
	}

	public List<IoHandlerChain.Entry> getAll() {
		ArrayList list = new ArrayList();

		for (IoHandlerChain.Entry e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
			list.add(e);
		}

		return list;
	}

	public List<IoHandlerChain.Entry> getAllReversed() {
		ArrayList list = new ArrayList();

		for (IoHandlerChain.Entry e = this.tail.prevEntry; e != this.head; e = e.prevEntry) {
			list.add(e);
		}

		return list;
	}

	public boolean contains(String name) {
		return this.getEntry(name) != null;
	}

	public boolean contains(IoHandlerCommand command) {
		for (IoHandlerChain.Entry e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
			if (e.getCommand() == command) {
				return true;
			}
		}

		return false;
	}

	public boolean contains(Class<? extends IoHandlerCommand> commandType) {
		for (IoHandlerChain.Entry e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
			if (commandType.isAssignableFrom(e.getCommand().getClass())) {
				return true;
			}
		}

		return false;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("{ ");
		boolean empty = true;

		for (IoHandlerChain.Entry e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
			if (!empty) {
				buf.append(", ");
			} else {
				empty = false;
			}

			buf.append('(');
			buf.append(e.getName());
			buf.append(':');
			buf.append(e.getCommand());
			buf.append(')');
		}

		if (empty) {
			buf.append("empty");
		}

		buf.append(" }");
		return buf.toString();
	}

	public class Entry {
		private IoHandlerChain.Entry prevEntry;
		private IoHandlerChain.Entry nextEntry;
		private final String name;
		private final IoHandlerCommand command;
		private final NextCommand nextCommand;

		private Entry(IoHandlerChain.Entry prevEntry, IoHandlerChain.Entry nextEntry, String name,
				IoHandlerCommand command) {
			if (command == null) {
				throw new IllegalArgumentException("command");
			} else if (name == null) {
				throw new IllegalArgumentException("name");
			} else {
				this.prevEntry = prevEntry;
				this.nextEntry = nextEntry;
				this.name = name;
				this.command = command;
				this.nextCommand = new NextCommand() {
					public void execute(IoSession session, Object message) throws Exception {
						IoHandlerChain.Entry nextEntry = Entry.this.nextEntry;
						IoHandlerChain.this.callNextCommand(nextEntry, session, message);
					}
				};
			}
		}

		public String getName() {
			return this.name;
		}

		public IoHandlerCommand getCommand() {
			return this.command;
		}

		public NextCommand getNextCommand() {
			return this.nextCommand;
		}
	}
}