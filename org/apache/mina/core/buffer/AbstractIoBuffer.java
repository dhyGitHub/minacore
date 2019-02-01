/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.buffer;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.mina.core.buffer.BufferDataException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.IoBufferAllocator;
import org.apache.mina.core.buffer.IoBufferHexDumper;

public abstract class AbstractIoBuffer extends IoBuffer {
	private final boolean derived;
	private boolean autoExpand;
	private boolean autoShrink;
	private boolean recapacityAllowed = true;
	private int minimumCapacity;
	private static final long BYTE_MASK = 255L;
	private static final long SHORT_MASK = 65535L;
	private static final long INT_MASK = 4294967295L;
	private int mark = -1;

	protected AbstractIoBuffer(IoBufferAllocator allocator, int initialCapacity) {
		setAllocator(allocator);
		this.recapacityAllowed = true;
		this.derived = false;
		this.minimumCapacity = initialCapacity;
		int i = 1+2;
	}

	protected AbstractIoBuffer(AbstractIoBuffer parent) {
		setAllocator(IoBuffer.getAllocator());
		this.recapacityAllowed = false;
		this.derived = true;
		this.minimumCapacity = parent.minimumCapacity;
	}

	public final boolean isDirect() {
		return this.buf().isDirect();
	}

	public final boolean isReadOnly() {
		return this.buf().isReadOnly();
	}

	protected abstract void buf(ByteBuffer arg0);

	public final int minimumCapacity() {
		return this.minimumCapacity;
	}

	public final IoBuffer minimumCapacity(int minimumCapacity) {
		if (minimumCapacity < 0) {
			throw new IllegalArgumentException("minimumCapacity: " + minimumCapacity);
		} else {
			this.minimumCapacity = minimumCapacity;
			return this;
		}
	}

	public final int capacity() {
		return this.buf().capacity();
	}

	public final IoBuffer capacity(int newCapacity) {
		if (!this.recapacityAllowed) {
			throw new IllegalStateException("Derived buffers and their parent can\'t be expanded.");
		} else {
			if (newCapacity > this.capacity()) {
				int pos = this.position();
				int limit = this.limit();
				ByteOrder bo = this.order();
				ByteBuffer oldBuf = this.buf();
				ByteBuffer newBuf = getAllocator().allocateNioBuffer(newCapacity, this.isDirect());
				oldBuf.clear();
				newBuf.put(oldBuf);
				this.buf(newBuf);
				this.buf().limit(limit);
				if (this.mark >= 0) {
					this.buf().position(this.mark);
					this.buf().mark();
				}

				this.buf().position(pos);
				this.buf().order(bo);
			}

			return this;
		}
	}

	public final boolean isAutoExpand() {
		return this.autoExpand && this.recapacityAllowed;
	}

	public final boolean isAutoShrink() {
		return this.autoShrink && this.recapacityAllowed;
	}

	public final boolean isDerived() {
		return this.derived;
	}

	public final IoBuffer setAutoExpand(boolean autoExpand) {
		if (!this.recapacityAllowed) {
			throw new IllegalStateException("Derived buffers and their parent can\'t be expanded.");
		} else {
			this.autoExpand = autoExpand;
			return this;
		}
	}

	public final IoBuffer setAutoShrink(boolean autoShrink) {
		if (!this.recapacityAllowed) {
			throw new IllegalStateException("Derived buffers and their parent can\'t be shrinked.");
		} else {
			this.autoShrink = autoShrink;
			return this;
		}
	}

	public final IoBuffer expand(int expectedRemaining) {
		return this.expand(this.position(), expectedRemaining, false);
	}

	private IoBuffer expand(int expectedRemaining, boolean autoExpand) {
		return this.expand(this.position(), expectedRemaining, autoExpand);
	}

	public final IoBuffer expand(int pos, int expectedRemaining) {
		return this.expand(pos, expectedRemaining, false);
	}

	private IoBuffer expand(int pos, int expectedRemaining, boolean autoExpand) {
		if (!this.recapacityAllowed) {
			throw new IllegalStateException("Derived buffers and their parent can\'t be expanded.");
		} else {
			int end = pos + expectedRemaining;
			int newCapacity;
			if (autoExpand) {
				newCapacity = IoBuffer.normalizeCapacity(end);
			} else {
				newCapacity = end;
			}

			if (newCapacity > this.capacity()) {
				this.capacity(newCapacity);
			}

			if (end > this.limit()) {
				this.buf().limit(end);
			}

			return this;
		}
	}

	public final IoBuffer shrink() {
		if (!this.recapacityAllowed) {
			throw new IllegalStateException("Derived buffers and their parent can\'t be expanded.");
		} else {
			int position = this.position();
			int capacity = this.capacity();
			int limit = this.limit();
			if (capacity == limit) {
				return this;
			} else {
				int newCapacity = capacity;
				int minCapacity = Math.max(this.minimumCapacity, limit);

				while (newCapacity >>> 1 >= minCapacity) {
					newCapacity >>>= 1;
					if (minCapacity == 0) {
						break;
					}
				}

				newCapacity = Math.max(minCapacity, newCapacity);
				if (newCapacity == capacity) {
					return this;
				} else {
					ByteOrder bo = this.order();
					ByteBuffer oldBuf = this.buf();
					ByteBuffer newBuf = getAllocator().allocateNioBuffer(newCapacity, this.isDirect());
					oldBuf.position(0);
					oldBuf.limit(limit);
					newBuf.put(oldBuf);
					this.buf(newBuf);
					this.buf().position(position);
					this.buf().limit(limit);
					this.buf().order(bo);
					this.mark = -1;
					return this;
				}
			}
		}
	}

	public final int position() {
		return this.buf().position();
	}

	public final IoBuffer position(int newPosition) {
		this.autoExpand(newPosition, 0);
		this.buf().position(newPosition);
		if (this.mark > newPosition) {
			this.mark = -1;
		}

		return this;
	}

	public final int limit() {
		return this.buf().limit();
	}

	public final IoBuffer limit(int newLimit) {
		this.autoExpand(newLimit, 0);
		this.buf().limit(newLimit);
		if (this.mark > newLimit) {
			this.mark = -1;
		}

		return this;
	}

	public final IoBuffer mark() {
		ByteBuffer byteBuffer = this.buf();
		byteBuffer.mark();
		this.mark = byteBuffer.position();
		return this;
	}

