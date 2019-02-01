/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.byteaccess.ByteArray;

public interface IoRelativeReader {
	int getRemaining();

	boolean hasRemaining();

	void skip(int arg0);

	ByteArray slice(int arg0);

	ByteOrder order();

	byte get();

	void get(IoBuffer arg0);

	short getShort();

	int getInt();

	long getLong();

	float getFloat();

	double getDouble();

	char getChar();
}