/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.demux;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public interface MessageEncoder<T> {
	void encode(IoSession arg0, T arg1, ProtocolEncoderOutput arg2) throws Exception;
}