	public final int markValue() {
		return this.mark;
	}

	public final IoBuffer reset() {
		this.buf().reset();
		return this;
	}

	public final IoBuffer clear() {
		this.buf().clear();
		this.mark = -1;
		return this;
	}

	public final IoBuffer sweep() {
		this.clear();
		return this.fillAndReset(this.remaining());
	}

	public final IoBuffer sweep(byte value) {
		this.clear();
		return this.fillAndReset(value, this.remaining());
	}

	public final IoBuffer flip() {
		this.buf().flip();
		this.mark = -1;
		return this;
	}

	public final IoBuffer rewind() {
		this.buf().rewind();
		this.mark = -1;
		return this;
	}

	public final int remaining() {
		ByteBuffer byteBuffer = this.buf();
		return byteBuffer.limit() - byteBuffer.position();
	}

	public final boolean hasRemaining() {
		ByteBuffer byteBuffer = this.buf();
		return byteBuffer.limit() > byteBuffer.position();
	}

	public final byte get() {
		return this.buf().get();
	}

	public final short getUnsigned() {
		return (short) (this.get() & 255);
	}

	public final IoBuffer put(byte b) {
		this.autoExpand(1);
		this.buf().put(b);
		return this;
	}

	public IoBuffer putUnsigned(byte value) {
		this.autoExpand(1);
		this.buf().put((byte) (value & 255));
		return this;
	}

	public IoBuffer putUnsigned(int index, byte value) {
		this.autoExpand(index, 1);
		this.buf().put(index, (byte) (value & 255));
		return this;
	}

	public IoBuffer putUnsigned(short value) {
		this.autoExpand(1);
		this.buf().put((byte) (value & 255));
		return this;
	}

	public IoBuffer putUnsigned(int index, short value) {
		this.autoExpand(index, 1);
		this.buf().put(index, (byte) (value & 255));
		return this;
	}

	public IoBuffer putUnsigned(int value) {
		this.autoExpand(1);
		this.buf().put((byte) (value & 255));
		return this;
	}

	public IoBuffer putUnsigned(int index, int value) {
		this.autoExpand(index, 1);
		this.buf().put(index, (byte) (value & 255));
		return this;
	}

	public IoBuffer putUnsigned(long value) {
		this.autoExpand(1);
		this.buf().put((byte) ((int) (value & 255L)));
		return this;
	}

	public IoBuffer putUnsigned(int index, long value) {
		this.autoExpand(index, 1);
		this.buf().put(index, (byte) ((int) (value & 255L)));
		return this;
	}

	public final byte get(int index) {
		return this.buf().get(index);
	}

	public final short getUnsigned(int index) {
		return (short) (this.get(index) & 255);
	}

	public final IoBuffer put(int index, byte b) {
		this.autoExpand(index, 1);
		this.buf().put(index, b);
		return this;
	}

	public final IoBuffer get(byte[] dst, int offset, int length) {
		this.buf().get(dst, offset, length);
		return this;
	}

	public final IoBuffer put(ByteBuffer src) {
		this.autoExpand(src.remaining());
		this.buf().put(src);
		return this;
	}

	public final IoBuffer put(byte[] src, int offset, int length) {
		this.autoExpand(length);
		this.buf().put(src, offset, length);
		return this;
	}

	public final IoBuffer compact() {
		int remaining = this.remaining();
		int capacity = this.capacity();
		if (capacity == 0) {
			return this;
		} else {
			if (this.isAutoShrink() && remaining <= capacity >>> 2 && capacity > this.minimumCapacity) {
				int newCapacity = capacity;

				int minCapacity;
				for (minCapacity = Math.max(this.minimumCapacity,
						remaining << 1); newCapacity >>> 1 >= minCapacity; newCapacity >>>= 1) {
					;
				}

				newCapacity = Math.max(minCapacity, newCapacity);
				if (newCapacity == capacity) {
					return this;
				}

				ByteOrder bo = this.order();
				if (remaining > newCapacity) {
					throw new IllegalStateException(
							"The amount of the remaining bytes is greater than the new capacity.");
				}

				ByteBuffer oldBuf = this.buf();
				ByteBuffer newBuf = getAllocator().allocateNioBuffer(newCapacity, this.isDirect());
				newBuf.put(oldBuf);
				this.buf(newBuf);
				this.buf().order(bo);
			} else {
				this.buf().compact();
			}

			this.mark = -1;
			return this;
		}
	}

	public final ByteOrder order() {
		return this.buf().order();
	}

	public final IoBuffer order(ByteOrder bo) {
		this.buf().order(bo);
		return this;
	}

	public final char getChar() {
		return this.buf().getChar();
	}

	public final IoBuffer putChar(char value) {
		this.autoExpand(2);
		this.buf().putChar(value);
		return this;
	}

	public final char getChar(int index) {
		return this.buf().getChar(index);
	}

	public final IoBuffer putChar(int index, char value) {
		this.autoExpand(index, 2);
		this.buf().putChar(index, value);
		return this;
	}

	public final CharBuffer asCharBuffer() {
		return this.buf().asCharBuffer();
	}

	public final short getShort() {
		return this.buf().getShort();
	}

	public final IoBuffer putShort(short value) {
		this.autoExpand(2);
		this.buf().putShort(value);
		return this;
	}

	public final short getShort(int index) {
		return this.buf().getShort(index);
	}

	public final IoBuffer putShort(int index, short value) {
		this.autoExpand(index, 2);
		this.buf().putShort(index, value);
		return this;
	}

	public final ShortBuffer asShortBuffer() {
		return this.buf().asShortBuffer();
	}

	public final int getInt() {
		return this.buf().getInt();
	}

	public final IoBuffer putInt(int value) {
		this.autoExpand(4);
		this.buf().putInt(value);
		return this;
	}

	public final IoBuffer putUnsignedInt(byte value) {
		this.autoExpand(4);
		this.buf().putInt(value & 255);
		return this;
	}

	public final IoBuffer putUnsignedInt(int index, byte value) {
		this.autoExpand(index, 4);
		this.buf().putInt(index, value & 255);
		return this;
	}

	public final IoBuffer putUnsignedInt(short value) {
		this.autoExpand(4);
		this.buf().putInt(value & '?');
		return this;
	}

