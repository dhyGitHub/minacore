/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util.byteaccess;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.byteaccess.ByteArray;
import org.apache.mina.util.byteaccess.CompositeByteArray;
import org.apache.mina.util.byteaccess.CompositeByteArrayRelativeBase;
import org.apache.mina.util.byteaccess.IoRelativeReader;

public class CompositeByteArrayRelativeReader extends CompositeByteArrayRelativeBase implements IoRelativeReader {
	private final boolean autoFree;

	public CompositeByteArrayRelativeReader(CompositeByteArray cba, boolean autoFree) {
		super(cba);
		this.autoFree = autoFree;
	}

	protected void cursorPassedFirstComponent() {
		if (this.autoFree) {
			this.cba.removeFirst().free();
		}

	}

	public void skip(int length) {
		this.cursor.skip(length);
	}

	public ByteArray slice(int length) {
		return this.cursor.slice(length);
	}

	public byte get() {
		return this.cursor.get();
	}

	public void get(IoBuffer bb) {
		this.cursor.get(bb);
	}

	public short getShort() {
		return this.cursor.getShort();
	}

	public int getInt() {
		return this.cursor.getInt();
	}

	public long getLong() {
		return this.cursor.getLong();
	}

	public float getFloat() {
		return this.cursor.getFloat();
	}

	public double getDouble() {
		return this.cursor.getDouble();
	}

	public char getChar() {
		return this.cursor.getChar();
	}
}