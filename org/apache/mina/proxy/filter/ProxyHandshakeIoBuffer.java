/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.filter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.IoBufferWrapper;

public class ProxyHandshakeIoBuffer extends IoBufferWrapper {
	public ProxyHandshakeIoBuffer(IoBuffer buf) {
		super(buf);
	}
}