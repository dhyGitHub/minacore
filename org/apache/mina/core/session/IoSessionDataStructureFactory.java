/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionAttributeMap;
import org.apache.mina.core.write.WriteRequestQueue;

public interface IoSessionDataStructureFactory {
	IoSessionAttributeMap getAttributeMap(IoSession arg0) throws Exception;

	WriteRequestQueue getWriteRequestQueue(IoSession arg0) throws Exception;
}