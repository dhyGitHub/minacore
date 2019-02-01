/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.executor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.session.IoEvent;
import org.apache.mina.filter.executor.IoEventQueueHandler;

public class UnorderedThreadPoolExecutor extends ThreadPoolExecutor {
	private static final Runnable EXIT_SIGNAL = new Runnable() {
		public void run() {
			throw new Error("This method shouldn\'t be called. Please file a bug report.");
		}
	};
	private final Set<UnorderedThreadPoolExecutor.Worker> workers;
	private volatile int corePoolSize;
	private volatile int maximumPoolSize;
	private volatile int largestPoolSize;
	private final AtomicInteger idleWorkers;
	private long completedTaskCount;
	private volatile boolean shutdown;
	private final IoEventQueueHandler queueHandler;

	public UnorderedThreadPoolExecutor() {
		this(16);
	}

	public UnorderedThreadPoolExecutor(int maximumPoolSize) {
		this(0, maximumPoolSize);
	}

	public UnorderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
		this(corePoolSize, maximumPoolSize, 30L, TimeUnit.SECONDS);
	}

	public UnorderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory());
	}

	public UnorderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			IoEventQueueHandler queueHandler) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), queueHandler);
	}

	public UnorderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			ThreadFactory threadFactory) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, (IoEventQueueHandler) null);
	}

	public UnorderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			ThreadFactory threadFactory, IoEventQueueHandler queueHandler) {
		super(0, 1, keepAliveTime, unit, new LinkedBlockingQueue(), threadFactory, new AbortPolicy());
		this.workers = new HashSet();
		this.idleWorkers = new AtomicInteger();
		if (corePoolSize < 0) {
			throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
		} else if (maximumPoolSize != 0 && maximumPoolSize >= corePoolSize) {
			if (queueHandler == null) {
				queueHandler = IoEventQueueHandler.NOOP;
			}

			this.corePoolSize = corePoolSize;
			this.maximumPoolSize = maximumPoolSize;
			this.queueHandler = queueHandler;
		} else {
			throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize);
		}
	}

	public IoEventQueueHandler getQueueHandler() {
		return this.queueHandler;
	}

	public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
	}

	private void addWorker() {
		Set arg0 = this.workers;
		synchronized (this.workers) {
			if (this.workers.size() < this.maximumPoolSize) {
				UnorderedThreadPoolExecutor.Worker worker = new UnorderedThreadPoolExecutor.Worker(null);
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
			if (this.workers.size() > this.corePoolSize) {
				this.getQueue().offer(EXIT_SIGNAL);
			}
		}
	}

	public int getMaximumPoolSize() {
		return this.maximumPoolSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		if (maximumPoolSize > 0 && maximumPoolSize >= this.corePoolSize) {
			Set arg1 = this.workers;
			synchronized (this.workers) {
				this.maximumPoolSize = maximumPoolSize;

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
					this.getQueue().offer(EXIT_SIGNAL);
				}

			}
		}
	}

	public List<Runnable> shutdownNow() {
		this.shutdown();
		ArrayList answer = new ArrayList();

		Runnable task;
		while ((task = (Runnable) this.getQueue().poll()) != null) {
			if (task == EXIT_SIGNAL) {
				this.getQueue().offer(EXIT_SIGNAL);
				Thread.yield();
			} else {
				this.getQueueHandler().polled(this, (IoEvent) task);
				answer.add(task);
			}
		}

		return answer;
	}

	public void execute(Runnable task) {
		if (this.shutdown) {
			this.rejectTask(task);
		}

		this.checkTaskType(task);
		IoEvent e = (IoEvent) task;
		boolean offeredEvent = this.queueHandler.accept(this, e);
		if (offeredEvent) {
			this.getQueue().offer(e);
		}

		this.addWorkerIfNecessary();
		if (offeredEvent) {
			this.queueHandler.offered(this, e);
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

			UnorderedThreadPoolExecutor.Worker w;
			for (Iterator i$ = this.workers.iterator(); i$.hasNext(); answer += w.completedTaskCount.get()) {
				w = (UnorderedThreadPoolExecutor.Worker) i$.next();
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
			for (int i = this.corePoolSize - this.workers.size(); i > 0; --i) {
				this.addWorker();
				++answer;
			}

			return answer;
		}
	}

	public boolean prestartCoreThread() {
		Set arg0 = this.workers;
		synchronized (this.workers) {
			if (this.workers.size() < this.corePoolSize) {
				this.addWorker();
				return true;
			} else {
				return false;
			}
		}
	}

	public void purge() {
	}

	public boolean remove(Runnable task) {
		boolean removed = super.remove(task);
		if (removed) {
			this.getQueueHandler().polled(this, (IoEvent) task);
		}

		return removed;
	}

	public int getCorePoolSize() {
		return this.corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		if (corePoolSize < 0) {
			throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
		} else if (corePoolSize > this.maximumPoolSize) {
			throw new IllegalArgumentException("corePoolSize exceeds maximumPoolSize");
		} else {
			Set arg1 = this.workers;
			synchronized (this.workers) {
				if (this.corePoolSize > corePoolSize) {
					for (int i = this.corePoolSize - corePoolSize; i > 0; --i) {
						this.removeWorker();
					}
				}

				this.corePoolSize = corePoolSize;
			}
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
					Runnable task = this.fetchTask();
					UnorderedThreadPoolExecutor.this.idleWorkers.decrementAndGet();
					if (task == null) {
						synchronized (UnorderedThreadPoolExecutor.this.workers) {
							if (UnorderedThreadPoolExecutor.this.workers
									.size() > UnorderedThreadPoolExecutor.this.corePoolSize) {
								UnorderedThreadPoolExecutor.this.workers.remove(this);
								arg13 = false;
								break;
							}
						}
					}

					if (task == UnorderedThreadPoolExecutor.EXIT_SIGNAL) {
						arg13 = false;
						break;
					}

					try {
						if (task != null) {
							UnorderedThreadPoolExecutor.this.queueHandler.polled(UnorderedThreadPoolExecutor.this,
									(IoEvent) task);
							this.runTask(task);
						}
					} finally {
						UnorderedThreadPoolExecutor.this.idleWorkers.incrementAndGet();
					}
				} finally {
					if (arg13) {
						synchronized (UnorderedThreadPoolExecutor.this.workers) {
							UnorderedThreadPoolExecutor.this.workers.remove(this);
							UnorderedThreadPoolExecutor.this.completedTaskCount = this.completedTaskCount.get();
							UnorderedThreadPoolExecutor.this.workers.notifyAll();
						}
					}
				}
			}

			synchronized (UnorderedThreadPoolExecutor.this.workers) {
				UnorderedThreadPoolExecutor.this.workers.remove(this);
				UnorderedThreadPoolExecutor.this.completedTaskCount = this.completedTaskCount.get();
				UnorderedThreadPoolExecutor.this.workers.notifyAll();
			}
		}

		private Runnable fetchTask() {
			Runnable task = null;
			long currentTime = System.currentTimeMillis();
			long deadline = currentTime + UnorderedThreadPoolExecutor.this.getKeepAliveTime(TimeUnit.MILLISECONDS);

			while (true) {
				try {
					long e = deadline - currentTime;
					if (e > 0L) {
						try {
							task = (Runnable) UnorderedThreadPoolExecutor.this.getQueue().poll(e,
									TimeUnit.MILLISECONDS);
						} finally {
							if (task == null) {
								currentTime = System.currentTimeMillis();
							}

						}
					}

					return task;
				} catch (InterruptedException arg11) {
					;
				}
			}
		}

		private void runTask(Runnable task) {
			UnorderedThreadPoolExecutor.this.beforeExecute(this.thread, task);
			boolean ran = false;

			try {
				task.run();
				ran = true;
				UnorderedThreadPoolExecutor.this.afterExecute(task, (Throwable) null);
				this.completedTaskCount.incrementAndGet();
			} catch (RuntimeException arg3) {
				if (!ran) {
					UnorderedThreadPoolExecutor.this.afterExecute(task, arg3);
				}

				throw arg3;
			}
		}
	}
}