	public final IoBuffer putUnsignedInt(int index, short value) {
		this.autoExpand(index, 4);
		this.buf().putInt(index, value & '?');
		return this;
	}

	public final IoBuffer putUnsignedInt(int value) {
		this.autoExpand(4);
		this.buf().putInt(value);
		return this;
	}

	public final IoBuffer putUnsignedInt(int index, int value) {
		this.autoExpand(index, 4);
		this.buf().putInt(index, value);
		return this;
	}

	public final IoBuffer putUnsignedInt(long value) {
		this.autoExpand(4);
		this.buf().putInt((int) (value & -1L));
		return this;
	}

	public final IoBuffer putUnsignedInt(int index, long value) {
		this.autoExpand(index, 4);
		this.buf().putInt(index, (int) (value & 4294967295L));
		return this;
	}

	public final IoBuffer putUnsignedShort(byte value) {
		this.autoExpand(2);
		this.buf().putShort((short) (value & 255));
		return this;
	}

	public final IoBuffer putUnsignedShort(int index, byte value) {
		this.autoExpand(index, 2);
		this.buf().putShort(index, (short) (value & 255));
		return this;
	}

	public final IoBuffer putUnsignedShort(short value) {
		this.autoExpand(2);
		this.buf().putShort(value);
		return this;
	}

	public final IoBuffer putUnsignedShort(int index, short value) {
		this.autoExpand(index, 2);
		this.buf().putShort(index, value);
		return this;
	}

	public final IoBuffer putUnsignedShort(int value) {
		this.autoExpand(2);
		this.buf().putShort((short) value);
		return this;
	}

	public final IoBuffer putUnsignedShort(int index, int value) {
		this.autoExpand(index, 2);
		this.buf().putShort(index, (short) value);
		return this;
	}

	public final IoBuffer putUnsignedShort(long value) {
		this.autoExpand(2);
		this.buf().putShort((short) ((int) value));
		return this;
	}

	public final IoBuffer putUnsignedShort(int index, long value) {
		this.autoExpand(index, 2);
		this.buf().putShort(index, (short) ((int) value));
		return this;
	}

	public final int getInt(int index) {
		return this.buf().getInt(index);
	}

	public final IoBuffer putInt(int index, int value) {
		this.autoExpand(index, 4);
		this.buf().putInt(index, value);
		return this;
	}

	public final IntBuffer asIntBuffer() {
		return this.buf().asIntBuffer();
	}

	public final long getLong() {
		return this.buf().getLong();
	}

	public final IoBuffer putLong(long value) {
		this.autoExpand(8);
		this.buf().putLong(value);
		return this;
	}

	public final long getLong(int index) {
		return this.buf().getLong(index);
	}

	public final IoBuffer putLong(int index, long value) {
		this.autoExpand(index, 8);
		this.buf().putLong(index, value);
		return this;
	}

	public final LongBuffer asLongBuffer() {
		return this.buf().asLongBuffer();
	}

	public final float getFloat() {
		return this.buf().getFloat();
	}

	public final IoBuffer putFloat(float value) {
		this.autoExpand(4);
		this.buf().putFloat(value);
		return this;
	}

	public final float getFloat(int index) {
		return this.buf().getFloat(index);
	}

	public final IoBuffer putFloat(int index, float value) {
		this.autoExpand(index, 4);
		this.buf().putFloat(index, value);
		return this;
	}

	public final FloatBuffer asFloatBuffer() {
		return this.buf().asFloatBuffer();
	}

	public final double getDouble() {
		return this.buf().getDouble();
	}

	public final IoBuffer putDouble(double value) {
		this.autoExpand(8);
		this.buf().putDouble(value);
		return this;
	}

	public final double getDouble(int index) {
		return this.buf().getDouble(index);
	}

	public final IoBuffer putDouble(int index, double value) {
		this.autoExpand(index, 8);
		this.buf().putDouble(index, value);
		return this;
	}

	public final DoubleBuffer asDoubleBuffer() {
		return this.buf().asDoubleBuffer();
	}

	public final IoBuffer asReadOnlyBuffer() {
		this.recapacityAllowed = false;
		return this.asReadOnlyBuffer0();
	}

	protected abstract IoBuffer asReadOnlyBuffer0();

	public final IoBuffer duplicate() {
		this.recapacityAllowed = false;
		return this.duplicate0();
	}

	protected abstract IoBuffer duplicate0();

	public final IoBuffer slice() {
		this.recapacityAllowed = false;
		return this.slice0();
	}

	public final IoBuffer getSlice(int index, int length) {
		if (length < 0) {
			throw new IllegalArgumentException("length: " + length);
		} else {
			int pos = this.position();
			int limit = this.limit();
			if (index > limit) {
				throw new IllegalArgumentException("index: " + index);
			} else {
				int endIndex = index + length;
				if (endIndex > limit) {
					throw new IndexOutOfBoundsException(
							"index + length (" + endIndex + ") is greater " + "than limit (" + limit + ").");
				} else {
					this.clear();
					this.limit(endIndex);
					this.position(index);
					IoBuffer slice = this.slice();
					this.limit(limit);
					this.position(pos);
					return slice;
				}
			}
		}
	}

	public final IoBuffer getSlice(int length) {
		if (length < 0) {
			throw new IllegalArgumentException("length: " + length);
		} else {
			int pos = this.position();
			int limit = this.limit();
			int nextPos = pos + length;
			if (limit < nextPos) {
				throw new IndexOutOfBoundsException(
						"position + length (" + nextPos + ") is greater " + "than limit (" + limit + ").");
			} else {
				this.limit(pos + length);
				IoBuffer slice = this.slice();
				this.position(nextPos);
				this.limit(limit);
				return slice;
			}
		}
	}

	protected abstract IoBuffer slice0();

	public int hashCode() {
		int h = 1;
		int p = this.position();

		for (int i = this.limit() - 1; i >= p; --i) {
			h = 31 * h + this.get(i);
		}

		return h;
	}

	public boolean equals(Object o) {
		if (!(o instanceof IoBuffer)) {
			return false;
		} else {
			IoBuffer that = (IoBuffer) o;
			if (this.remaining() != that.remaining()) {
				return false;
			} else {
				int p = this.position();
				int i = this.limit() - 1;

				for (int j = that.limit() - 1; i >= p; --j) {
					byte v1 = this.get(i);
					byte v2 = that.get(j);
					if (v1 != v2) {
						return false;
					}

					--i;
				}

				return true;
			}
		}
	}

