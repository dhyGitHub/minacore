/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket.nio;

import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.transport.socket.AbstractDatagramSessionConfig;

class NioDatagramSessionConfig extends AbstractDatagramSessionConfig {
	private final DatagramChannel channel;

	NioDatagramSessionConfig(DatagramChannel channel) {
		this.channel = channel;
	}

	public int getReceiveBufferSize() {
		try {
			return this.channel.socket().getReceiveBufferSize();
		} catch (SocketException arg1) {
			throw new RuntimeIoException(arg1);
		}
	}

	public void setReceiveBufferSize(int receiveBufferSize) {
		try {
			this.channel.socket().setReceiveBufferSize(receiveBufferSize);
		} catch (SocketException arg2) {
			throw new RuntimeIoException(arg2);
		}
	}

	public boolean isBroadcast() {
		try {
			return this.channel.socket().getBroadcast();
		} catch (SocketException arg1) {
			throw new RuntimeIoException(arg1);
		}
	}

	public void setBroadcast(boolean broadcast) {
		try {
			this.channel.socket().setBroadcast(broadcast);
		} catch (SocketException arg2) {
			throw new RuntimeIoException(arg2);
		}
	}

	public int getSendBufferSize() {
		try {
			return this.channel.socket().getSendBufferSize();
		} catch (SocketException arg1) {
			throw new RuntimeIoException(arg1);
		}
	}

	public void setSendBufferSize(int sendBufferSize) {
		try {
			this.channel.socket().setSendBufferSize(sendBufferSize);
		} catch (SocketException arg2) {
			throw new RuntimeIoException(arg2);
		}
	}

	public boolean isReuseAddress() {
		try {
			return this.channel.socket().getReuseAddress();
		} catch (SocketException arg1) {
			throw new RuntimeIoException(arg1);
		}
	}

	public void setReuseAddress(boolean reuseAddress) {
		try {
			this.channel.socket().setReuseAddress(reuseAddress);
		} catch (SocketException arg2) {
			throw new RuntimeIoException(arg2);
		}
	}

	public int getTrafficClass() {
		try {
			return this.channel.socket().getTrafficClass();
		} catch (SocketException arg1) {
			throw new RuntimeIoException(arg1);
		}
	}

	public void setTrafficClass(int trafficClass) {
		try {
			this.channel.socket().setTrafficClass(trafficClass);
		} catch (SocketException arg2) {
			throw new RuntimeIoException(arg2);
		}
	}
}