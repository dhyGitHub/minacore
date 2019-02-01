/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public interface ProtocolDecoder {
	void decode(IoSession arg0, IoBuffer arg1, ProtocolDecoderOutput arg2) throws Exception;

	void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1) throws Exception;

	void dispose(IoSession arg0) throws Exception;
}