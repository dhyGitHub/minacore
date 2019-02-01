/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.executor;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoEvent;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.executor.IoEventSizeEstimator;

public class DefaultIoEventSizeEstimator implements IoEventSizeEstimator {
	private final ConcurrentMap<Class<?>, Integer> class2size = new ConcurrentHashMap();

	public DefaultIoEventSizeEstimator() {
		this.class2size.put(Boolean.TYPE, Integer.valueOf(4));
		this.class2size.put(Byte.TYPE, Integer.valueOf(1));
		this.class2size.put(Character.TYPE, Integer.valueOf(2));
		this.class2size.put(Integer.TYPE, Integer.valueOf(4));
		this.class2size.put(Short.TYPE, Integer.valueOf(2));
		this.class2size.put(Long.TYPE, Integer.valueOf(8));
		this.class2size.put(Float.TYPE, Integer.valueOf(4));
		this.class2size.put(Double.TYPE, Integer.valueOf(8));
		this.class2size.put(Void.TYPE, Integer.valueOf(0));
	}

	public int estimateSize(IoEvent event) {
		return this.estimateSize((Object) event) + this.estimateSize(event.getParameter());
	}

	public int estimateSize(Object message) {
		if (message == null) {
			return 8;
		} else {
			int answer = 8 + this.estimateSize(message.getClass(), (Set) null);
			if (message instanceof IoBuffer) {
				answer += ((IoBuffer) message).remaining();
			} else if (message instanceof WriteRequest) {
				answer += this.estimateSize(((WriteRequest) message).getMessage());
			} else if (message instanceof CharSequence) {
				answer += ((CharSequence) message).length() << 1;
			} else {
				Object m;
				if (message instanceof Iterable) {
					for (Iterator i$ = ((Iterable) message).iterator(); i$.hasNext(); answer += this.estimateSize(m)) {
						m = i$.next();
					}
				}
			}

			return align(answer);
		}
	}

	private int estimateSize(Class<?> clazz, Set<Class<?>> visitedClasses) {
		Integer objectSize = (Integer) this.class2size.get(clazz);
		if (objectSize != null) {
			return objectSize.intValue();
		} else {
			if (visitedClasses != null) {
				if (((Set) visitedClasses).contains(clazz)) {
					return 0;
				}
			} else {
				visitedClasses = new HashSet();
			}

			((Set) visitedClasses).add(clazz);
			int answer = 8;

			for (Class tmpAnswer = clazz; tmpAnswer != null; tmpAnswer = tmpAnswer.getSuperclass()) {
				Field[] fields = tmpAnswer.getDeclaredFields();
				Field[] arr$ = fields;
				int len$ = fields.length;

				for (int i$ = 0; i$ < len$; ++i$) {
					Field f = arr$[i$];
					if ((f.getModifiers() & 8) == 0) {
						answer += this.estimateSize(f.getType(), (Set) visitedClasses);
					}
				}
			}

			((Set) visitedClasses).remove(clazz);
			answer = align(answer);
			Integer arg10 = (Integer) this.class2size.putIfAbsent(clazz, Integer.valueOf(answer));
			if (arg10 != null) {
				answer = arg10.intValue();
			}

			return answer;
		}
	}

	private static int align(int size) {
		if (size % 8 != 0) {
			size /= 8;
			++size;
			size *= 8;
		}

		return size;
	}
}