	public int compareTo(IoBuffer that) {
		int n = this.position() + Math.min(this.remaining(), that.remaining());
		int i = this.position();

		for (int j = that.position(); i < n; ++j) {
			byte v1 = this.get(i);
			byte v2 = that.get(j);
			if (v1 != v2) {
				if (v1 < v2) {
					return -1;
				}

				return 1;
			}

			++i;
		}

		return this.remaining() - that.remaining();
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (this.isDirect()) {
			buf.append("DirectBuffer");
		} else {
			buf.append("HeapBuffer");
		}

		buf.append("[pos=");
		buf.append(this.position());
		buf.append(" lim=");
		buf.append(this.limit());
		buf.append(" cap=");
		buf.append(this.capacity());
		buf.append(": ");
		buf.append(this.getHexDump(16));
		buf.append(']');
		return buf.toString();
	}

	public IoBuffer get(byte[] dst) {
		return this.get(dst, 0, dst.length);
	}

	public IoBuffer put(IoBuffer src) {
		return this.put(src.buf());
	}

	public IoBuffer put(byte[] src) {
		return this.put(src, 0, src.length);
	}

	public int getUnsignedShort() {
		return this.getShort() & '?';
	}

	public int getUnsignedShort(int index) {
		return this.getShort(index) & '?';
	}

	public long getUnsignedInt() {
		return (long) this.getInt() & 4294967295L;
	}

	public int getMediumInt() {
		byte b1 = this.get();
		byte b2 = this.get();
		byte b3 = this.get();
		return ByteOrder.BIG_ENDIAN.equals(this.order()) ? this.getMediumInt(b1, b2, b3)
				: this.getMediumInt(b3, b2, b1);
	}

	public int getUnsignedMediumInt() {
		short b1 = this.getUnsigned();
		short b2 = this.getUnsigned();
		short b3 = this.getUnsigned();
		return ByteOrder.BIG_ENDIAN.equals(this.order()) ? b1 << 16 | b2 << 8 | b3 : b3 << 16 | b2 << 8 | b1;
	}

	public int getMediumInt(int index) {
		byte b1 = this.get(index);
		byte b2 = this.get(index + 1);
		byte b3 = this.get(index + 2);
		return ByteOrder.BIG_ENDIAN.equals(this.order()) ? this.getMediumInt(b1, b2, b3)
				: this.getMediumInt(b3, b2, b1);
	}

	public int getUnsignedMediumInt(int index) {
		short b1 = this.getUnsigned(index);
		short b2 = this.getUnsigned(index + 1);
		short b3 = this.getUnsigned(index + 2);
		return ByteOrder.BIG_ENDIAN.equals(this.order()) ? b1 << 16 | b2 << 8 | b3 : b3 << 16 | b2 << 8 | b1;
	}

	private int getMediumInt(byte b1, byte b2, byte b3) {
		int ret = b1 << 16 & 16711680 | b2 << 8 & '?' | b3 & 255;
		if ((b1 & 128) == 128) {
			ret |= -16777216;
		}

		return ret;
	}

	public IoBuffer putMediumInt(int value) {
		byte b1 = (byte) (value >> 16);
		byte b2 = (byte) (value >> 8);
		byte b3 = (byte) value;
		if (ByteOrder.BIG_ENDIAN.equals(this.order())) {
			this.put(b1).put(b2).put(b3);
		} else {
			this.put(b3).put(b2).put(b1);
		}

		return this;
	}

	public IoBuffer putMediumInt(int index, int value) {
		byte b1 = (byte) (value >> 16);
		byte b2 = (byte) (value >> 8);
		byte b3 = (byte) value;
		if (ByteOrder.BIG_ENDIAN.equals(this.order())) {
			this.put(index, b1).put(index + 1, b2).put(index + 2, b3);
		} else {
			this.put(index, b3).put(index + 1, b2).put(index + 2, b1);
		}

		return this;
	}

	public long getUnsignedInt(int index) {
		return (long) this.getInt(index) & 4294967295L;
	}

	public InputStream asInputStream() {
		return new InputStream() {
			public int available() {
				return AbstractIoBuffer.this.remaining();
			}

			public synchronized void mark(int readlimit) {
				AbstractIoBuffer.this.mark();
			}

			public boolean markSupported() {
				return true;
			}

			public int read() {
				return AbstractIoBuffer.this.hasRemaining() ? AbstractIoBuffer.this.get() & 255 : -1;
			}

			public int read(byte[] b, int off, int len) {
				int remaining = AbstractIoBuffer.this.remaining();
				if (remaining > 0) {
					int readBytes = Math.min(remaining, len);
					AbstractIoBuffer.this.get(b, off, readBytes);
					return readBytes;
				} else {
					return -1;
				}
			}

			public synchronized void reset() {
				AbstractIoBuffer.this.reset();
			}

			public long skip(long n) {
				int bytes;
				if (n > 2147483647L) {
					bytes = AbstractIoBuffer.this.remaining();
				} else {
					bytes = Math.min(AbstractIoBuffer.this.remaining(), (int) n);
				}

				AbstractIoBuffer.this.skip(bytes);
				return (long) bytes;
			}
		};
	}

	public OutputStream asOutputStream() {
		return new OutputStream() {
			public void write(byte[] b, int off, int len) {
				AbstractIoBuffer.this.put(b, off, len);
			}

			public void write(int b) {
				AbstractIoBuffer.this.put((byte) b);
			}
		};
	}

	public String getHexDump() {
		return this.getHexDump(Integer.MAX_VALUE);
	}

	public String getHexDump(int lengthLimit) {
		return IoBufferHexDumper.getHexdump(this, lengthLimit);
	}

