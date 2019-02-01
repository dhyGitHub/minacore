/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.statistic;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public class ProfilerTimerFilter extends IoFilterAdapter {
	private volatile TimeUnit timeUnit;
	private ProfilerTimerFilter.TimerWorker messageReceivedTimerWorker;
	private boolean profileMessageReceived;
	private ProfilerTimerFilter.TimerWorker messageSentTimerWorker;
	private boolean profileMessageSent;
	private ProfilerTimerFilter.TimerWorker sessionCreatedTimerWorker;
	private boolean profileSessionCreated;
	private ProfilerTimerFilter.TimerWorker sessionOpenedTimerWorker;
	private boolean profileSessionOpened;
	private ProfilerTimerFilter.TimerWorker sessionIdleTimerWorker;
	private boolean profileSessionIdle;
	private ProfilerTimerFilter.TimerWorker sessionClosedTimerWorker;
	private boolean profileSessionClosed;

	public ProfilerTimerFilter() {
		this(TimeUnit.MILLISECONDS, new IoEventType[] { IoEventType.MESSAGE_RECEIVED, IoEventType.MESSAGE_SENT });
	}

	public ProfilerTimerFilter(TimeUnit timeUnit) {
		this(timeUnit, new IoEventType[] { IoEventType.MESSAGE_RECEIVED, IoEventType.MESSAGE_SENT });
	}

	public ProfilerTimerFilter(TimeUnit timeUnit, IoEventType... eventTypes) {
		this.profileMessageReceived = false;
		this.profileMessageSent = false;
		this.profileSessionCreated = false;
		this.profileSessionOpened = false;
		this.profileSessionIdle = false;
		this.profileSessionClosed = false;
		this.timeUnit = timeUnit;
		this.setProfilers(eventTypes);
	}

	private void setProfilers(IoEventType... eventTypes) {
		IoEventType[] arr$ = eventTypes;
		int len$ = eventTypes.length;

		for (int i$ = 0; i$ < len$; ++i$) {
			IoEventType type = arr$[i$];
			switch (ProfilerTimerFilter.SyntheticClass_1.$SwitchMap$org$apache$mina$core$session$IoEventType[type
					.ordinal()]) {
			case 1:
				this.messageReceivedTimerWorker = new ProfilerTimerFilter.TimerWorker();
				this.profileMessageReceived = true;
				break;
			case 2:
				this.messageSentTimerWorker = new ProfilerTimerFilter.TimerWorker();
				this.profileMessageSent = true;
				break;
			case 3:
				this.sessionCreatedTimerWorker = new ProfilerTimerFilter.TimerWorker();
				this.profileSessionCreated = true;
				break;
			case 4:
				this.sessionOpenedTimerWorker = new ProfilerTimerFilter.TimerWorker();
				this.profileSessionOpened = true;
				break;
			case 5:
				this.sessionIdleTimerWorker = new ProfilerTimerFilter.TimerWorker();
				this.profileSessionIdle = true;
				break;
			case 6:
				this.sessionClosedTimerWorker = new ProfilerTimerFilter.TimerWorker();
				this.profileSessionClosed = true;
			}
		}

	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public void profile(IoEventType type) {
		switch (ProfilerTimerFilter.SyntheticClass_1.$SwitchMap$org$apache$mina$core$session$IoEventType[type
				.ordinal()]) {
		case 1:
			this.profileMessageReceived = true;
			if (this.messageReceivedTimerWorker == null) {
				this.messageReceivedTimerWorker = new ProfilerTimerFilter.TimerWorker();
			}

			return;
		case 2:
			this.profileMessageSent = true;
			if (this.messageSentTimerWorker == null) {
				this.messageSentTimerWorker = new ProfilerTimerFilter.TimerWorker();
			}

			return;
		case 3:
			this.profileSessionCreated = true;
			if (this.sessionCreatedTimerWorker == null) {
				this.sessionCreatedTimerWorker = new ProfilerTimerFilter.TimerWorker();
			}

			return;
		case 4:
			this.profileSessionOpened = true;
			if (this.sessionOpenedTimerWorker == null) {
				this.sessionOpenedTimerWorker = new ProfilerTimerFilter.TimerWorker();
			}

			return;
		case 5:
			this.profileSessionIdle = true;
			if (this.sessionIdleTimerWorker == null) {
				this.sessionIdleTimerWorker = new ProfilerTimerFilter.TimerWorker();
			}

			return;
		case 6:
			this.profileSessionClosed = true;
			if (this.sessionClosedTimerWorker == null) {
				this.sessionClosedTimerWorker = new ProfilerTimerFilter.TimerWorker();
			}

			return;
		default:
		}
	}

	public void stopProfile(IoEventType type) {
		switch (ProfilerTimerFilter.SyntheticClass_1.$SwitchMap$org$apache$mina$core$session$IoEventType[type
				.ordinal()]) {
		case 1:
			this.profileMessageReceived = false;
			return;
		case 2:
			this.profileMessageSent = false;
			return;
		case 3:
			this.profileSessionCreated = false;
			return;
		case 4:
			this.profileSessionOpened = false;
			return;
		case 5:
			this.profileSessionIdle = false;
			return;
		case 6:
			this.profileSessionClosed = false;
			return;
		default:
		}
	}

	public Set<IoEventType> getEventsToProfile() {
		HashSet set = new HashSet();
		if (this.profileMessageReceived) {
			set.add(IoEventType.MESSAGE_RECEIVED);
		}

		if (this.profileMessageSent) {
			set.add(IoEventType.MESSAGE_SENT);
		}

		if (this.profileSessionCreated) {
			set.add(IoEventType.SESSION_CREATED);
		}

		if (this.profileSessionOpened) {
			set.add(IoEventType.SESSION_OPENED);
		}

		if (this.profileSessionIdle) {
			set.add(IoEventType.SESSION_IDLE);
		}

		if (this.profileSessionClosed) {
			set.add(IoEventType.SESSION_CLOSED);
		}

		return set;
	}

	public void setEventsToProfile(IoEventType... eventTypes) {
		this.setProfilers(eventTypes);
	}

	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		if (this.profileMessageReceived) {
			long start = this.timeNow();
			nextFilter.messageReceived(session, message);
			long end = this.timeNow();
			this.messageReceivedTimerWorker.addNewDuration(end - start);
		} else {
			nextFilter.messageReceived(session, message);
		}

	}

	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		if (this.profileMessageSent) {
			long start = this.timeNow();
			nextFilter.messageSent(session, writeRequest);
			long end = this.timeNow();
			this.messageSentTimerWorker.addNewDuration(end - start);
		} else {
			nextFilter.messageSent(session, writeRequest);
		}

	}

	public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
		if (this.profileSessionCreated) {
			long start = this.timeNow();
			nextFilter.sessionCreated(session);
			long end = this.timeNow();
			this.sessionCreatedTimerWorker.addNewDuration(end - start);
		} else {
			nextFilter.sessionCreated(session);
		}

	}

	public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		if (this.profileSessionOpened) {
			long start = this.timeNow();
			nextFilter.sessionOpened(session);
			long end = this.timeNow();
			this.sessionOpenedTimerWorker.addNewDuration(end - start);
		} else {
			nextFilter.sessionOpened(session);
		}

	}

	public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
		if (this.profileSessionIdle) {
			long start = this.timeNow();
			nextFilter.sessionIdle(session, status);
			long end = this.timeNow();
			this.sessionIdleTimerWorker.addNewDuration(end - start);
		} else {
			nextFilter.sessionIdle(session, status);
		}

	}

	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		if (this.profileSessionClosed) {
			long start = this.timeNow();
			nextFilter.sessionClosed(session);
			long end = this.timeNow();
			this.sessionClosedTimerWorker.addNewDuration(end - start);
		} else {
			nextFilter.sessionClosed(session);
		}

	}

	public double getAverageTime(IoEventType type) {
		switch (ProfilerTimerFilter.SyntheticClass_1.$SwitchMap$org$apache$mina$core$session$IoEventType[type
				.ordinal()]) {
		case 1:
			if (this.profileMessageReceived) {
				return this.messageReceivedTimerWorker.getAverage();
			}
			break;
		case 2:
			if (this.profileMessageSent) {
				return this.messageSentTimerWorker.getAverage();
			}
			break;
		case 3:
			if (this.profileSessionCreated) {
				return this.sessionCreatedTimerWorker.getAverage();
			}
			break;
		case 4:
			if (this.profileSessionOpened) {
				return this.sessionOpenedTimerWorker.getAverage();
			}
			break;
		case 5:
			if (this.profileSessionIdle) {
				return this.sessionIdleTimerWorker.getAverage();
			}
			break;
		case 6:
			if (this.profileSessionClosed) {
				return this.sessionClosedTimerWorker.getAverage();
			}
		}

		throw new IllegalArgumentException("You are not monitoring this event.  Please add this event first.");
	}

	public long getTotalCalls(IoEventType type) {
		switch (ProfilerTimerFilter.SyntheticClass_1.$SwitchMap$org$apache$mina$core$session$IoEventType[type
				.ordinal()]) {
		case 1:
			if (this.profileMessageReceived) {
				return this.messageReceivedTimerWorker.getCallsNumber();
			}
			break;
		case 2:
			if (this.profileMessageSent) {
				return this.messageSentTimerWorker.getCallsNumber();
			}
			break;
		case 3:
			if (this.profileSessionCreated) {
				return this.sessionCreatedTimerWorker.getCallsNumber();
			}
			break;
		case 4:
			if (this.profileSessionOpened) {
				return this.sessionOpenedTimerWorker.getCallsNumber();
			}
			break;
		case 5:
			if (this.profileSessionIdle) {
				return this.sessionIdleTimerWorker.getCallsNumber();
			}
			break;
		case 6:
			if (this.profileSessionClosed) {
				return this.sessionClosedTimerWorker.getCallsNumber();
			}
		}

		throw new IllegalArgumentException("You are not monitoring this event.  Please add this event first.");
	}

	public long getTotalTime(IoEventType type) {
		switch (ProfilerTimerFilter.SyntheticClass_1.$SwitchMap$org$apache$mina$core$session$IoEventType[type
				.ordinal()]) {
		case 1:
			if (this.profileMessageReceived) {
				return this.messageReceivedTimerWorker.getTotal();
			}
			break;
		case 2:
			if (this.profileMessageSent) {
				return this.messageSentTimerWorker.getTotal();
			}
			break;
		case 3:
			if (this.profileSessionCreated) {
				return this.sessionCreatedTimerWorker.getTotal();
			}
			break;
		case 4:
			if (this.profileSessionOpened) {
				return this.sessionOpenedTimerWorker.getTotal();
			}
			break;
		case 5:
			if (this.profileSessionIdle) {
				return this.sessionIdleTimerWorker.getTotal();
			}
			break;
		case 6:
			if (this.profileSessionClosed) {
				return this.sessionClosedTimerWorker.getTotal();
			}
		}

		throw new IllegalArgumentException("You are not monitoring this event.  Please add this event first.");
	}

	public long getMinimumTime(IoEventType type) {
		switch (ProfilerTimerFilter.SyntheticClass_1.$SwitchMap$org$apache$mina$core$session$IoEventType[type
				.ordinal()]) {
		case 1:
			if (this.profileMessageReceived) {
				return this.messageReceivedTimerWorker.getMinimum();
			}
			break;
		case 2:
			if (this.profileMessageSent) {
				return this.messageSentTimerWorker.getMinimum();
			}
			break;
		case 3:
			if (this.profileSessionCreated) {
				return this.sessionCreatedTimerWorker.getMinimum();
			}
			break;
		case 4:
			if (this.profileSessionOpened) {
				return this.sessionOpenedTimerWorker.getMinimum();
			}
			break;
		case 5:
			if (this.profileSessionIdle) {
				return this.sessionIdleTimerWorker.getMinimum();
			}
			break;
		case 6:
			if (this.profileSessionClosed) {
				return this.sessionClosedTimerWorker.getMinimum();
			}
		}

		throw new IllegalArgumentException("You are not monitoring this event.  Please add this event first.");
	}

	public long getMaximumTime(IoEventType type) {
		switch (ProfilerTimerFilter.SyntheticClass_1.$SwitchMap$org$apache$mina$core$session$IoEventType[type
				.ordinal()]) {
		case 1:
			if (this.profileMessageReceived) {
				return this.messageReceivedTimerWorker.getMaximum();
			}
			break;
		case 2:
			if (this.profileMessageSent) {
				return this.messageSentTimerWorker.getMaximum();
			}
			break;
		case 3:
			if (this.profileSessionCreated) {
				return this.sessionCreatedTimerWorker.getMaximum();
			}
			break;
		case 4:
			if (this.profileSessionOpened) {
				return this.sessionOpenedTimerWorker.getMaximum();
			}
			break;
		case 5:
			if (this.profileSessionIdle) {
				return this.sessionIdleTimerWorker.getMaximum();
			}
			break;
		case 6:
			if (this.profileSessionClosed) {
				return this.sessionClosedTimerWorker.getMaximum();
			}
		}

		throw new IllegalArgumentException("You are not monitoring this event.  Please add this event first.");
	}

	private long timeNow() {
		switch (ProfilerTimerFilter.SyntheticClass_1.$SwitchMap$java$util$concurrent$TimeUnit[this.timeUnit
				.ordinal()]) {
		case 1:
			return System.currentTimeMillis() / 1000L;
		case 2:
			return System.nanoTime() / 1000L;
		case 3:
			return System.nanoTime();
		default:
			return System.currentTimeMillis();
		}
	}

	private class TimerWorker {
		private final AtomicLong total = new AtomicLong();
		private final AtomicLong callsNumber = new AtomicLong();
		private final AtomicLong minimum = new AtomicLong();
		private final AtomicLong maximum = new AtomicLong();
		private final Object lock = new Object();

		public void addNewDuration(long duration) {
			this.callsNumber.incrementAndGet();
			this.total.addAndGet(duration);
			Object arg2 = this.lock;
			synchronized (this.lock) {
				if (duration < this.minimum.longValue()) {
					this.minimum.set(duration);
				}

				if (duration > this.maximum.longValue()) {
					this.maximum.set(duration);
				}

			}
		}

		public double getAverage() {
			Object arg0 = this.lock;
			synchronized (this.lock) {
				return (double) (this.total.longValue() / this.callsNumber.longValue());
			}
		}

		public long getCallsNumber() {
			return this.callsNumber.longValue();
		}

		public long getTotal() {
			return this.total.longValue();
		}

		public long getMinimum() {
			return this.minimum.longValue();
		}

		public long getMaximum() {
			return this.maximum.longValue();
		}
	}
}