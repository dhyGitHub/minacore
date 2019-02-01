/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.demux;

import org.apache.mina.filter.codec.demux.MessageEncoder;

public interface MessageEncoderFactory<T> {
	MessageEncoder<T> getEncoder() throws Exception;
}