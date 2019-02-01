/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.byteaccess.ByteArray;

public interface IoAbsoluteReader {
	int first();

	int last();

	int length();

	ByteArray slice(int arg0, int arg1);

	ByteOrder order();

	byte get(int arg0);

	void get(int arg0, IoBuffer arg1);

	short getShort(int arg0);

	int getInt(int arg0);

	long getLong(int arg0);

	float getFloat(int arg0);

	double getDouble(int arg0);

	char getChar(int arg0);
}