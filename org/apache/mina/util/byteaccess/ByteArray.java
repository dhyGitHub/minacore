/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.byteaccess.IoAbsoluteReader;
import org.apache.mina.util.byteaccess.IoAbsoluteWriter;
import org.apache.mina.util.byteaccess.IoRelativeReader;
import org.apache.mina.util.byteaccess.IoRelativeWriter;

public interface ByteArray extends IoAbsoluteReader, IoAbsoluteWriter {
	int first();

	int last();

	ByteOrder order();

	void order(ByteOrder arg0);

	void free();

	Iterable<IoBuffer> getIoBuffers();

	IoBuffer getSingleIoBuffer();

	boolean equals(Object arg0);

	byte get(int arg0);

	void get(int arg0, IoBuffer arg1);

	int getInt(int arg0);

	ByteArray.Cursor cursor();

	ByteArray.Cursor cursor(int arg0);

	public interface Cursor extends IoRelativeReader, IoRelativeWriter {
		int getIndex();

		void setIndex(int arg0);

		int getRemaining();

		boolean hasRemaining();

		byte get();

		void get(IoBuffer arg0);

		int getInt();
	}
}