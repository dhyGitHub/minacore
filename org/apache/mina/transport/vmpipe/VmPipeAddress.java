/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.vmpipe;

import java.net.SocketAddress;

public class VmPipeAddress extends SocketAddress implements Comparable<VmPipeAddress> {
	private static final long serialVersionUID = 3257844376976830515L;
	private final int port;

	public VmPipeAddress(int port) {
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}

	public int hashCode() {
		return this.port;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof VmPipeAddress) {
			VmPipeAddress that = (VmPipeAddress) o;
			return this.port == that.port;
		} else {
			return false;
		}
	}

	public int compareTo(VmPipeAddress o) {
		return this.port - o.port;
	}

	public String toString() {
		return this.port >= 0 ? "vm:server:" + this.port : "vm:client:" + -this.port;
	}
}