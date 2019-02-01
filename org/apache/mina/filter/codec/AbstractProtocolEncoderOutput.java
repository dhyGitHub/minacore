/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public abstract class AbstractProtocolEncoderOutput implements ProtocolEncoderOutput {
	private final Queue<Object> messageQueue = new ConcurrentLinkedQueue();
	private boolean buffersOnly = true;

	public Queue<Object> getMessageQueue() {
		return this.messageQueue;
	}

	public void write(Object encodedMessage) {
		if (encodedMessage instanceof IoBuffer) {
			IoBuffer buf = (IoBuffer) encodedMessage;
			if (!buf.hasRemaining()) {
				throw new IllegalArgumentException("buf is empty. Forgot to call flip()?");
			}

			this.messageQueue.offer(buf);
		} else {
			this.messageQueue.offer(encodedMessage);
			this.buffersOnly = false;
		}

	}

	public void mergeAll() {
		if (!this.buffersOnly) {
			throw new IllegalStateException("the encoded message list contains a non-buffer.");
		} else {
			int size = this.messageQueue.size();
			if (size >= 2) {
				int sum = 0;

				Object buf;
				for (Iterator newBuf = this.messageQueue.iterator(); newBuf
						.hasNext(); sum += ((IoBuffer) buf).remaining()) {
					buf = newBuf.next();
				}

				IoBuffer newBuf1 = IoBuffer.allocate(sum);

				while (true) {
					IoBuffer buf1 = (IoBuffer) this.messageQueue.poll();
					if (buf1 == null) {
						newBuf1.flip();
						this.messageQueue.add(newBuf1);
						return;
					}

					newBuf1.put(buf1);
				}
			}
		}
	}
}