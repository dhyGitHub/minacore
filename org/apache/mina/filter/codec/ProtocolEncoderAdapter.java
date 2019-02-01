/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;

public abstract class ProtocolEncoderAdapter implements ProtocolEncoder {
	public void dispose(IoSession session) throws Exception {
	}
}