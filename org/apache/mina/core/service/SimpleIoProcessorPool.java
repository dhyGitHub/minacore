/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.service;

import java.lang.reflect.Constructor;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.write.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleIoProcessorPool<S extends AbstractIoSession> implements IoProcessor<S> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleIoProcessorPool.class);
	private static final int DEFAULT_SIZE = Runtime.getRuntime().availableProcessors() + 1;
	private static final AttributeKey PROCESSOR = new AttributeKey(SimpleIoProcessorPool.class, "processor");
	private final IoProcessor<S>[] pool;
	private final Executor executor;
	private final boolean createdExecutor;
	private final Object disposalLock;
	private volatile boolean disposing;
	private volatile boolean disposed;

	public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType) {
		this(processorType, (Executor) null, DEFAULT_SIZE, (SelectorProvider) null);
	}

	public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType, int size) {
		this(processorType, (Executor) null, size, (SelectorProvider) null);
	}

	public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType, int size,
			SelectorProvider selectorProvider) {
		this(processorType, (Executor) null, size, selectorProvider);
	}

	public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType, Executor executor) {
		this(processorType, executor, DEFAULT_SIZE, (SelectorProvider) null);
	}

	public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType, Executor executor, int size,
			SelectorProvider selectorProvider) {
		this.disposalLock = new Object();
		if (processorType == null) {
			throw new IllegalArgumentException("processorType");
		} else if (size <= 0) {
			throw new IllegalArgumentException("size: " + size + " (expected: positive integer)");
		} else {
			this.createdExecutor = executor == null;
			if (this.createdExecutor) {
				this.executor = Executors.newCachedThreadPool();
				((ThreadPoolExecutor) this.executor).setRejectedExecutionHandler(new CallerRunsPolicy());
			} else {
				this.executor = executor;
			}

			this.pool = new IoProcessor[size];
			boolean success = false;
			Constructor processorConstructor = null;
			boolean usesExecutorArg = true;

			try {
				try {
					try {
						processorConstructor = processorType.getConstructor(new Class[] { ExecutorService.class });
						this.pool[0] = (IoProcessor) processorConstructor.newInstance(new Object[] { this.executor });
					} catch (NoSuchMethodException arg21) {
						try {
							if (selectorProvider == null) {
								processorConstructor = processorType.getConstructor(new Class[] { Executor.class });
								this.pool[0] = (IoProcessor) processorConstructor
										.newInstance(new Object[] { this.executor });
							} else {
								processorConstructor = processorType
										.getConstructor(new Class[] { Executor.class, SelectorProvider.class });
								this.pool[0] = (IoProcessor) processorConstructor
										.newInstance(new Object[] { this.executor, selectorProvider });
							}
						} catch (NoSuchMethodException arg20) {
							try {
								processorConstructor = processorType.getConstructor(new Class[0]);
								usesExecutorArg = false;
								this.pool[0] = (IoProcessor) processorConstructor.newInstance(new Object[0]);
							} catch (NoSuchMethodException arg19) {
								;
							}
						}
					}
				} catch (RuntimeException arg22) {
					LOGGER.error("Cannot create an IoProcessor :{}", arg22.getMessage());
					throw arg22;
				} catch (Exception arg23) {
					String e = "Failed to create a new instance of " + processorType.getName() + ":"
							+ arg23.getMessage();
					LOGGER.error(e, arg23);
					throw new RuntimeIoException(e, arg23);
				}

				if (processorConstructor == null) {
					String arg25 = processorType + " must have a public constructor with one "
							+ ExecutorService.class.getSimpleName() + " parameter, a public constructor with one "
							+ Executor.class.getSimpleName() + " parameter or a public default constructor.";
					LOGGER.error(arg25);
					throw new IllegalArgumentException(arg25);
				}

				for (int i = 1; i < this.pool.length; ++i) {
					try {
						if (usesExecutorArg) {
							if (selectorProvider == null) {
								this.pool[i] = (IoProcessor) processorConstructor
										.newInstance(new Object[] { this.executor });
							} else {
								this.pool[i] = (IoProcessor) processorConstructor
										.newInstance(new Object[] { this.executor, selectorProvider });
							}
						} else {
							this.pool[i] = (IoProcessor) processorConstructor.newInstance(new Object[0]);
						}
					} catch (Exception arg18) {
						;
					}
				}

				success = true;
			} finally {
				if (!success) {
					this.dispose();
				}

			}

		}
	}

	public final void add(S session) {
		this.getProcessor(session).add(session);
	}

	public final void flush(S session) {
		this.getProcessor(session).flush(session);
	}

	public final void write(S session, WriteRequest writeRequest) {
		this.getProcessor(session).write(session, writeRequest);
	}

	public final void remove(S session) {
		this.getProcessor(session).remove(session);
	}

	public final void updateTrafficControl(S session) {
		this.getProcessor(session).updateTrafficControl(session);
	}

	public boolean isDisposed() {
		return this.disposed;
	}

	public boolean isDisposing() {
		return this.disposing;
	}

	public final void dispose() {
		if (!this.disposed) {
			Object arg0 = this.disposalLock;
			synchronized (this.disposalLock) {
				if (!this.disposing) {
					this.disposing = true;
					IoProcessor[] arr$ = this.pool;
					int len$ = arr$.length;

					for (int i$ = 0; i$ < len$; ++i$) {
						IoProcessor ioProcessor = arr$[i$];
						if (ioProcessor != null && !ioProcessor.isDisposing()) {
							try {
								ioProcessor.dispose();
							} catch (Exception arg7) {
								LOGGER.warn("Failed to dispose the {} IoProcessor.",
										ioProcessor.getClass().getSimpleName(), arg7);
							}
						}
					}

					if (this.createdExecutor) {
						((ExecutorService) this.executor).shutdown();
					}
				}

				Arrays.fill(this.pool, (Object) null);
				this.disposed = true;
			}
		}
	}

	private IoProcessor<S> getProcessor(S session) {
		IoProcessor processor = (IoProcessor) session.getAttribute(PROCESSOR);
		if (processor == null) {
			if (this.disposed || this.disposing) {
				throw new IllegalStateException("A disposed processor cannot be accessed.");
			}

			processor = this.pool[Math.abs((int) session.getId()) % this.pool.length];
			if (processor == null) {
				throw new IllegalStateException("A disposed processor cannot be accessed.");
			}

			session.setAttributeIfAbsent(PROCESSOR, processor);
		}

		return processor;
	}
}