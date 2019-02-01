/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util.byteaccess;

import java.util.NoSuchElementException;
import org.apache.mina.util.byteaccess.ByteArray;

class ByteArrayList {
	private final ByteArrayList.Node header = new ByteArrayList.Node();
	private int firstByte;
	private int lastByte;

	public int lastByte() {
		return this.lastByte;
	}

	public int firstByte() {
		return this.firstByte;
	}

	public boolean isEmpty() {
		return this.header.next == this.header;
	}

	public ByteArrayList.Node getFirst() {
		return this.header.getNextNode();
	}

	public ByteArrayList.Node getLast() {
		return this.header.getPreviousNode();
	}

	public void addFirst(ByteArray ba) {
		this.addNode(new ByteArrayList.Node(ba), this.header.next);
		this.firstByte -= ba.last();
	}

	public void addLast(ByteArray ba) {
		this.addNode(new ByteArrayList.Node(ba), this.header);
		this.lastByte += ba.last();
	}

	public ByteArrayList.Node removeFirst() {
		ByteArrayList.Node node = this.header.getNextNode();
		this.firstByte += node.ba.last();
		return this.removeNode(node);
	}

	public ByteArrayList.Node removeLast() {
		ByteArrayList.Node node = this.header.getPreviousNode();
		this.lastByte -= node.ba.last();
		return this.removeNode(node);
	}

	protected void addNode(ByteArrayList.Node nodeToInsert, ByteArrayList.Node insertBeforeNode) {
		nodeToInsert.next = insertBeforeNode;
		nodeToInsert.previous = insertBeforeNode.previous;
		insertBeforeNode.previous.next = nodeToInsert;
		insertBeforeNode.previous = nodeToInsert;
	}

	protected ByteArrayList.Node removeNode(ByteArrayList.Node node) {
		node.previous.next = node.next;
		node.next.previous = node.previous;
		node.removed = true;
		return node;
	}

	public class Node {
		private ByteArrayList.Node previous;
		private ByteArrayList.Node next;
		private ByteArray ba;
		private boolean removed;

		private Node() {
			this.previous = this;
			this.next = this;
		}

		private Node(ByteArray ba) {
			if (ba == null) {
				throw new IllegalArgumentException("ByteArray must not be null.");
			} else {
				this.ba = ba;
			}
		}

		public ByteArrayList.Node getPreviousNode() {
			if (!this.hasPreviousNode()) {
				throw new NoSuchElementException();
			} else {
				return this.previous;
			}
		}

		public ByteArrayList.Node getNextNode() {
			if (!this.hasNextNode()) {
				throw new NoSuchElementException();
			} else {
				return this.next;
			}
		}

		public boolean hasPreviousNode() {
			return this.previous != ByteArrayList.this.header;
		}

		public boolean hasNextNode() {
			return this.next != ByteArrayList.this.header;
		}

		public ByteArray getByteArray() {
			return this.ba;
		}

		public boolean isRemoved() {
			return this.removed;
		}
	}
}