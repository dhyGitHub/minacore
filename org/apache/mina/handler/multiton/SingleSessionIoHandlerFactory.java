/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.multiton;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.multiton.SingleSessionIoHandler;

@Deprecated
public interface SingleSessionIoHandlerFactory {
	SingleSessionIoHandler getHandler(IoSession arg0) throws Exception;
}