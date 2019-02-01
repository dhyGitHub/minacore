/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.demux;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.UnknownMessageTypeException;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.apache.mina.filter.codec.demux.MessageEncoderFactory;
import org.apache.mina.util.CopyOnWriteMap;
import org.apache.mina.util.IdentityHashSet;

public class DemuxingProtocolEncoder implements ProtocolEncoder {
	private final AttributeKey STATE = new AttributeKey(this.getClass(), "state");
	private final Map<Class<?>, MessageEncoderFactory> type2encoderFactory = new CopyOnWriteMap();
	private static final Class<?>[] EMPTY_PARAMS = new Class[0];

	public void addMessageEncoder(Class<?> messageType, Class<? extends MessageEncoder> encoderClass) {
		if (encoderClass == null) {
			throw new IllegalArgumentException("encoderClass");
		} else {
			try {
				encoderClass.getConstructor(EMPTY_PARAMS);
			} catch (NoSuchMethodException arg3) {
				throw new IllegalArgumentException("The specified class doesn\'t have a public default constructor.");
			}

			boolean registered = false;
			if (MessageEncoder.class.isAssignableFrom(encoderClass)) {
				this.addMessageEncoder((Class) messageType,
						(MessageEncoderFactory) (new DemuxingProtocolEncoder.DefaultConstructorMessageEncoderFactory(
								encoderClass)));
				registered = true;
			}

			if (!registered) {
				throw new IllegalArgumentException("Unregisterable type: " + encoderClass);
			}
		}
	}

	public <T> void addMessageEncoder(Class<T> messageType, MessageEncoder<? super T> encoder) {
		this.addMessageEncoder((Class) messageType,
				(MessageEncoderFactory) (new DemuxingProtocolEncoder.SingletonMessageEncoderFactory(encoder)));
	}

	public <T> void addMessageEncoder(Class<T> messageType, MessageEncoderFactory<? super T> factory) {
		if (messageType == null) {
			throw new IllegalArgumentException("messageType");
		} else if (factory == null) {
			throw new IllegalArgumentException("factory");
		} else {
			Map arg2 = this.type2encoderFactory;
			synchronized (this.type2encoderFactory) {
				if (this.type2encoderFactory.containsKey(messageType)) {
					throw new IllegalStateException(
							"The specified message type (" + messageType.getName() + ") is registered already.");
				} else {
					this.type2encoderFactory.put(messageType, factory);
				}
			}
		}
	}

	public void addMessageEncoder(Iterable<Class<?>> messageTypes, Class<? extends MessageEncoder> encoderClass) {
		Iterator i$ = messageTypes.iterator();

		while (i$.hasNext()) {
			Class messageType = (Class) i$.next();
			this.addMessageEncoder(messageType, encoderClass);
		}

	}

	public <T> void addMessageEncoder(Iterable<Class<? extends T>> messageTypes, MessageEncoder<? super T> encoder) {
		Iterator i$ = messageTypes.iterator();

		while (i$.hasNext()) {
			Class messageType = (Class) i$.next();
			this.addMessageEncoder(messageType, encoder);
		}

	}