	public String getString(CharsetDecoder decoder) throws CharacterCodingException {
		if (!this.hasRemaining()) {
			return "";
		} else {
			boolean utf16 = decoder.charset().name().startsWith("UTF-16");
			int oldPos = this.position();
			int oldLimit = this.limit();
			int end = -1;
			int newPos;
			int expectedLength;
			if (!utf16) {
				end = this.indexOf(0);
				if (end < 0) {
					end = oldLimit;
					newPos = oldLimit;
				} else {
					newPos = end + 1;
				}
			} else {
				expectedLength = oldPos;

				while (true) {
					boolean out = this.get(expectedLength) == 0;
					++expectedLength;
					if (expectedLength >= oldLimit) {
						break;
					}

					if (this.get(expectedLength) != 0) {
						++expectedLength;
						if (expectedLength >= oldLimit) {
							break;
						}
					} else if (out) {
						end = expectedLength - 1;
						break;
					}
				}

				if (end < 0) {
					newPos = end = oldPos + (oldLimit - oldPos & -2);
				} else if (end + 2 <= oldLimit) {
					newPos = end + 2;
				} else {
					newPos = end;
				}
			}

			if (oldPos == end) {
				this.position(newPos);
				return "";
			} else {
				this.limit(end);
				decoder.reset();
				expectedLength = (int) ((float) this.remaining() * decoder.averageCharsPerByte()) + 1;
				CharBuffer arg10 = CharBuffer.allocate(expectedLength);

				while (true) {
					CoderResult cr;
					if (this.hasRemaining()) {
						cr = decoder.decode(this.buf(), arg10, true);
					} else {
						cr = decoder.flush(arg10);
					}

					if (cr.isUnderflow()) {
						this.limit(oldLimit);
						this.position(newPos);
						return arg10.flip().toString();
					}

					if (cr.isOverflow()) {
						CharBuffer o = CharBuffer.allocate(arg10.capacity() + expectedLength);
						arg10.flip();
						o.put(arg10);
						arg10 = o;
					} else if (cr.isError()) {
						this.limit(oldLimit);
						this.position(oldPos);
						cr.throwException();
					}
				}
			}
		}
	}

	public String getString(int fieldSize, CharsetDecoder decoder) throws CharacterCodingException {
		checkFieldSize(fieldSize);
		if (fieldSize == 0) {
			return "";
		} else if (!this.hasRemaining()) {
			return "";
		} else {
			boolean utf16 = decoder.charset().name().startsWith("UTF-16");
			if (utf16 && (fieldSize & 1) != 0) {
				throw new IllegalArgumentException("fieldSize is not even.");
			} else {
				int oldPos = this.position();
				int oldLimit = this.limit();
				int end = oldPos + fieldSize;
				if (oldLimit < end) {
					throw new BufferUnderflowException();
				} else {
					int i;
					if (!utf16) {
						for (i = oldPos; i < end && this.get(i) != 0; ++i) {
							;
						}

						if (i == end) {
							this.limit(end);
						} else {
							this.limit(i);
						}
					} else {
						for (i = oldPos; i < end && (this.get(i) != 0 || this.get(i + 1) != 0); i += 2) {
							;
						}

						if (i == end) {
							this.limit(end);
						} else {
							this.limit(i);
						}
					}

					if (!this.hasRemaining()) {
						this.limit(oldLimit);
						this.position(end);
						return "";
					} else {
						decoder.reset();
						int expectedLength = (int) ((float) this.remaining() * decoder.averageCharsPerByte()) + 1;
						CharBuffer out = CharBuffer.allocate(expectedLength);

						while (true) {
							CoderResult cr;
							if (this.hasRemaining()) {
								cr = decoder.decode(this.buf(), out, true);
							} else {
								cr = decoder.flush(out);
							}

							if (cr.isUnderflow()) {
								this.limit(oldLimit);
								this.position(end);
								return out.flip().toString();
							}

							if (cr.isOverflow()) {
								CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
								out.flip();
								o.put(out);
								out = o;
							} else if (cr.isError()) {
								this.limit(oldLimit);
								this.position(oldPos);
								cr.throwException();
							}
						}
					}
				}
			}
		}
	}

	public IoBuffer putString(CharSequence val, CharsetEncoder encoder) throws CharacterCodingException {
		if (val.length() == 0) {
			return this;
		} else {
			CharBuffer in = CharBuffer.wrap(val);
			encoder.reset();
			int expandedState = 0;

			while (true) {
				while (true) {
					CoderResult cr;
					if (in.hasRemaining()) {
						cr = encoder.encode(in, this.buf(), true);
					} else {
						cr = encoder.flush(this.buf());
					}

					if (cr.isUnderflow()) {
						return this;
					}

					if (cr.isOverflow()) {
						if (this.isAutoExpand()) {
							switch (expandedState) {
							case 0:
								this.autoExpand((int) Math
										.ceil((double) ((float) in.remaining() * encoder.averageBytesPerChar())));
								++expandedState;
								continue;
							case 1:
								this.autoExpand(
										(int) Math.ceil((double) ((float) in.remaining() * encoder.maxBytesPerChar())));
								++expandedState;
								continue;
							default:
								throw new RuntimeException(
										"Expanded by "
												+ (int) Math.ceil(
														(double) ((float) in.remaining() * encoder.maxBytesPerChar()))
												+ " but that wasn\'t enough for \'" + val + "\'");
							}
						}
					} else {
						expandedState = 0;
					}

					cr.throwException();
				}
			}
		}
	}

	public IoBuffer putString(CharSequence val, int fieldSize, CharsetEncoder encoder) throws CharacterCodingException {
		checkFieldSize(fieldSize);
		if (fieldSize == 0) {
			return this;
		} else {
			this.autoExpand(fieldSize);
			boolean utf16 = encoder.charset().name().startsWith("UTF-16");
			if (utf16 && (fieldSize & 1) != 0) {
				throw new IllegalArgumentException("fieldSize is not even.");
			} else {
				int oldLimit = this.limit();
				int end = this.position() + fieldSize;
				if (oldLimit < end) {
					throw new BufferOverflowException();
				} else if (val.length() == 0) {
					if (!utf16) {
						this.put(0);
					} else {
						this.put(0);
						this.put(0);
					}

					this.position(end);
					return this;
				} else {
					CharBuffer in = CharBuffer.wrap(val);
					this.limit(end);
					encoder.reset();

					while (true) {
						CoderResult cr;
						if (in.hasRemaining()) {
							cr = encoder.encode(in, this.buf(), true);
						} else {
							cr = encoder.flush(this.buf());
						}

						if (cr.isUnderflow() || cr.isOverflow()) {
							this.limit(oldLimit);
							if (this.position() < end) {
								if (!utf16) {
									this.put(0);
								} else {
									this.put(0);
									this.put(0);
								}
							}

							this.position(end);
							return this;
						}

						cr.throwException();
					}
				}
			}
		}
	}

