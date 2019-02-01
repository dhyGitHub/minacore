/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public interface ProtocolEncoder {
	void encode(IoSession arg0, Object arg1, ProtocolEncoderOutput arg2) throws Exception;

	void dispose(IoSession arg0) throws Exception;
}