/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;

public interface IoAbsoluteWriter {
	int first();

	int last();

	ByteOrder order();

	void put(int arg0, byte arg1);

	void put(int arg0, IoBuffer arg1);

	void putShort(int arg0, short arg1);

	void putInt(int arg0, int arg1);

	void putLong(int arg0, long arg1);

	void putFloat(int arg0, float arg1);

	void putDouble(int arg0, double arg1);

	void putChar(int arg0, char arg1);
}