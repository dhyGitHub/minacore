/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.serialization;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.StreamCorruptedException;
import org.apache.mina.core.buffer.IoBuffer;

public class ObjectSerializationInputStream extends InputStream implements ObjectInput {
	private final DataInputStream in;
	private final ClassLoader classLoader;
	private int maxObjectSize;

	public ObjectSerializationInputStream(InputStream in) {
		this(in, (ClassLoader) null);
	}

	public ObjectSerializationInputStream(InputStream in, ClassLoader classLoader) {
		this.maxObjectSize = 1048576;
		if (in == null) {
			throw new IllegalArgumentException("in");
		} else {
			if (classLoader == null) {
				classLoader = Thread.currentThread().getContextClassLoader();
			}

			if (in instanceof DataInputStream) {
				this.in = (DataInputStream) in;
			} else {
				this.in = new DataInputStream(in);
			}

			this.classLoader = classLoader;
		}
	}

	public int getMaxObjectSize() {
		return this.maxObjectSize;
	}

	public void setMaxObjectSize(int maxObjectSize) {
		if (maxObjectSize <= 0) {
			throw new IllegalArgumentException("maxObjectSize: " + maxObjectSize);
		} else {
			this.maxObjectSize = maxObjectSize;
		}
	}

	public int read() throws IOException {
		return this.in.read();
	}

	public Object readObject() throws ClassNotFoundException, IOException {
		int objectSize = this.in.readInt();
		if (objectSize <= 0) {
			throw new StreamCorruptedException("Invalid objectSize: " + objectSize);
		} else if (objectSize > this.maxObjectSize) {
			throw new StreamCorruptedException(
					"ObjectSize too big: " + objectSize + " (expected: <= " + this.maxObjectSize + ')');
		} else {
			IoBuffer buf = IoBuffer.allocate(objectSize + 4, false);
			buf.putInt(objectSize);
			this.in.readFully(buf.array(), 4, objectSize);
			buf.position(0);
			buf.limit(objectSize + 4);
			return buf.getObject(this.classLoader);
		}
	}

	public boolean readBoolean() throws IOException {
		return this.in.readBoolean();
	}

	public byte readByte() throws IOException {
		return this.in.readByte();
	}

	public char readChar() throws IOException {
		return this.in.readChar();
	}

	public double readDouble() throws IOException {
		return this.in.readDouble();
	}

	public float readFloat() throws IOException {
		return this.in.readFloat();
	}

	public void readFully(byte[] b) throws IOException {
		this.in.readFully(b);
	}

	public void readFully(byte[] b, int off, int len) throws IOException {
		this.in.readFully(b, off, len);
	}

	public int readInt() throws IOException {
		return this.in.readInt();
	}

	@Deprecated
	public String readLine() throws IOException {
		return this.in.readLine();
	}

	public long readLong() throws IOException {
		return this.in.readLong();
	}

	public short readShort() throws IOException {
		return this.in.readShort();
	}

	public String readUTF() throws IOException {
		return this.in.readUTF();
	}

	public int readUnsignedByte() throws IOException {
		return this.in.readUnsignedByte();
	}

	public int readUnsignedShort() throws IOException {
		return this.in.readUnsignedShort();
	}

	public int skipBytes(int n) throws IOException {
		return this.in.skipBytes(n);
	}
}