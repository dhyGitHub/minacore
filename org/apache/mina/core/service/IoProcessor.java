/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.service;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public interface IoProcessor<S extends IoSession> {
	boolean isDisposing();

	boolean isDisposed();

	void dispose();

	void add(S arg0);

	void flush(S arg0);

	void write(S arg0, WriteRequest arg1);

	void updateTrafficControl(S arg0);

	void remove(S arg0);
}