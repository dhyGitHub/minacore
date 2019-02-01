/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;

public interface IoRelativeWriter {
	int getRemaining();

	boolean hasRemaining();

	void skip(int arg0);

	ByteOrder order();

	void put(byte arg0);

	void put(IoBuffer arg0);

	void putShort(short arg0);

	void putInt(int arg0);

	void putLong(long arg0);

	void putFloat(float arg0);

	void putDouble(double arg0);

	void putChar(char arg0);
}