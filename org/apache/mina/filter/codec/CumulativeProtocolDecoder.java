/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public abstract class CumulativeProtocolDecoder extends ProtocolDecoderAdapter {
	private final AttributeKey BUFFER = new AttributeKey(this.getClass(), "buffer");
	private boolean transportMetadataFragmentation = true;

	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (this.transportMetadataFragmentation && !session.getTransportMetadata().hasFragmentation()) {
			while (in.hasRemaining() && this.doDecode(session, in, out)) {
				;
			}

		} else {
			boolean usingSessionBuffer = true;
			IoBuffer buf = (IoBuffer) session.getAttribute(this.BUFFER);
			if (buf != null) {
				boolean oldPos = false;
				if (buf.isAutoExpand()) {
					try {
						buf.put(in);
						oldPos = true;
					} catch (IllegalStateException arg7) {
						;
					} catch (IndexOutOfBoundsException arg8) {
						;
					}
				}

				if (oldPos) {
					buf.flip();
				} else {
					buf.flip();
					IoBuffer decoded = IoBuffer.allocate(buf.remaining() + in.remaining()).setAutoExpand(true);
					decoded.order(buf.order());
					decoded.put(buf);
					decoded.put(in);
					decoded.flip();
					buf = decoded;
					session.setAttribute(this.BUFFER, decoded);
				}
			} else {
				buf = in;
				usingSessionBuffer = false;
			}

			do {
				int oldPos1 = buf.position();
				boolean decoded1 = this.doDecode(session, buf, out);
				if (!decoded1) {
					break;
				}

				if (buf.position() == oldPos1) {
					throw new IllegalStateException("doDecode() can\'t return true when buffer is not consumed.");
				}
			} while (buf.hasRemaining());

			if (buf.hasRemaining()) {
				if (usingSessionBuffer && buf.isAutoExpand()) {
					buf.compact();
				} else {
					this.storeRemainingInSession(buf, session);
				}
			} else if (usingSessionBuffer) {
				this.removeSessionBuffer(session);
			}

		}
	}

	protected abstract boolean doDecode(IoSession arg0, IoBuffer arg1, ProtocolDecoderOutput arg2) throws Exception;

	public void dispose(IoSession session) throws Exception {
		this.removeSessionBuffer(session);
	}

	private void removeSessionBuffer(IoSession session) {
		session.removeAttribute(this.BUFFER);
	}

	private void storeRemainingInSession(IoBuffer buf, IoSession session) {
		IoBuffer remainingBuf = IoBuffer.allocate(buf.capacity()).setAutoExpand(true);
		remainingBuf.order(buf.order());
		remainingBuf.put(buf);
		session.setAttribute(this.BUFFER, remainingBuf);
	}

	public void setTransportMetadataFragmentation(boolean transportMetadataFragmentation) {
		this.transportMetadataFragmentation = transportMetadataFragmentation;
	}
}