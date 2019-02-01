/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public abstract class ProtocolDecoderAdapter implements ProtocolDecoder {
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
	}

	public void dispose(IoSession session) throws Exception {
	}
}