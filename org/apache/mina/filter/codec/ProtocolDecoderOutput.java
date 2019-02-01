/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;

public interface ProtocolDecoderOutput {
	void write(Object arg0);

	void flush(NextFilter arg0, IoSession arg1);
}