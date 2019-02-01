/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import java.util.Collections;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.byteaccess.AbstractByteArray;
import org.apache.mina.util.byteaccess.ByteArray;
import org.apache.mina.util.byteaccess.ByteArray.Cursor;

public abstract class BufferByteArray extends AbstractByteArray {
	protected IoBuffer bb;

	public BufferByteArray(IoBuffer bb) {
		this.bb = bb;
	}

	public Iterable<IoBuffer> getIoBuffers() {
		return Collections.singletonList(this.bb);
	}

	public IoBuffer getSingleIoBuffer() {
		return this.bb;
	}

	public ByteArray slice(int index, int length) {
		int oldLimit = this.bb.limit();
		this.bb.position(index);
		this.bb.limit(index + length);
		final IoBuffer slice = this.bb.slice();
		this.bb.limit(oldLimit);
		return new BufferByteArray(slice) {
			public void free() {
			}
		};
	}

	public abstract void free();

	public Cursor cursor() {
		return new BufferByteArray.CursorImpl();
	}

	public Cursor cursor(int index) {
		return new BufferByteArray.CursorImpl(index);
	}

	public int first() {
		return 0;
	}

	public int last() {
		return this.bb.limit();
	}

	public ByteOrder order() {
		return this.bb.order();
	}

	public void order(ByteOrder order) {
		this.bb.order(order);
	}

	public byte get(int index) {
		return this.bb.get(index);
	}

	public void put(int index, byte b) {
		this.bb.put(index, b);
	}

	public void get(int index, IoBuffer other) {
		this.bb.position(index);
		other.put(this.bb);
	}

	public void put(int index, IoBuffer other) {
		this.bb.position(index);
		this.bb.put(other);
	}

	public short getShort(int index) {
		return this.bb.getShort(index);
	}

	public void putShort(int index, short s) {
		this.bb.putShort(index, s);
	}

	public int getInt(int index) {
		return this.bb.getInt(index);
	}

	public void putInt(int index, int i) {
		this.bb.putInt(index, i);
	}

	public long getLong(int index) {
		return this.bb.getLong(index);
	}

	public void putLong(int index, long l) {
		this.bb.putLong(index, l);
	}

	public float getFloat(int index) {
		return this.bb.getFloat(index);
	}

	public void putFloat(int index, float f) {
		this.bb.putFloat(index, f);
	}

	public double getDouble(int index) {
		return this.bb.getDouble(index);
	}

	public void putDouble(int index, double d) {
		this.bb.putDouble(index, d);
	}

	public char getChar(int index) {
		return this.bb.getChar(index);
	}

	public void putChar(int index, char c) {
		this.bb.putChar(index, c);
	}

	private class CursorImpl implements Cursor {
		private int index;

		public CursorImpl() {
		}

		public CursorImpl(int index) {
			this.setIndex(index);
		}

		public int getRemaining() {
			return BufferByteArray.this.last() - this.index;
		}

		public boolean hasRemaining() {
			return this.getRemaining() > 0;
		}

		public int getIndex() {
			return this.index;
		}

		public void setIndex(int index) {
			if (index >= 0 && index <= BufferByteArray.this.last()) {
				this.index = index;
			} else {
				throw new IndexOutOfBoundsException();
			}
		}

		public void skip(int length) {
			this.setIndex(this.index + length);
		}

		public ByteArray slice(int length) {
			ByteArray slice = BufferByteArray.this.slice(this.index, length);
			this.index += length;
			return slice;
		}

		public ByteOrder order() {
			return BufferByteArray.this.order();
		}

		public byte get() {
			byte b = BufferByteArray.this.get(this.index);
			++this.index;
			return b;
		}

		public void put(byte b) {
			BufferByteArray.this.put(this.index, b);
			++this.index;
		}

		public void get(IoBuffer bb) {
			int size = Math.min(this.getRemaining(), bb.remaining());
			BufferByteArray.this.get(this.index, bb);
			this.index += size;
		}

		public void put(IoBuffer bb) {
			int size = bb.remaining();
			BufferByteArray.this.put(this.index, bb);
			this.index += size;
		}

		public short getShort() {
			short s = BufferByteArray.this.getShort(this.index);
			this.index += 2;
			return s;
		}

		public void putShort(short s) {
			BufferByteArray.this.putShort(this.index, s);
			this.index += 2;
		}

		public int getInt() {
			int i = BufferByteArray.this.getInt(this.index);
			this.index += 4;
			return i;
		}

		public void putInt(int i) {
			BufferByteArray.this.putInt(this.index, i);
			this.index += 4;
		}

		public long getLong() {
			long l = BufferByteArray.this.getLong(this.index);
			this.index += 8;
			return l;
		}

		public void putLong(long l) {
			BufferByteArray.this.putLong(this.index, l);
			this.index += 8;
		}

		public float getFloat() {
			float f = BufferByteArray.this.getFloat(this.index);
			this.index += 4;
			return f;
		}

		public void putFloat(float f) {
			BufferByteArray.this.putFloat(this.index, f);
			this.index += 4;
		}

		public double getDouble() {
			double d = BufferByteArray.this.getDouble(this.index);
			this.index += 8;
			return d;
		}

		public void putDouble(double d) {
			BufferByteArray.this.putDouble(this.index, d);
			this.index += 8;
		}

		public char getChar() {
			char c = BufferByteArray.this.getChar(this.index);
			this.index += 2;
			return c;
		}

		public void putChar(char c) {
			BufferByteArray.this.putChar(this.index, c);
			this.index += 2;
		}
	}
}