/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.filter.codec.statemachine.SkippingState;

public abstract class LinearWhitespaceSkippingState extends SkippingState {
	protected boolean canSkip(byte b) {
		return b == 32 || b == 9;
	}
}