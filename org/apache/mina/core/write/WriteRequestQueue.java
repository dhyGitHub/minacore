/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.write;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public interface WriteRequestQueue {
	WriteRequest poll(IoSession arg0);

	void offer(IoSession arg0, WriteRequest arg1);

	boolean isEmpty(IoSession arg0);

	void clear(IoSession arg0);

	void dispose(IoSession arg0);

	int size();
}