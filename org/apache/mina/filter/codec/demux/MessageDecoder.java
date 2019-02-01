/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.demux;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

public interface MessageDecoder {
	MessageDecoderResult OK = MessageDecoderResult.OK;
	MessageDecoderResult NEED_DATA = MessageDecoderResult.NEED_DATA;
	MessageDecoderResult NOT_OK = MessageDecoderResult.NOT_OK;

	MessageDecoderResult decodable(IoSession arg0, IoBuffer arg1);

	MessageDecoderResult decode(IoSession arg0, IoBuffer arg1, ProtocolDecoderOutput arg2) throws Exception;

	void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1) throws Exception;
}