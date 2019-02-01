/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public interface ProtocolCodecFactory {
	ProtocolEncoder getEncoder(IoSession arg0) throws Exception;

	ProtocolDecoder getDecoder(IoSession arg0) throws Exception;
}