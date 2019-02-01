/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public class SynchronizedQueue<E> implements Queue<E>, Serializable {
	private static final long serialVersionUID = -1439242290701194806L;
	private final Queue<E> q;

	public SynchronizedQueue(Queue<E> q) {
		this.q = q;
	}

	public synchronized boolean add(E e) {
		return this.q.add(e);
	}

	public synchronized E element() {
		return this.q.element();
	}

	public synchronized boolean offer(E e) {
		return this.q.offer(e);
	}

	public synchronized E peek() {
		return this.q.peek();
	}

	public synchronized E poll() {
		return this.q.poll();
	}

	public synchronized E remove() {
		return this.q.remove();
	}

	public synchronized boolean addAll(Collection<? extends E> c) {
		return this.q.addAll(c);
	}

	public synchronized void clear() {
		this.q.clear();
	}

	public synchronized boolean contains(Object o) {
		return this.q.contains(o);
	}

	public synchronized boolean containsAll(Collection<?> c) {
		return this.q.containsAll(c);
	}

	public synchronized boolean isEmpty() {
		return this.q.isEmpty();
	}

	public synchronized Iterator<E> iterator() {
		return this.q.iterator();
	}

	public synchronized boolean remove(Object o) {
		return this.q.remove(o);
	}

	public synchronized boolean removeAll(Collection<?> c) {
		return this.q.removeAll(c);
	}

	public synchronized boolean retainAll(Collection<?> c) {
		return this.q.retainAll(c);
	}

	public synchronized int size() {
		return this.q.size();
	}

	public synchronized Object[] toArray() {
		return this.q.toArray();
	}

	public synchronized <T> T[] toArray(T[] a) {
		return this.q.toArray(a);
	}

	public synchronized boolean equals(Object obj) {
		return this.q.equals(obj);
	}

	public synchronized int hashCode() {
		return this.q.hashCode();
	}

	public synchronized String toString() {
		return this.q.toString();
	}
}