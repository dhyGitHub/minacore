/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.buffer.AbstractIoBuffer;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.IoBufferAllocator;

public class CachedBufferAllocator implements IoBufferAllocator {
	private static final int DEFAULT_MAX_POOL_SIZE = 8;
	private static final int DEFAULT_MAX_CACHED_BUFFER_SIZE = 262144;
	private final int maxPoolSize;
	private final int maxCachedBufferSize;
	private final ThreadLocal<Map<Integer, Queue<CachedBufferAllocator.CachedBuffer>>> heapBuffers;
	private final ThreadLocal<Map<Integer, Queue<CachedBufferAllocator.CachedBuffer>>> directBuffers;

	public CachedBufferAllocator() {
		this(8, 262144);
	}

	public CachedBufferAllocator(int maxPoolSize, int maxCachedBufferSize) {
		if (maxPoolSize < 0) {
			throw new IllegalArgumentException("maxPoolSize: " + maxPoolSize);
		} else if (maxCachedBufferSize < 0) {
			throw new IllegalArgumentException("maxCachedBufferSize: " + maxCachedBufferSize);
		} else {
			this.maxPoolSize = maxPoolSize;
			this.maxCachedBufferSize = maxCachedBufferSize;
			this.heapBuffers = new ThreadLocal() {
				protected Map<Integer, Queue<CachedBufferAllocator.CachedBuffer>> initialValue() {
					return CachedBufferAllocator.this.newPoolMap();
				}
			};
			this.directBuffers = new ThreadLocal() {
				protected Map<Integer, Queue<CachedBufferAllocator.CachedBuffer>> initialValue() {
					return CachedBufferAllocator.this.newPoolMap();
				}
			};
		}
	}

	public int getMaxPoolSize() {
		return this.maxPoolSize;
	}

	public int getMaxCachedBufferSize() {
		return this.maxCachedBufferSize;
	}

	Map<Integer, Queue<CachedBufferAllocator.CachedBuffer>> newPoolMap() {
		HashMap poolMap = new HashMap();

		for (int i = 0; i < 31; ++i) {
			poolMap.put(Integer.valueOf(1 << i), new ConcurrentLinkedQueue());
		}

		poolMap.put(Integer.valueOf(0), new ConcurrentLinkedQueue());
		poolMap.put(Integer.valueOf(Integer.MAX_VALUE), new ConcurrentLinkedQueue());
		return poolMap;
	}

	public IoBuffer allocate(int requestedCapacity, boolean direct) {
		int actualCapacity = IoBuffer.normalizeCapacity(requestedCapacity);
		IoBuffer buf;
		if (this.maxCachedBufferSize != 0 && actualCapacity > this.maxCachedBufferSize) {
			if (direct) {
				buf = this.wrap(ByteBuffer.allocateDirect(actualCapacity));
			} else {
				buf = this.wrap(ByteBuffer.allocate(actualCapacity));
			}
		} else {
			Queue pool;
			if (direct) {
				pool = (Queue) ((Map) this.directBuffers.get()).get(Integer.valueOf(actualCapacity));
			} else {
				pool = (Queue) ((Map) this.heapBuffers.get()).get(Integer.valueOf(actualCapacity));
			}

			buf = (IoBuffer) pool.poll();
			if (buf != null) {
				buf.clear();
				buf.setAutoExpand(false);
				buf.order(ByteOrder.BIG_ENDIAN);
			} else if (direct) {
				buf = this.wrap(ByteBuffer.allocateDirect(actualCapacity));
			} else {
				buf = this.wrap(ByteBuffer.allocate(actualCapacity));
			}
		}

		buf.limit(requestedCapacity);
		return buf;
	}

	public ByteBuffer allocateNioBuffer(int capacity, boolean direct) {
		return this.allocate(capacity, direct).buf();
	}

	public IoBuffer wrap(ByteBuffer nioBuffer) {
		return new CachedBufferAllocator.CachedBuffer(nioBuffer);
	}

	public void dispose() {
	}

	private class CachedBuffer extends AbstractIoBuffer {
		private final Thread ownerThread = Thread.currentThread();
		private ByteBuffer buf;

		protected CachedBuffer(ByteBuffer buf) {
			super(CachedBufferAllocator.this, buf.capacity());
			this.buf = buf;
			buf.order(ByteOrder.BIG_ENDIAN);
		}

		protected CachedBuffer(CachedBufferAllocator.CachedBuffer parent, ByteBuffer buf) {
			super(parent);
			this.buf = buf;
		}

		public ByteBuffer buf() {
			if (this.buf == null) {
				throw new IllegalStateException("Buffer has been freed already.");
			} else {
				return this.buf;
			}
		}

		protected void buf(ByteBuffer buf) {
			ByteBuffer oldBuf = this.buf;
			this.buf = buf;
			this.free(oldBuf);
		}

		protected IoBuffer duplicate0() {
			return CachedBufferAllocator.this.new CachedBuffer(this, this.buf().duplicate());
		}

		protected IoBuffer slice0() {
			return CachedBufferAllocator.this.new CachedBuffer(this, this.buf().slice());
		}

		protected IoBuffer asReadOnlyBuffer0() {
			return CachedBufferAllocator.this.new CachedBuffer(this, this.buf().asReadOnlyBuffer());
		}

		public byte[] array() {
			return this.buf().array();
		}

		public int arrayOffset() {
			return this.buf().arrayOffset();
		}

		public boolean hasArray() {
			return this.buf().hasArray();
		}

		public void free() {
			this.free(this.buf);
			this.buf = null;
		}

		private void free(ByteBuffer oldBuf) {
			if (oldBuf != null
					&& (CachedBufferAllocator.this.maxCachedBufferSize == 0
							|| oldBuf.capacity() <= CachedBufferAllocator.this.maxCachedBufferSize)
					&& !oldBuf.isReadOnly() && !this.isDerived() && Thread.currentThread() == this.ownerThread) {
				Queue pool;
				if (oldBuf.isDirect()) {
					pool = (Queue) ((Map) CachedBufferAllocator.this.directBuffers.get())
							.get(Integer.valueOf(oldBuf.capacity()));
				} else {
					pool = (Queue) ((Map) CachedBufferAllocator.this.heapBuffers.get())
							.get(Integer.valueOf(oldBuf.capacity()));
				}

				if (pool != null) {
					if (CachedBufferAllocator.this.maxPoolSize == 0
							|| pool.size() < CachedBufferAllocator.this.maxPoolSize) {
						pool.offer(CachedBufferAllocator.this.new CachedBuffer(oldBuf));
					}

				}
			}
		}
	}
}