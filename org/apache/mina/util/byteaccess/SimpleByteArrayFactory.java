/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util.byteaccess;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.byteaccess.BufferByteArray;
import org.apache.mina.util.byteaccess.ByteArray;
import org.apache.mina.util.byteaccess.ByteArrayFactory;

public class SimpleByteArrayFactory implements ByteArrayFactory {
	public ByteArray create(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Buffer size must not be negative:" + size);
		} else {
			final IoBuffer bb = IoBuffer.allocate(size);
			BufferByteArray ba = new BufferByteArray(bb) {
				public void free() {
				}
			};
			return ba;
		}
	}
}