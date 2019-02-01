/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.demux;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderFactory;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

public class DemuxingProtocolDecoder extends CumulativeProtocolDecoder {
	private final AttributeKey STATE = new AttributeKey(this.getClass(), "state");
	private MessageDecoderFactory[] decoderFactories = new MessageDecoderFactory[0];
	private static final Class<?>[] EMPTY_PARAMS = new Class[0];

	public void addMessageDecoder(Class<? extends MessageDecoder> decoderClass) {
		if (decoderClass == null) {
			throw new IllegalArgumentException("decoderClass");
		} else {
			try {
				decoderClass.getConstructor(EMPTY_PARAMS);
			} catch (NoSuchMethodException arg2) {
				throw new IllegalArgumentException("The specified class doesn\'t have a public default constructor.");
			}

			boolean registered = false;
			if (MessageDecoder.class.isAssignableFrom(decoderClass)) {
				this.addMessageDecoder(
						(MessageDecoderFactory) (new DemuxingProtocolDecoder.DefaultConstructorMessageDecoderFactory(
								decoderClass)));
				registered = true;
			}

			if (!registered) {
				throw new IllegalArgumentException("Unregisterable type: " + decoderClass);
			}
		}
	}

	public void addMessageDecoder(MessageDecoder decoder) {
		this.addMessageDecoder(
				(MessageDecoderFactory) (new DemuxingProtocolDecoder.SingletonMessageDecoderFactory(decoder)));
	}

	public void addMessageDecoder(MessageDecoderFactory factory) {
		if (factory == null) {
			throw new IllegalArgumentException("factory");
		} else {
			MessageDecoderFactory[] decoderFactories = this.decoderFactories;
			MessageDecoderFactory[] newDecoderFactories = new MessageDecoderFactory[decoderFactories.length + 1];
			System.arraycopy(decoderFactories, 0, newDecoderFactories, 0, decoderFactories.length);
			newDecoderFactories[decoderFactories.length] = factory;
			this.decoderFactories = newDecoderFactories;
		}
	}

	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		DemuxingProtocolDecoder.State state = this.getState(session);
		if (state.currentDecoder == null) {
			MessageDecoder[] e = state.decoders;
			int undecodables = 0;

			for (int dump = e.length - 1; dump >= 0; --dump) {
				MessageDecoder e1 = e[dump];
				int limit = in.limit();
				int pos = in.position();

				MessageDecoderResult result;
				try {
					result = e1.decodable(session, in);
				} finally {
					in.position(pos);
					in.limit(limit);
				}

				if (result == MessageDecoder.OK) {
					state.currentDecoder = e1;
					break;
				}

				if (result == MessageDecoder.NOT_OK) {
					++undecodables;
				} else if (result != MessageDecoder.NEED_DATA) {
					throw new IllegalStateException("Unexpected decode result (see your decodable()): " + result);
				}
			}

			if (undecodables == e.length) {
				String arg17 = in.getHexDump();
				in.position(in.limit());
				ProtocolDecoderException arg18 = new ProtocolDecoderException(
						"No appropriate message decoder: " + arg17);
				arg18.setHexdump(arg17);
				throw arg18;
			}

			if (state.currentDecoder == null) {
				return false;
			}
		}

		try {
			MessageDecoderResult arg16 = state.currentDecoder.decode(session, in, out);
			if (arg16 == MessageDecoder.OK) {
				state.currentDecoder = null;
				return true;
			} else if (arg16 == MessageDecoder.NEED_DATA) {
				return false;
			} else if (arg16 == MessageDecoder.NOT_OK) {
				state.currentDecoder = null;
				throw new ProtocolDecoderException("Message decoder returned NOT_OK.");
			} else {
				state.currentDecoder = null;
				throw new IllegalStateException("Unexpected decode result (see your decode()): " + arg16);
			}
		} catch (Exception arg14) {
			state.currentDecoder = null;
			throw arg14;
		}
	}

	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
		super.finishDecode(session, out);
		DemuxingProtocolDecoder.State state = this.getState(session);
		MessageDecoder currentDecoder = state.currentDecoder;
		if (currentDecoder != null) {
			currentDecoder.finishDecode(session, out);
		}
	}

	public void dispose(IoSession session) throws Exception {
		super.dispose(session);
		session.removeAttribute(this.STATE);
	}

	private DemuxingProtocolDecoder.State getState(IoSession session) throws Exception {
		DemuxingProtocolDecoder.State state = (DemuxingProtocolDecoder.State) session.getAttribute(this.STATE);
		if (state == null) {
			state = new DemuxingProtocolDecoder.State();
			DemuxingProtocolDecoder.State oldState = (DemuxingProtocolDecoder.State) session
					.setAttributeIfAbsent(this.STATE, state);
			if (oldState != null) {
				state = oldState;
			}
		}

		return state;
	}

	private static class DefaultConstructorMessageDecoderFactory implements MessageDecoderFactory {
		private final Class<?> decoderClass;

		private DefaultConstructorMessageDecoderFactory(Class<?> decoderClass) {
			if (decoderClass == null) {
				throw new IllegalArgumentException("decoderClass");
			} else if (!MessageDecoder.class.isAssignableFrom(decoderClass)) {
				throw new IllegalArgumentException("decoderClass is not assignable to MessageDecoder");
			} else {
				this.decoderClass = decoderClass;
			}
		}

		public MessageDecoder getDecoder() throws Exception {
			return (MessageDecoder) this.decoderClass.newInstance();
		}
	}

	private static class SingletonMessageDecoderFactory implements MessageDecoderFactory {
		private final MessageDecoder decoder;

		private SingletonMessageDecoderFactory(MessageDecoder decoder) {
			if (decoder == null) {
				throw new IllegalArgumentException("decoder");
			} else {
				this.decoder = decoder;
			}
		}

		public MessageDecoder getDecoder() {
			return this.decoder;
		}
	}

	private class State {
		private final MessageDecoder[] decoders;
		private MessageDecoder currentDecoder;

		private State() throws Exception {
			MessageDecoderFactory[] decoderFactories = DemuxingProtocolDecoder.this.decoderFactories;
			this.decoders = new MessageDecoder[decoderFactories.length];

			for (int i = decoderFactories.length - 1; i >= 0; --i) {
				this.decoders[i] = decoderFactories[i].getDecoder();
			}

		}
	}
}