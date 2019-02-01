/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public interface DecodingState {
	DecodingState decode(IoBuffer arg0, ProtocolDecoderOutput arg1) throws Exception;

	DecodingState finishDecode(ProtocolDecoderOutput arg0) throws Exception;
}