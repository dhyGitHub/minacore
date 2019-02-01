/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import java.util.LinkedList;
import java.util.Queue;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public abstract class AbstractProtocolDecoderOutput implements ProtocolDecoderOutput {
	private final Queue<Object> messageQueue = new LinkedList();

	public Queue<Object> getMessageQueue() {
		return this.messageQueue;
	}

	public void write(Object message) {
		if (message == null) {
			throw new IllegalArgumentException("message");
		} else {
			this.messageQueue.add(message);
		}
	}
}