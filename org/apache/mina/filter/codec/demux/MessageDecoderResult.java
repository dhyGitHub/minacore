/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.demux;

public class MessageDecoderResult {
	public static final MessageDecoderResult OK = new MessageDecoderResult("OK");
	public static final MessageDecoderResult NEED_DATA = new MessageDecoderResult("NEED_DATA");
	public static final MessageDecoderResult NOT_OK = new MessageDecoderResult("NOT_OK");
	private final String name;

	private MessageDecoderResult(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}
}