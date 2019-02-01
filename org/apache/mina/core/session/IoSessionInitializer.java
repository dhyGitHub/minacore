/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.session.IoSession;

public interface IoSessionInitializer<T extends IoFuture> {
	void initializeSession(IoSession arg0, T arg1);
}