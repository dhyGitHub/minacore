/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.demux;

import java.util.Iterator;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.demux.DemuxingProtocolDecoder;
import org.apache.mina.filter.codec.demux.DemuxingProtocolEncoder;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderFactory;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.apache.mina.filter.codec.demux.MessageEncoderFactory;

public class DemuxingProtocolCodecFactory implements ProtocolCodecFactory {
	private final DemuxingProtocolEncoder encoder = new DemuxingProtocolEncoder();
	private final DemuxingProtocolDecoder decoder = new DemuxingProtocolDecoder();

	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return this.encoder;
	}

	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return this.decoder;
	}

	public void addMessageEncoder(Class<?> messageType, Class<? extends MessageEncoder> encoderClass) {
		this.encoder.addMessageEncoder(messageType, encoderClass);
	}

	public <T> void addMessageEncoder(Class<T> messageType, MessageEncoder<? super T> encoder) {
		this.encoder.addMessageEncoder(messageType, encoder);
	}

	public <T> void addMessageEncoder(Class<T> messageType, MessageEncoderFactory<? super T> factory) {
		this.encoder.addMessageEncoder(messageType, factory);
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

	public void addMessageDecoder(Class<? extends MessageDecoder> decoderClass) {
		this.decoder.addMessageDecoder(decoderClass);
	}

	public void addMessageDecoder(MessageDecoder decoder) {
		this.decoder.addMessageDecoder(decoder);
	}

	public void addMessageDecoder(MessageDecoderFactory factory) {
		this.decoder.addMessageDecoder(factory);
	}
}