	public <T> void addMessageEncoder(Iterable<Class<? extends T>> messageTypes,
			MessageEncoderFactory<? super T> factory) {
		Iterator i$ = messageTypes.iterator();

		while (i$.hasNext()) {
			Class messageType = (Class) i$.next();
			this.addMessageEncoder(messageType, factory);
		}

	}

	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		DemuxingProtocolEncoder.State state = this.getState(session);
		MessageEncoder encoder = this.findEncoder(state, message.getClass());
		if (encoder != null) {
			encoder.encode(session, message, out);
		} else {
			throw new UnknownMessageTypeException("No message encoder found for message: " + message);
		}
	}

	protected MessageEncoder<Object> findEncoder(DemuxingProtocolEncoder.State state, Class<?> type) {
		return this.findEncoder(state, type, (Set) null);
	}

	private MessageEncoder<Object> findEncoder(DemuxingProtocolEncoder.State state, Class<?> type,
			Set<Class<?>> triedClasses) {
		MessageEncoder encoder = null;
		if (triedClasses != null && ((Set) triedClasses).contains(type)) {
			return null;
		} else {
			encoder = (MessageEncoder) state.findEncoderCache.get(type);
			if (encoder != null) {
				return encoder;
			} else {
				encoder = (MessageEncoder) state.type2encoder.get(type);
				if (encoder == null) {
					if (triedClasses == null) {
						triedClasses = new IdentityHashSet();
					}

					((Set) triedClasses).add(type);
					Class[] tmpEncoder = type.getInterfaces();
					Class[] arr$ = tmpEncoder;
					int len$ = tmpEncoder.length;

					for (int i$ = 0; i$ < len$; ++i$) {
						Class element = arr$[i$];
						encoder = this.findEncoder(state, element, (Set) triedClasses);
						if (encoder != null) {
							break;
						}
					}
				}

				if (encoder == null) {
					Class arg9 = type.getSuperclass();
					if (arg9 != null) {
						encoder = this.findEncoder(state, arg9);
					}
				}

				if (encoder != null) {
					state.findEncoderCache.put(type, encoder);
					MessageEncoder arg10 = (MessageEncoder) state.findEncoderCache.putIfAbsent(type, encoder);
					if (arg10 != null) {
						encoder = arg10;
					}
				}

				return encoder;
			}
		}
	}

	public void dispose(IoSession session) throws Exception {
		session.removeAttribute(this.STATE);
	}

	private DemuxingProtocolEncoder.State getState(IoSession session) throws Exception {
		DemuxingProtocolEncoder.State state = (DemuxingProtocolEncoder.State) session.getAttribute(this.STATE);
		if (state == null) {
			state = new DemuxingProtocolEncoder.State();
			DemuxingProtocolEncoder.State oldState = (DemuxingProtocolEncoder.State) session
					.setAttributeIfAbsent(this.STATE, state);
			if (oldState != null) {
				state = oldState;
			}
		}

		return state;
	}

	private static class DefaultConstructorMessageEncoderFactory<T> implements MessageEncoderFactory<T> {
		private final Class<MessageEncoder<T>> encoderClass;

		private DefaultConstructorMessageEncoderFactory(Class<MessageEncoder<T>> encoderClass) {
			if (encoderClass == null) {
				throw new IllegalArgumentException("encoderClass");
			} else if (!MessageEncoder.class.isAssignableFrom(encoderClass)) {
				throw new IllegalArgumentException("encoderClass is not assignable to MessageEncoder");
			} else {
				this.encoderClass = encoderClass;
			}
		}

		public MessageEncoder<T> getEncoder() throws Exception {
			return (MessageEncoder) this.encoderClass.newInstance();
		}
	}

	private static class SingletonMessageEncoderFactory<T> implements MessageEncoderFactory<T> {
		private final MessageEncoder<T> encoder;

		private SingletonMessageEncoderFactory(MessageEncoder<T> encoder) {
			if (encoder == null) {
				throw new IllegalArgumentException("encoder");
			} else {
				this.encoder = encoder;
			}
		}

		public MessageEncoder<T> getEncoder() {
			return this.encoder;
		}
	}

	private class State {
		private final ConcurrentHashMap<Class<?>, MessageEncoder> findEncoderCache;
		private final Map<Class<?>, MessageEncoder> type2encoder;

		private State() throws Exception {
			this.findEncoderCache = new ConcurrentHashMap();
			this.type2encoder = new ConcurrentHashMap();
			Iterator i$ = DemuxingProtocolEncoder.this.type2encoderFactory.entrySet().iterator();

			while (i$.hasNext()) {
				Entry e = (Entry) i$.next();
				this.type2encoder.put(e.getKey(), ((MessageEncoderFactory) e.getValue()).getEncoder());
			}

		}
	}
}