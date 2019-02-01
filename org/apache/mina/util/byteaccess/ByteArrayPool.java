/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util.byteaccess;

import java.util.ArrayList;
import java.util.Stack;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.byteaccess.BufferByteArray;
import org.apache.mina.util.byteaccess.ByteArray;
import org.apache.mina.util.byteaccess.ByteArrayFactory;

public class ByteArrayPool implements ByteArrayFactory {
	private final int MAX_BITS = 32;
	private boolean freed;
	private final boolean direct;
	private ArrayList<Stack<ByteArrayPool.DirectBufferByteArray>> freeBuffers;
	private int freeBufferCount = 0;
	private long freeMemory = 0L;
	private final int maxFreeBuffers;
	private final int maxFreeMemory;

	public ByteArrayPool(boolean direct, int maxFreeBuffers, int maxFreeMemory) {
		this.direct = direct;
		this.freeBuffers = new ArrayList();

		for (int i = 0; i < 32; ++i) {
			this.freeBuffers.add(new Stack());
		}

		this.maxFreeBuffers = maxFreeBuffers;
		this.maxFreeMemory = maxFreeMemory;
		this.freed = false;
	}

	public ByteArray create(int size) {
		if (size < 1) {
			throw new IllegalArgumentException("Buffer size must be at least 1: " + size);
		} else {
			int bits = this.bits(size);
			synchronized (this) {
				if (!((Stack) this.freeBuffers.get(bits)).isEmpty()) {
					ByteArrayPool.DirectBufferByteArray bbSize1 = (ByteArrayPool.DirectBufferByteArray) ((Stack) this.freeBuffers
							.get(bits)).pop();
					bbSize1.setFreed(false);
					bbSize1.getSingleIoBuffer().limit(size);
					return bbSize1;
				}
			}

			int bbSize = 1 << bits;
			IoBuffer bb = IoBuffer.allocate(bbSize, this.direct);
			bb.limit(size);
			ByteArrayPool.DirectBufferByteArray ba = new ByteArrayPool.DirectBufferByteArray(bb);
			ba.setFreed(false);
			return ba;
		}
	}

	private int bits(int index) {
		int bits;
		for (bits = 0; 1 << bits < index; ++bits) {
			;
		}

		return bits;
	}

	public void free() {
		synchronized (this) {
			if (this.freed) {
				throw new IllegalStateException("Already freed.");
			} else {
				this.freed = true;
				this.freeBuffers.clear();
				this.freeBuffers = null;
			}
		}
	}

	private class DirectBufferByteArray extends BufferByteArray {
		private boolean freed;

		public DirectBufferByteArray(IoBuffer bb) {
			super(bb);
		}

		public void setFreed(boolean freed) {
			this.freed = freed;
		}

		public void free() {
			synchronized (this) {
				if (this.freed) {
					throw new IllegalStateException("Already freed.");
				}

				this.freed = true;
			}

			int bits = ByteArrayPool.this.bits(this.last());
			ByteArrayPool arg1 = ByteArrayPool.this;
			synchronized (ByteArrayPool.this) {
				if (ByteArrayPool.this.freeBuffers != null
						&& ByteArrayPool.this.freeBufferCount < ByteArrayPool.this.maxFreeBuffers
						&& ByteArrayPool.this.freeMemory
								+ (long) this.last() <= (long) ByteArrayPool.this.maxFreeMemory) {
					((Stack) ByteArrayPool.this.freeBuffers.get(bits)).push(this);
					ByteArrayPool.this.freeBufferCount++;
					ByteArrayPool.this.freeMemory = (long) this.last();
				}
			}
		}
	}
}