/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.demux;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.UnknownMessageTypeException;
import org.apache.mina.handler.demux.ExceptionHandler;
import org.apache.mina.handler.demux.MessageHandler;
import org.apache.mina.util.IdentityHashSet;

public class DemuxingIoHandler extends IoHandlerAdapter {
	private final Map<Class<?>, MessageHandler<?>> receivedMessageHandlerCache = new ConcurrentHashMap();
	private final Map<Class<?>, MessageHandler<?>> receivedMessageHandlers = new ConcurrentHashMap();
	private final Map<Class<?>, MessageHandler<?>> sentMessageHandlerCache = new ConcurrentHashMap();
	private final Map<Class<?>, MessageHandler<?>> sentMessageHandlers = new ConcurrentHashMap();
	private final Map<Class<?>, ExceptionHandler<?>> exceptionHandlerCache = new ConcurrentHashMap();
	private final Map<Class<?>, ExceptionHandler<?>> exceptionHandlers = new ConcurrentHashMap();

	public <E> MessageHandler<? super E> addReceivedMessageHandler(Class<E> type, MessageHandler<? super E> handler) {
		this.receivedMessageHandlerCache.clear();
		return (MessageHandler) this.receivedMessageHandlers.put(type, handler);
	}

	public <E> MessageHandler<? super E> removeReceivedMessageHandler(Class<E> type) {
		this.receivedMessageHandlerCache.clear();
		return (MessageHandler) this.receivedMessageHandlers.remove(type);
	}

	public <E> MessageHandler<? super E> addSentMessageHandler(Class<E> type, MessageHandler<? super E> handler) {
		this.sentMessageHandlerCache.clear();
		return (MessageHandler) this.sentMessageHandlers.put(type, handler);
	}

	public <E> MessageHandler<? super E> removeSentMessageHandler(Class<E> type) {
		this.sentMessageHandlerCache.clear();
		return (MessageHandler) this.sentMessageHandlers.remove(type);
	}

	public <E extends Throwable> ExceptionHandler<? super E> addExceptionHandler(Class<E> type,
			ExceptionHandler<? super E> handler) {
		this.exceptionHandlerCache.clear();
		return (ExceptionHandler) this.exceptionHandlers.put(type, handler);
	}

	public <E extends Throwable> ExceptionHandler<? super E> removeExceptionHandler(Class<E> type) {
		this.exceptionHandlerCache.clear();
		return (ExceptionHandler) this.exceptionHandlers.remove(type);
	}

	public <E> MessageHandler<? super E> getMessageHandler(Class<E> type) {
		return (MessageHandler) this.receivedMessageHandlers.get(type);
	}

	public Map<Class<?>, MessageHandler<?>> getReceivedMessageHandlerMap() {
		return Collections.unmodifiableMap(this.receivedMessageHandlers);
	}

	public Map<Class<?>, MessageHandler<?>> getSentMessageHandlerMap() {
		return Collections.unmodifiableMap(this.sentMessageHandlers);
	}

	public Map<Class<?>, ExceptionHandler<?>> getExceptionHandlerMap() {
		return Collections.unmodifiableMap(this.exceptionHandlers);
	}

	public void messageReceived(IoSession session, Object message) throws Exception {
		MessageHandler handler = this.findReceivedMessageHandler(message.getClass());
		if (handler != null) {
			handler.handleMessage(session, message);
		} else {
			throw new UnknownMessageTypeException(
					"No message handler found for message type: " + message.getClass().getSimpleName());
		}
	}

	public void messageSent(IoSession session, Object message) throws Exception {
		MessageHandler handler = this.findSentMessageHandler(message.getClass());
		if (handler != null) {
			handler.handleMessage(session, message);
		} else {
			throw new UnknownMessageTypeException(
					"No handler found for message type: " + message.getClass().getSimpleName());
		}
	}

	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		ExceptionHandler handler = this.findExceptionHandler(cause.getClass());
		if (handler != null) {
			handler.exceptionCaught(session, cause);
		} else {
			throw new UnknownMessageTypeException(
					"No handler found for exception type: " + cause.getClass().getSimpleName());
		}
	}

	protected MessageHandler<Object> findReceivedMessageHandler(Class<?> type) {
		return this.findReceivedMessageHandler(type, (Set) null);
	}

	protected MessageHandler<Object> findSentMessageHandler(Class<?> type) {
		return this.findSentMessageHandler(type, (Set) null);
	}

	protected ExceptionHandler<Throwable> findExceptionHandler(Class<? extends Throwable> type) {
		return this.findExceptionHandler(type, (Set) null);
	}

	private MessageHandler<Object> findReceivedMessageHandler(Class<?> type, Set<Class<?>> triedClasses) {
		return (MessageHandler) this.findHandler(this.receivedMessageHandlers, this.receivedMessageHandlerCache, type,
				triedClasses);
	}

	private MessageHandler<Object> findSentMessageHandler(Class<?> type, Set<Class<?>> triedClasses) {
		return (MessageHandler) this.findHandler(this.sentMessageHandlers, this.sentMessageHandlerCache, type,
				triedClasses);
	}

	private ExceptionHandler<Throwable> findExceptionHandler(Class<?> type, Set<Class<?>> triedClasses) {
		return (ExceptionHandler) this.findHandler(this.exceptionHandlers, this.exceptionHandlerCache, type,
				triedClasses);
	}

	private Object findHandler(Map<Class<?>, ?> handlers, Map handlerCache, Class<?> type, Set<Class<?>> triedClasses) {
		if (triedClasses != null && ((Set) triedClasses).contains(type)) {
			return null;
		} else {
			Object handler = handlerCache.get(type);
			if (handler != null) {
				return handler;
			} else {
				handler = handlers.get(type);
				if (handler == null) {
					if (triedClasses == null) {
						triedClasses = new IdentityHashSet();
					}

					((Set) triedClasses).add(type);
					Class[] superclass = type.getInterfaces();
					Class[] arr$ = superclass;
					int len$ = superclass.length;

					for (int i$ = 0; i$ < len$; ++i$) {
						Class element = arr$[i$];
						handler = this.findHandler(handlers, handlerCache, element, (Set) triedClasses);
						if (handler != null) {
							break;
						}
					}
				}

				if (handler == null) {
					Class arg10 = type.getSuperclass();
					if (arg10 != null) {
						handler = this.findHandler(handlers, handlerCache, arg10, (Set) null);
					}
				}

				if (handler != null) {
					handlerCache.put(type, handler);
				}

				return handler;
			}
		}
	}
}