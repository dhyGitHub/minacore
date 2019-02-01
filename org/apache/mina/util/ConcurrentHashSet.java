/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.mina.util.MapBackedSet;

public class ConcurrentHashSet<E> extends MapBackedSet<E> {
	private static final long serialVersionUID = 8518578988740277828L;

	public ConcurrentHashSet() {
		super(new ConcurrentHashMap());
	}

	public ConcurrentHashSet(Collection<E> c) {
		super(new ConcurrentHashMap(), c);
	}

	public boolean add(E o) {
		Boolean answer = (Boolean) ((ConcurrentMap) this.map).putIfAbsent(o, Boolean.TRUE);
		return answer == null;
	}
}