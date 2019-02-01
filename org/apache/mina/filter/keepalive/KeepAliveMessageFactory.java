/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.keepalive;

import org.apache.mina.core.session.IoSession;

public interface KeepAliveMessageFactory {
	boolean isRequest(IoSession arg0, Object arg1);

	boolean isResponse(IoSession arg0, Object arg1);

	Object getRequest(IoSession arg0);

	Object getResponse(IoSession arg0, Object arg1);
}