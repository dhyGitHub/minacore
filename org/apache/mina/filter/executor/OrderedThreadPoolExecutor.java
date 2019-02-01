/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.executor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.core.session.IoEvent;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.executor.IoEventQueueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderedThreadPoolExecutor extends ThreadPoolExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderedThreadPoolExecutor.class);
	private static final int DEFAULT_INITIAL_THREAD_POOL_SIZE = 0;
	private static final int DEFAULT_MAX_THREAD_POOL = 16;
	private static final int DEFAULT_KEEP_ALIVE = 30;
	private static final IoSession EXIT_SIGNAL = new DummySession();
	private final AttributeKey TASKS_QUEUE;
	private final BlockingQueue<IoSession> waitingSessions;
	private final Set<OrderedThreadPoolExecutor.Worker> workers;
	private volatile int largestPoolSize;
	private final AtomicInteger idleWorkers;
	private long completedTaskCount;
	private volatile boolean shutdown;
	private final IoEventQueueHandler eventQueueHandler;

	public OrderedThreadPoolExecutor() {
		this(0, 16, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), (IoEventQueueHandler) null);
	}

	public OrderedThreadPoolExecutor(int maximumPoolSize) {
		this(0, maximumPoolSize, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(), (IoEventQueueHandler) null);
	}

	public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
		this(corePoolSize, maximumPoolSize, 30L, TimeUnit.SECONDS, Executors.defaultThreadFactory(),
				(IoEventQueueHandler) null);
	}

	public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(),
				(IoEventQueueHandler) null);
	}

	public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			IoEventQueueHandler eventQueueHandler) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), eventQueueHandler);
	}

	public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			ThreadFactory threadFactory) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, (IoEventQueueHandler) null);
	}

	public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			ThreadFactory threadFactory, IoEventQueueHandler eventQueueHandler) {
		super(0, 1, keepAliveTime, unit, new SynchronousQueue(), threadFactory, new AbortPolicy());
		this.TASKS_QUEUE = new AttributeKey(this.getClass(), "tasksQueue");
		this.waitingSessions = new LinkedBlockingQueue();
		this.workers = new HashSet();
		this.idleWorkers = new AtomicInteger();
		if (corePoolSize < 0) {
			throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
		} else if (maximumPoolSize != 0 && maximumPoolSize >= corePoolSize) {
			super.setCorePoolSize(corePoolSize);
			super.setMaximumPoolSize(maximumPoolSize);
			if (eventQueueHandler == null) {
				this.eventQueueHandler = IoEventQueueHandler.NOOP;
			} else {
				this.eventQueueHandler = eventQueueHandler;
			}

		} else {
			throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize);
		}
	}

	private OrderedThreadPoolExecutor.SessionTasksQueue getSessionTasksQueue(IoSession session) {
		OrderedThreadPoolExecutor.SessionTasksQueue queue = (OrderedThreadPoolExecutor.SessionTasksQueue) session
				.getAttribute(this.TASKS_QUEUE);
		if (queue == null) {
			queue = new OrderedThreadPoolExecutor.SessionTasksQueue();
			OrderedThreadPoolExecutor.SessionTasksQueue oldQueue = (OrderedThreadPoolExecutor.SessionTasksQueue) session
					.setAttributeIfAbsent(this.TASKS_QUEUE, queue);
			if (oldQueue != null) {
				queue = oldQueue;
			}
		}

		return queue;
	}

	public IoEventQueueHandler getQueueHandler() {
		return this.eventQueueHandler;
	}

	public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
	}

	private void addWorker() {
		Set arg0 = this.workers;
		synchronized (this.workers) {
			if (this.workers.size() < super.getMaximumPoolSize()) {
				OrderedThreadPoolExecutor.Worker worker = new OrderedThreadPoolExecutor.Worker();
				Thread thread = this.getThreadFactory().newThread(worker);
				this.idleWorkers.incrementAndGet();
				thread.start();
				this.workers.add(worker);
				if (this.workers.size() > this.largestPoolSize) {
					this.largestPoolSize = this.workers.size();
				}

			}
		}
	}

	private void addWorkerIfNecessary() {
		if (this.idleWorkers.get() == 0) {
			Set arg0 = this.workers;
			synchronized (this.workers) {
				if (this.workers.isEmpty() || this.idleWorkers.get() == 0) {
					this.addWorker();
				}
			}
		}

	}

	private void removeWorker() {
		Set arg0 = this.workers;
		synchronized (this.workers) {
			if (this.workers.size() > super.getCorePoolSize()) {
				this.waitingSessions.offer(EXIT_SIGNAL);
			}
		}
	}

	public int getMaximumPoolSize() {
		return super.getMaximumPoolSize();
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		if (maximumPoolSize > 0 && maximumPoolSize >= super.getCorePoolSize()) {
			Set arg1 = this.workers;
			synchronized (this.workers) {
				super.setMaximumPoolSize(maximumPoolSize);

				for (int difference = this.workers.size() - maximumPoolSize; difference > 0; --difference) {
					this.removeWorker();
				}

			}
		} else {
			throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize);
		}
	}

	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
		Set arg5 = this.workers;
		synchronized (this.workers) {
			while (!this.isTerminated()) {
				long waitTime = deadline - System.currentTimeMillis();
				if (waitTime <= 0L) {
					break;
				}

				this.workers.wait(waitTime);
			}
		}

		return this.isTerminated();
	}

	public boolean isShutdown() {
		return this.shutdown;
	}

	public boolean isTerminated() {
		if (!this.shutdown) {
			return false;
		} else {
			Set arg0 = this.workers;
			synchronized (this.workers) {
				return this.workers.isEmpty();
			}
		}
	}

	public void shutdown() {
		if (!this.shutdown) {
			this.shutdown = true;
			Set arg0 = this.workers;
			synchronized (this.workers) {
				for (int i = this.workers.size(); i > 0; --i) {
					this.waitingSessions.offer(EXIT_SIGNAL);
				}

			}
		}
	}

	public List<Runnable> shutdownNow() {
		this.shutdown();
		ArrayList answer = new ArrayList();

		while (true) {
			IoSession session;
			while ((session = (IoSession) this.waitingSessions.poll()) != null) {
				if (session == EXIT_SIGNAL) {
					this.waitingSessions.offer(EXIT_SIGNAL);
					Thread.yield();
				} else {
					OrderedThreadPoolExecutor.SessionTasksQueue sessionTasksQueue = (OrderedThreadPoolExecutor.SessionTasksQueue) session
							.getAttribute(this.TASKS_QUEUE);
					synchronized (sessionTasksQueue.tasksQueue) {
						Iterator i$ = sessionTasksQueue.tasksQueue.iterator();

						while (i$.hasNext()) {
							Runnable task = (Runnable) i$.next();
							this.getQueueHandler().polled(this, (IoEvent) task);
							answer.add(task);
						}

						sessionTasksQueue.tasksQueue.clear();
					}
				}
			}

			return answer;
		}
	}

	private void print(Queue<Runnable> queue, IoEvent event) {
		StringBuilder sb = new StringBuilder();
		sb.append("Adding event ").append(event.getType()).append(" to session ").append(event.getSession().getId());
		boolean first = true;
		sb.append("\nQueue : [");

		Runnable elem;
		for (Iterator i$ = queue.iterator(); i$.hasNext(); sb.append(((IoEvent) elem).getType()).append(", ")) {
			elem = (Runnable) i$.next();
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
		}

		sb.append("]\n");
		LOGGER.debug(sb.toString());
	}

	public void execute(Runnable task) {
		if (this.shutdown) {
			this.rejectTask(task);
		}

		this.checkTaskType(task);
		IoEvent event = (IoEvent) task;
		IoSession session = event.getSession();
		OrderedThreadPoolExecutor.SessionTasksQueue sessionTasksQueue = this.getSessionTasksQueue(session);
		Queue tasksQueue = sessionTasksQueue.tasksQueue;
		boolean offerEvent = this.eventQueueHandler.accept(this, event);
		boolean offerSession;
		if (offerEvent) {
			synchronized (tasksQueue) {
				tasksQueue.offer(event);
				if (sessionTasksQueue.processingCompleted) {
					sessionTasksQueue.processingCompleted = false;
					offerSession = true;
				} else {
					offerSession = false;
				}

				if (LOGGER.isDebugEnabled()) {
					this.print(tasksQueue, event);
				}
			}
		} else {
			offerSession = false;
		}

		if (offerSession) {
			this.waitingSessions.offer(session);
		}

		this.addWorkerIfNecessary();
		if (offerEvent) {
			this.eventQueueHandler.offered(this, event);
		}

	}

	private void rejectTask(Runnable task) {
		this.getRejectedExecutionHandler().rejectedExecution(task, this);
	}

	private void checkTaskType(Runnable task) {
		if (!(task instanceof IoEvent)) {
			throw new IllegalArgumentException("task must be an IoEvent or its subclass.");
		}
	}

	public int getActiveCount() {
		Set arg0 = this.workers;
		synchronized (this.workers) {
			return this.workers.size() - this.idleWorkers.get();
		}
	}

	public long getCompletedTaskCount() {
		Set arg0 = this.workers;
		synchronized (this.workers) {
			long answer = this.completedTaskCount;

			OrderedThreadPoolExecutor.Worker w;
			for (Iterator i$ = this.workers.iterator(); i$.hasNext(); answer += w.completedTaskCount.get()) {
				w = (OrderedThreadPoolExecutor.Worker) i$.next();
			}

			return answer;
		}
	}

	public int getLargestPoolSize() {
		return this.largestPoolSize;
	}

	public int getPoolSize() {
		Set arg0 = this.workers;
		synchronized (this.workers) {
			return this.workers.size();
		}
	}

	public long getTaskCount() {
		return this.getCompletedTaskCount();
	}

	public boolean isTerminating() {
		Set arg0 = this.workers;
		synchronized (this.workers) {
			return this.isShutdown() && !this.isTerminated();
		}
	}

	public int prestartAllCoreThreads() {
		int answer = 0;
		Set arg1 = this.workers;
		synchronized (this.workers) {
			for (int i = super.getCorePoolSize() - this.workers.size(); i > 0; --i) {
				this.addWorker();
				++answer;
			}

			return answer;
		}
	}

	public boolean prestartCoreThread() {
		Set arg0 = this.workers;
		synchronized (this.workers) {
			if (this.workers.size() < super.getCorePoolSize()) {
				this.addWorker();
				return true;
			} else {
				return false;
			}
		}
	}

	public BlockingQueue<Runnable> getQueue() {
		throw new UnsupportedOperationException();
	}

	public void purge() {
	}

	public boolean remove(Runnable task) {
		this.checkTaskType(task);
		IoEvent event = (IoEvent) task;
		IoSession session = event.getSession();
		OrderedThreadPoolExecutor.SessionTasksQueue sessionTasksQueue = (OrderedThreadPoolExecutor.SessionTasksQueue) session
				.getAttribute(this.TASKS_QUEUE);
		if (sessionTasksQueue == null) {
			return false;
		} else {
			Queue tasksQueue = sessionTasksQueue.tasksQueue;
			boolean removed;
			synchronized (tasksQueue) {
				removed = tasksQueue.remove(task);
			}

			if (removed) {
				this.getQueueHandler().polled(this, event);
			}

			return removed;
		}
	}

	public int getCorePoolSize() {
		return super.getCorePoolSize();
	}

	public void setCorePoolSize(int corePoolSize) {
		if (corePoolSize < 0) {
			throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
		} else if (corePoolSize > super.getMaximumPoolSize()) {
			throw new IllegalArgumentException("corePoolSize exceeds maximumPoolSize");
		} else {
			Set arg1 = this.workers;
			synchronized (this.workers) {
				if (super.getCorePoolSize() > corePoolSize) {
					for (int i = super.getCorePoolSize() - corePoolSize; i > 0; --i) {
						this.removeWorker();
					}
				}

				super.setCorePoolSize(corePoolSize);
			}
		}
	}

	private class SessionTasksQueue {
		private final Queue<Runnable> tasksQueue;
		private boolean processingCompleted;

		private SessionTasksQueue() {
			this.tasksQueue = new ConcurrentLinkedQueue();
			this.processingCompleted = true;
		}
	}

	private class Worker implements Runnable {
		private AtomicLong completedTaskCount;
		private Thread thread;

		private Worker() {
			this.completedTaskCount = new AtomicLong(0L);
		}

		public void run() {
			this.thread = Thread.currentThread();

			while (true) {
				boolean arg13 = false;

				try {
					arg13 = true;
					IoSession session = this.fetchSession();
					OrderedThreadPoolExecutor.this.idleWorkers.decrementAndGet();
					if (session == null) {
						synchronized (OrderedThreadPoolExecutor.this.workers) {
							if (OrderedThreadPoolExecutor.this.workers.size() > OrderedThreadPoolExecutor.this
									.getCorePoolSize()) {
								OrderedThreadPoolExecutor.this.workers.remove(this);
								arg13 = false;
								break;
							}
						}
					}

					if (session == OrderedThreadPoolExecutor.EXIT_SIGNAL) {
						arg13 = false;
						break;
					}

					try {
						if (session != null) {
							this.runTasks(OrderedThreadPoolExecutor.this.getSessionTasksQueue(session));
						}
					} finally {
						OrderedThreadPoolExecutor.this.idleWorkers.incrementAndGet();
					}
				} finally {
					if (arg13) {
						synchronized (OrderedThreadPoolExecutor.this.workers) {
							OrderedThreadPoolExecutor.this.workers.remove(this);
							OrderedThreadPoolExecutor.this.completedTaskCount = this.completedTaskCount.get();
							OrderedThreadPoolExecutor.this.workers.notifyAll();
						}
					}
				}
			}

			synchronized (OrderedThreadPoolExecutor.this.workers) {
				OrderedThreadPoolExecutor.this.workers.remove(this);
				OrderedThreadPoolExecutor.this.completedTaskCount = this.completedTaskCount.get();
				OrderedThreadPoolExecutor.this.workers.notifyAll();
			}
		}

		private IoSession fetchSession() {
			IoSession session = null;
			long currentTime = System.currentTimeMillis();
			long deadline = currentTime + OrderedThreadPoolExecutor.this.getKeepAliveTime(TimeUnit.MILLISECONDS);

			while (true) {
				try {
					long e = deadline - currentTime;
					if (e > 0L) {
						try {
							session = (IoSession) OrderedThreadPoolExecutor.this.waitingSessions.poll(e,
									TimeUnit.MILLISECONDS);
						} finally {
							if (session == null) {
								currentTime = System.currentTimeMillis();
							}

						}
					}

					return session;
				} catch (InterruptedException arg11) {
					;
				}
			}
		}

		private void runTasks(OrderedThreadPoolExecutor.SessionTasksQueue sessionTasksQueue) {
			while (true) {
				Queue tasksQueue = sessionTasksQueue.tasksQueue;
				Runnable task;
				synchronized (tasksQueue) {
					task = (Runnable) tasksQueue.poll();
					if (task == null) {
						sessionTasksQueue.processingCompleted = true;
						return;
					}
				}

				OrderedThreadPoolExecutor.this.eventQueueHandler.polled(OrderedThreadPoolExecutor.this, (IoEvent) task);
				this.runTask(task);
			}
		}

		private void runTask(Runnable task) {
			OrderedThreadPoolExecutor.this.beforeExecute(this.thread, task);
			boolean ran = false;

			try {
				task.run();
				ran = true;
				OrderedThreadPoolExecutor.this.afterExecute(task, (Throwable) null);
				this.completedTaskCount.incrementAndGet();
			} catch (RuntimeException arg3) {
				if (!ran) {
					OrderedThreadPoolExecutor.this.afterExecute(task, arg3);
				}

				throw arg3;
			}
		}
	}
}