/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.filter.codec.statemachine.ConsumeToDynamicTerminatorDecodingState;

public abstract class ConsumeToLinearWhitespaceDecodingState extends ConsumeToDynamicTerminatorDecodingState {
	protected boolean isTerminator(byte b) {
		return b == 32 || b == 9;
	}
}