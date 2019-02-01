/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.logging;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.filterchain.IoFilterEvent;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.util.CommonEventFilter;
import org.slf4j.MDC;

public class MdcInjectionFilter extends CommonEventFilter {
	private static final AttributeKey CONTEXT_KEY = new AttributeKey(MdcInjectionFilter.class, "context");
	private ThreadLocal<Integer> callDepth = new ThreadLocal() {
		protected Integer initialValue() {
			return Integer.valueOf(0);
		}
	};
	private EnumSet<MdcInjectionFilter.MdcKey> mdcKeys;

	public MdcInjectionFilter(EnumSet<MdcInjectionFilter.MdcKey> keys) {
		this.mdcKeys = keys.clone();
	}

	public MdcInjectionFilter(MdcInjectionFilter.MdcKey... keys) {
		HashSet keySet = new HashSet(Arrays.asList(keys));
		this.mdcKeys = EnumSet.copyOf(keySet);
	}

	public MdcInjectionFilter() {
		this.mdcKeys = EnumSet.allOf(MdcInjectionFilter.MdcKey.class);
	}

	protected void filter(IoFilterEvent event) throws Exception {
		int currentCallDepth = ((Integer) this.callDepth.get()).intValue();
		this.callDepth.set(Integer.valueOf(currentCallDepth + 1));
		Map context = this.getAndFillContext(event.getSession());
		Iterator i$;
		if (currentCallDepth == 0) {
			i$ = context.entrySet().iterator();

			while (i$.hasNext()) {
				Entry key = (Entry) i$.next();
				MDC.put((String) key.getKey(), (String) key.getValue());
			}
		}

		boolean arg9 = false;

		try {
			arg9 = true;
			event.fire();
			arg9 = false;
		} finally {
			if (arg9) {
				if (currentCallDepth == 0) {
					Iterator i$1 = context.keySet().iterator();

					while (i$1.hasNext()) {
						String key1 = (String) i$1.next();
						MDC.remove(key1);
					}

					this.callDepth.remove();
				} else {
					this.callDepth.set(Integer.valueOf(currentCallDepth));
				}

			}
		}

		if (currentCallDepth == 0) {
			i$ = context.keySet().iterator();

			while (i$.hasNext()) {
				String key2 = (String) i$.next();
				MDC.remove(key2);
			}

			this.callDepth.remove();
		} else {
			this.callDepth.set(Integer.valueOf(currentCallDepth));
		}

	}

	private Map<String, String> getAndFillContext(IoSession session) {
		Map context = getContext(session);
		if (context.isEmpty()) {
			this.fillContext(session, context);
		}

		return context;
	}

	private static Map<String, String> getContext(IoSession session) {
		Object context = (Map) session.getAttribute(CONTEXT_KEY);
		if (context == null) {
			context = new ConcurrentHashMap();
			session.setAttribute(CONTEXT_KEY, context);
		}

		return (Map) context;
	}

	protected void fillContext(IoSession session, Map<String, String> context) {
		if (this.mdcKeys.contains(MdcInjectionFilter.MdcKey.handlerClass)) {
			context.put(MdcInjectionFilter.MdcKey.handlerClass.name(), session.getHandler().getClass().getName());
		}

		if (this.mdcKeys.contains(MdcInjectionFilter.MdcKey.remoteAddress)) {
			context.put(MdcInjectionFilter.MdcKey.remoteAddress.name(), session.getRemoteAddress().toString());
		}

		if (this.mdcKeys.contains(MdcInjectionFilter.MdcKey.localAddress)) {
			context.put(MdcInjectionFilter.MdcKey.localAddress.name(), session.getLocalAddress().toString());
		}

		if (session.getTransportMetadata().getAddressType() == InetSocketAddress.class) {
			InetSocketAddress remoteAddress = (InetSocketAddress) session.getRemoteAddress();
			InetSocketAddress localAddress = (InetSocketAddress) session.getLocalAddress();
			if (this.mdcKeys.contains(MdcInjectionFilter.MdcKey.remoteIp)) {
				context.put(MdcInjectionFilter.MdcKey.remoteIp.name(), remoteAddress.getAddress().getHostAddress());
			}

			if (this.mdcKeys.contains(MdcInjectionFilter.MdcKey.remotePort)) {
				context.put(MdcInjectionFilter.MdcKey.remotePort.name(), String.valueOf(remoteAddress.getPort()));
			}

			if (this.mdcKeys.contains(MdcInjectionFilter.MdcKey.localIp)) {
				context.put(MdcInjectionFilter.MdcKey.localIp.name(), localAddress.getAddress().getHostAddress());
			}

			if (this.mdcKeys.contains(MdcInjectionFilter.MdcKey.localPort)) {
				context.put(MdcInjectionFilter.MdcKey.localPort.name(), String.valueOf(localAddress.getPort()));
			}
		}

	}

	public static String getProperty(IoSession session, String key) {
		if (key == null) {
			throw new IllegalArgumentException("key should not be null");
		} else {
			Map context = getContext(session);
			String answer = (String) context.get(key);
			return answer != null ? answer : MDC.get(key);
		}
	}

	public static void setProperty(IoSession session, String key, String value) {
		if (key == null) {
			throw new IllegalArgumentException("key should not be null");
		} else {
			if (value == null) {
				removeProperty(session, key);
			}

			Map context = getContext(session);
			context.put(key, value);
			MDC.put(key, value);
		}
	}

	public static void removeProperty(IoSession session, String key) {
		if (key == null) {
			throw new IllegalArgumentException("key should not be null");
		} else {
			Map context = getContext(session);
			context.remove(key);
			MDC.remove(key);
		}
	}

	public static enum MdcKey {
		handlerClass, remoteAddress, localAddress, remoteIp, remotePort, localIp, localPort;
	}
}