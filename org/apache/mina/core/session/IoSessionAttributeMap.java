/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import java.util.Set;
import org.apache.mina.core.session.IoSession;

public interface IoSessionAttributeMap {
	Object getAttribute(IoSession arg0, Object arg1, Object arg2);

	Object setAttribute(IoSession arg0, Object arg1, Object arg2);

	Object setAttributeIfAbsent(IoSession arg0, Object arg1, Object arg2);

	Object removeAttribute(IoSession arg0, Object arg1);

	boolean removeAttribute(IoSession arg0, Object arg1, Object arg2);

	boolean replaceAttribute(IoSession arg0, Object arg1, Object arg2, Object arg3);

	boolean containsAttribute(IoSession arg0, Object arg1);

	Set<Object> getAttributeKeys(IoSession arg0);

	void dispose(IoSession arg0) throws Exception;
}