	public String getPrefixedString(CharsetDecoder decoder) throws CharacterCodingException {
		return this.getPrefixedString(2, decoder);
	}

	public String getPrefixedString(int prefixLength, CharsetDecoder decoder) throws CharacterCodingException {
		if (!this.prefixedDataAvailable(prefixLength)) {
			throw new BufferUnderflowException();
		} else {
			int fieldSize = 0;
			switch (prefixLength) {
			case 1:
				fieldSize = this.getUnsigned();
				break;
			case 2:
				fieldSize = this.getUnsignedShort();
			case 3:
			default:
				break;
			case 4:
				fieldSize = this.getInt();
			}

			if (fieldSize == 0) {
				return "";
			} else {
				boolean utf16 = decoder.charset().name().startsWith("UTF-16");
				if (utf16 && (fieldSize & 1) != 0) {
					throw new BufferDataException("fieldSize is not even for a UTF-16 string.");
				} else {
					int oldLimit = this.limit();
					int end = this.position() + fieldSize;
					if (oldLimit < end) {
						throw new BufferUnderflowException();
					} else {
						this.limit(end);
						decoder.reset();
						int expectedLength = (int) ((float) this.remaining() * decoder.averageCharsPerByte()) + 1;
						CharBuffer out = CharBuffer.allocate(expectedLength);

						while (true) {
							CoderResult cr;
							if (this.hasRemaining()) {
								cr = decoder.decode(this.buf(), out, true);
							} else {
								cr = decoder.flush(out);
							}

							if (cr.isUnderflow()) {
								this.limit(oldLimit);
								this.position(end);
								return out.flip().toString();
							}

							if (cr.isOverflow()) {
								CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
								out.flip();
								o.put(out);
								out = o;
							} else {
								cr.throwException();
							}
						}
					}
				}
			}
		}
	}

	public IoBuffer putPrefixedString(CharSequence in, CharsetEncoder encoder) throws CharacterCodingException {
		return this.putPrefixedString(in, 2, 0, encoder);
	}

	public IoBuffer putPrefixedString(CharSequence in, int prefixLength, CharsetEncoder encoder)
			throws CharacterCodingException {
		return this.putPrefixedString(in, prefixLength, 0, encoder);
	}

	public IoBuffer putPrefixedString(CharSequence in, int prefixLength, int padding, CharsetEncoder encoder)
			throws CharacterCodingException {
		return this.putPrefixedString(in, prefixLength, padding, 0, encoder);
	}

	public IoBuffer putPrefixedString(CharSequence val, int prefixLength, int padding, byte padValue,
			CharsetEncoder encoder) throws CharacterCodingException {
		int maxLength;
		switch (prefixLength) {
		case 1:
			maxLength = 255;
			break;
		case 2:
			maxLength = '?';
			break;
		case 3:
		default:
			throw new IllegalArgumentException("prefixLength: " + prefixLength);
		case 4:
			maxLength = Integer.MAX_VALUE;
		}

		if (val.length() > maxLength) {
			throw new IllegalArgumentException("The specified string is too long.");
		} else if (val.length() == 0) {
			switch (prefixLength) {
			case 1:
				this.put(0);
				break;
			case 2:
				this.putShort(0);
			case 3:
			default:
				break;
			case 4:
				this.putInt(0);
			}

			return this;
		} else {
			byte padMask;
			switch (padding) {
			case 0:
			case 1:
				padMask = 0;
				break;
			case 2:
				padMask = 1;
				break;
			case 3:
			default:
				throw new IllegalArgumentException("padding: " + padding);
			case 4:
				padMask = 3;
			}

			CharBuffer in = CharBuffer.wrap(val);
			this.skip(prefixLength);
			int oldPos = this.position();
			encoder.reset();
			int expandedState = 0;

			while (true) {
				CoderResult length;
				if (in.hasRemaining()) {
					length = encoder.encode(in, this.buf(), true);
				} else {
					length = encoder.flush(this.buf());
				}

				if (this.position() - oldPos > maxLength) {
					throw new IllegalArgumentException("The specified string is too long.");
				}

				if (length.isUnderflow()) {
					this.fill(padValue, padding - (this.position() - oldPos & padMask));
					int arg11 = this.position() - oldPos;
					switch (prefixLength) {
					case 1:
						this.put(oldPos - 1, (byte) arg11);
						break;
					case 2:
						this.putShort(oldPos - 2, (short) arg11);
					case 3:
					default:
						break;
					case 4:
						this.putInt(oldPos - 4, arg11);
					}

					return this;
				}

				if (length.isOverflow()) {
					if (this.isAutoExpand()) {
						switch (expandedState) {
						case 0:
							this.autoExpand(
									(int) Math.ceil((double) ((float) in.remaining() * encoder.averageBytesPerChar())));
							++expandedState;
							continue;
						case 1:
							this.autoExpand(
									(int) Math.ceil((double) ((float) in.remaining() * encoder.maxBytesPerChar())));
							++expandedState;
							continue;
						default:
							throw new RuntimeException("Expanded by "
									+ (int) Math.ceil((double) ((float) in.remaining() * encoder.maxBytesPerChar()))
									+ " but that wasn\'t enough for \'" + val + "\'");
						}
					}
				} else {
					expandedState = 0;
				}

				length.throwException();
			}
		}
	}

	public Object getObject() throws ClassNotFoundException {
		return this.getObject(Thread.currentThread().getContextClassLoader());
	}

