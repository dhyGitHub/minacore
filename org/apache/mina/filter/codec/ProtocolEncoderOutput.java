/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.core.future.WriteFuture;

public interface ProtocolEncoderOutput {
	void write(Object arg0);

	void mergeAll();

	WriteFuture flush();
}