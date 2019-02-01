/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.serialization;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import org.apache.mina.core.buffer.IoBuffer;

public class ObjectSerializationOutputStream extends OutputStream implements ObjectOutput {
	private final DataOutputStream out;
	private int maxObjectSize = Integer.MAX_VALUE;

	public ObjectSerializationOutputStream(OutputStream out) {
		if (out == null) {
			throw new IllegalArgumentException("out");
		} else {
			if (out instanceof DataOutputStream) {
				this.out = (DataOutputStream) out;
			} else {
				this.out = new DataOutputStream(out);
			}

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

	public void close() throws IOException {
		this.out.close();
	}

	public void flush() throws IOException {
		this.out.flush();
	}

	public void write(int b) throws IOException {
		this.out.write(b);
	}

	public void write(byte[] b) throws IOException {
		this.out.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		this.out.write(b, off, len);
	}

	public void writeObject(Object obj) throws IOException {
		IoBuffer buf = IoBuffer.allocate(64, false);
		buf.setAutoExpand(true);
		buf.putObject(obj);
		int objectSize = buf.position() - 4;
		if (objectSize > this.maxObjectSize) {
			throw new IllegalArgumentException(
					"The encoded object is too big: " + objectSize + " (> " + this.maxObjectSize + ')');
		} else {
			this.out.write(buf.array(), 0, buf.position());
		}
	}

	public void writeBoolean(boolean v) throws IOException {
		this.out.writeBoolean(v);
	}

	public void writeByte(int v) throws IOException {
		this.out.writeByte(v);
	}

	public void writeBytes(String s) throws IOException {
		this.out.writeBytes(s);
	}

	public void writeChar(int v) throws IOException {
		this.out.writeChar(v);
	}

	public void writeChars(String s) throws IOException {
		this.out.writeChars(s);
	}

	public void writeDouble(double v) throws IOException {
		this.out.writeDouble(v);
	}

	public void writeFloat(float v) throws IOException {
		this.out.writeFloat(v);
	}

	public void writeInt(int v) throws IOException {
		this.out.writeInt(v);
	}

	public void writeLong(long v) throws IOException {
		this.out.writeLong(v);
	}

	public void writeShort(int v) throws IOException {
		this.out.writeShort(v);
	}

	public void writeUTF(String str) throws IOException {
		this.out.writeUTF(str);
	}
}