	public Object getObject(final ClassLoader classLoader) throws ClassNotFoundException {
		if (!this.prefixedDataAvailable(4)) {
			throw new BufferUnderflowException();
		} else {
			int length = this.getInt();
			if (length <= 4) {
				throw new BufferDataException("Object length should be greater than 4: " + length);
			} else {
				int oldLimit = this.limit();
				this.limit(this.position() + length);
				ObjectInputStream in = null;

				Object e;
				try {
					in = new ObjectInputStream(this.asInputStream()) {
						protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
							int type = this.read();
							if (type < 0) {
								throw new EOFException();
							} else {
								switch (type) {
								case 0:
									return super.readClassDescriptor();
								case 1:
									String className = this.readUTF();
									Class clazz = Class.forName(className, true, classLoader);
									return ObjectStreamClass.lookup(clazz);
								default:
									throw new StreamCorruptedException("Unexpected class descriptor type: " + type);
								}
							}
						}

						protected Class<?> resolveClass(ObjectStreamClass desc)
								throws IOException, ClassNotFoundException {
							Class clazz = desc.forClass();
							if (clazz == null) {
								String name = desc.getName();

								try {
									return Class.forName(name, false, classLoader);
								} catch (ClassNotFoundException arg4) {
									return super.resolveClass(desc);
								}
							} else {
								return clazz;
							}
						}
					};
					e = in.readObject();
				} catch (IOException arg13) {
					throw new BufferDataException(arg13);
				} finally {
					try {
						if (in != null) {
							in.close();
						}
					} catch (IOException arg12) {
						;
					}

					this.limit(oldLimit);
				}

				return e;
			}
		}
	}

	public IoBuffer putObject(Object o) {
		int oldPos = this.position();
		this.skip(4);
		ObjectOutputStream out = null;

		try {
			out = new ObjectOutputStream(this.asOutputStream()) {
				protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
					Class clazz = desc.forClass();
					if (!clazz.isArray() && !clazz.isPrimitive() && Serializable.class.isAssignableFrom(clazz)) {
						this.write(1);
						this.writeUTF(desc.getName());
					} else {
						this.write(0);
						super.writeClassDescriptor(desc);
					}

				}
			};
			out.writeObject(o);
			out.flush();
		} catch (IOException arg11) {
			throw new BufferDataException(arg11);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException arg10) {
				;
			}

		}

		int newPos = this.position();
		this.position(oldPos);
		this.putInt(newPos - oldPos - 4);
		this.position(newPos);
		return this;
	}

	public boolean prefixedDataAvailable(int prefixLength) {
		return this.prefixedDataAvailable(prefixLength, Integer.MAX_VALUE);
	}

	public boolean prefixedDataAvailable(int prefixLength, int maxDataLength) {
		if (this.remaining() < prefixLength) {
			return false;
		} else {
			int dataLength;
			switch (prefixLength) {
			case 1:
				dataLength = this.getUnsigned(this.position());
				break;
			case 2:
				dataLength = this.getUnsignedShort(this.position());
				break;
			case 3:
			default:
				throw new IllegalArgumentException("prefixLength: " + prefixLength);
			case 4:
				dataLength = this.getInt(this.position());
			}

			if (dataLength >= 0 && dataLength <= maxDataLength) {
				return this.remaining() - prefixLength >= dataLength;
			} else {
				throw new BufferDataException("dataLength: " + dataLength);
			}
		}
	}

	public int indexOf(byte b) {
		int beginPos;
		int limit;
		int i;
		if (this.hasArray()) {
			beginPos = this.arrayOffset();
			limit = beginPos + this.position();
			i = beginPos + this.limit();
			byte[] array = this.array();

			for (int i1 = limit; i1 < i; ++i1) {
				if (array[i1] == b) {
					return i1 - beginPos;
				}
			}
		} else {
			beginPos = this.position();
			limit = this.limit();

			for (i = beginPos; i < limit; ++i) {
				if (this.get(i) == b) {
					return i;
				}
			}
		}

		return -1;
	}

	public IoBuffer skip(int size) {
		this.autoExpand(size);
		return this.position(this.position() + size);
	}

	public IoBuffer fill(byte value, int size) {
		this.autoExpand(size);
		int q = size >>> 3;
		int r = size & 7;
		int shortValue;
		if (q > 0) {
			shortValue = value & 255 | value << 8 & '?' | value << 16 & 16711680 | value << 24;
			long longValue = (long) shortValue & 4294967295L | (long) shortValue << 32;

			for (int i = q; i > 0; --i) {
				this.putLong(longValue);
			}
		}

		q = r >>> 2;
		r &= 3;
		if (q > 0) {
			shortValue = value & 255 | value << 8 & '?' | value << 16 & 16711680 | value << 24;
			this.putInt(shortValue);
		}

		q = r >> 1;
		r &= 1;
		if (q > 0) {
			short arg8 = (short) (value & 255 | value << 8);
			this.putShort(arg8);
		}

		if (r > 0) {
			this.put(value);
		}

		return this;
	}

	public IoBuffer fillAndReset(byte value, int size) {
		this.autoExpand(size);
		int pos = this.position();

		try {
			this.fill(value, size);
		} finally {
			this.position(pos);
		}

		return this;
	}

	public IoBuffer fill(int size) {
		this.autoExpand(size);
		int q = size >>> 3;
		int r = size & 7;

		for (int i = q; i > 0; --i) {
			this.putLong(0L);
		}

		q = r >>> 2;
		r &= 3;
		if (q > 0) {
			this.putInt(0);
		}

		q = r >> 1;
		r &= 1;
		if (q > 0) {
			this.putShort(0);
		}

		if (r > 0) {
			this.put(0);
		}

		return this;
	}

	public IoBuffer fillAndReset(int size) {
		this.autoExpand(size);
		int pos = this.position();

		try {
			this.fill(size);
		} finally {
			this.position(pos);
		}

		return this;
	}

	public <E extends Enum<E>> E getEnum(Class<E> enumClass) {
		return (Enum) this.toEnum(enumClass, this.getUnsigned());
	}

	public <E extends Enum<E>> E getEnum(int index, Class<E> enumClass) {
		return (Enum) this.toEnum(enumClass, this.getUnsigned(index));
	}

	public <E extends Enum<E>> E getEnumShort(Class<E> enumClass) {
		return (Enum) this.toEnum(enumClass, this.getUnsignedShort());
	}

	public <E extends Enum<E>> E getEnumShort(int index, Class<E> enumClass) {
		return (Enum) this.toEnum(enumClass, this.getUnsignedShort(index));
	}

	public <E extends Enum<E>> E getEnumInt(Class<E> enumClass) {
		return (Enum) this.toEnum(enumClass, this.getInt());
	}

	public <E extends Enum<E>> E getEnumInt(int index, Class<E> enumClass) {
		return (Enum) this.toEnum(enumClass, this.getInt(index));
	}

	public IoBuffer putEnum(Enum<?> e) {
		if ((long) e.ordinal() > 255L) {
			throw new IllegalArgumentException(this.enumConversionErrorMessage(e, "byte"));
		} else {
			return this.put((byte) e.ordinal());
		}
	}

	public IoBuffer putEnum(int index, Enum<?> e) {
		if ((long) e.ordinal() > 255L) {
			throw new IllegalArgumentException(this.enumConversionErrorMessage(e, "byte"));
		} else {
			return this.put(index, (byte) e.ordinal());
		}
	}

	public IoBuffer putEnumShort(Enum<?> e) {
		if ((long) e.ordinal() > 65535L) {
			throw new IllegalArgumentException(this.enumConversionErrorMessage(e, "short"));
		} else {
			return this.putShort((short) e.ordinal());
		}
	}

	public IoBuffer putEnumShort(int index, Enum<?> e) {
		if ((long) e.ordinal() > 65535L) {
			throw new IllegalArgumentException(this.enumConversionErrorMessage(e, "short"));
		} else {
			return this.putShort(index, (short) e.ordinal());
		}
	}

	public IoBuffer putEnumInt(Enum<?> e) {
		return this.putInt(e.ordinal());
	}

	public IoBuffer putEnumInt(int index, Enum<?> e) {
		return this.putInt(index, e.ordinal());
	}

	private <E> E toEnum(Class<E> enumClass, int i) {
		Object[] enumConstants = enumClass.getEnumConstants();
		if (i > enumConstants.length) {
			throw new IndexOutOfBoundsException(String.format("%d is too large of an ordinal to convert to the enum %s",
					new Object[] { Integer.valueOf(i), enumClass.getName() }));
		} else {
			return enumConstants[i];
		}
	}

	private String enumConversionErrorMessage(Enum<?> e, String type) {
		return String.format("%s.%s has an ordinal value too large for a %s",
				new Object[] { e.getClass().getName(), e.name(), type });
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSet(Class<E> enumClass) {
		return this.toEnumSet(enumClass, (long) this.get() & 255L);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSet(int index, Class<E> enumClass) {
		return this.toEnumSet(enumClass, (long) this.get(index) & 255L);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetShort(Class<E> enumClass) {
		return this.toEnumSet(enumClass, (long) this.getShort() & 65535L);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetShort(int index, Class<E> enumClass) {
		return this.toEnumSet(enumClass, (long) this.getShort(index) & 65535L);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetInt(Class<E> enumClass) {
		return this.toEnumSet(enumClass, (long) this.getInt() & 4294967295L);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetInt(int index, Class<E> enumClass) {
		return this.toEnumSet(enumClass, (long) this.getInt(index) & 4294967295L);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetLong(Class<E> enumClass) {
		return this.toEnumSet(enumClass, this.getLong());
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetLong(int index, Class<E> enumClass) {
		return this.toEnumSet(enumClass, this.getLong(index));
	}

	private <E extends Enum<E>> EnumSet<E> toEnumSet(Class<E> clazz, long vector) {
		EnumSet set = EnumSet.noneOf(clazz);
		long mask = 1L;
		Enum[] arr$ = (Enum[]) clazz.getEnumConstants();
		int len$ = arr$.length;

		for (int i$ = 0; i$ < len$; ++i$) {
			Enum e = arr$[i$];
			if ((mask & vector) == mask) {
				set.add(e);
			}

			mask <<= 1;
		}

		return set;
	}

	public <E extends Enum<E>> IoBuffer putEnumSet(Set<E> set) {
		long vector = this.toLong(set);
		if ((vector & -256L) != 0L) {
			throw new IllegalArgumentException("The enum set is too large to fit in a byte: " + set);
		} else {
			return this.put((byte) ((int) vector));
		}
	}

	public <E extends Enum<E>> IoBuffer putEnumSet(int index, Set<E> set) {
		long vector = this.toLong(set);
		if ((vector & -256L) != 0L) {
			throw new IllegalArgumentException("The enum set is too large to fit in a byte: " + set);
		} else {
			return this.put(index, (byte) ((int) vector));
		}
	}

	public <E extends Enum<E>> IoBuffer putEnumSetShort(Set<E> set) {
		long vector = this.toLong(set);
		if ((vector & -65536L) != 0L) {
			throw new IllegalArgumentException("The enum set is too large to fit in a short: " + set);
		} else {
			return this.putShort((short) ((int) vector));
		}
	}

	public <E extends Enum<E>> IoBuffer putEnumSetShort(int index, Set<E> set) {
		long vector = this.toLong(set);
		if ((vector & -65536L) != 0L) {
			throw new IllegalArgumentException("The enum set is too large to fit in a short: " + set);
		} else {
			return this.putShort(index, (short) ((int) vector));
		}
	}

	public <E extends Enum<E>> IoBuffer putEnumSetInt(Set<E> set) {
		long vector = this.toLong(set);
		if ((vector & -4294967296L) != 0L) {
			throw new IllegalArgumentException("The enum set is too large to fit in an int: " + set);
		} else {
			return this.putInt((int) vector);
		}
	}

	public <E extends Enum<E>> IoBuffer putEnumSetInt(int index, Set<E> set) {
		long vector = this.toLong(set);
		if ((vector & -4294967296L) != 0L) {
			throw new IllegalArgumentException("The enum set is too large to fit in an int: " + set);
		} else {
			return this.putInt(index, (int) vector);
		}
	}

	public <E extends Enum<E>> IoBuffer putEnumSetLong(Set<E> set) {
		return this.putLong(this.toLong(set));
	}

	public <E extends Enum<E>> IoBuffer putEnumSetLong(int index, Set<E> set) {
		return this.putLong(index, this.toLong(set));
	}

	private <E extends Enum<E>> long toLong(Set<E> set) {
		long vector = 0L;

		Enum e;
		for (Iterator i$ = set.iterator(); i$.hasNext(); vector |= 1L << e.ordinal()) {
			e = (Enum) i$.next();
			if (e.ordinal() >= 64) {
				throw new IllegalArgumentException("The enum set is too large to fit in a bit vector: " + set);
			}
		}

		return vector;
	}

	private IoBuffer autoExpand(int expectedRemaining) {
		if (this.isAutoExpand()) {
			this.expand(expectedRemaining, true);
		}

		return this;
	}

	private IoBuffer autoExpand(int pos, int expectedRemaining) {
		if (this.isAutoExpand()) {
			this.expand(pos, expectedRemaining, true);
		}

		return this;
	}

	private static void checkFieldSize(int fieldSize) {
		if (fieldSize < 0) {
			throw new IllegalArgumentException("fieldSize cannot be negative: " + fieldSize);
		}
	}
}