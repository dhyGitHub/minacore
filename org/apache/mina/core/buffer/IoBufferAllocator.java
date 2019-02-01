/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.buffer;

import java.nio.ByteBuffer;
import org.apache.mina.core.buffer.IoBuffer;

public interface IoBufferAllocator {
	IoBuffer allocate(int arg0, boolean arg1);

	ByteBuffer allocateNioBuffer(int arg0, boolean arg1);

	IoBuffer wrap(ByteBuffer arg0);

	void dispose();
}