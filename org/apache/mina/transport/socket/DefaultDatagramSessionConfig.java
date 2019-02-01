/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket;

import org.apache.mina.transport.socket.AbstractDatagramSessionConfig;

public class DefaultDatagramSessionConfig extends AbstractDatagramSessionConfig {
	private static final boolean DEFAULT_BROADCAST = false;
	private static final boolean DEFAULT_REUSE_ADDRESS = false;
	private static final int DEFAULT_RECEIVE_BUFFER_SIZE = -1;
	private static final int DEFAULT_SEND_BUFFER_SIZE = -1;
	private static final int DEFAULT_TRAFFIC_CLASS = 0;
	private boolean broadcast = false;
	private boolean reuseAddress = false;
	private int receiveBufferSize = -1;
	private int sendBufferSize = -1;
	private int trafficClass = 0;

	public boolean isBroadcast() {
		return this.broadcast;
	}

	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}

	public boolean isReuseAddress() {
		return this.reuseAddress;
	}

	public void setReuseAddress(boolean reuseAddress) {
		this.reuseAddress = reuseAddress;
	}

	public int getReceiveBufferSize() {
		return this.receiveBufferSize;
	}

	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
	}

	public int getSendBufferSize() {
		return this.sendBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

	public int getTrafficClass() {
		return this.trafficClass;
	}

	public void setTrafficClass(int trafficClass) {
		this.trafficClass = trafficClass;
	}

	protected boolean isBroadcastChanged() {
		return this.broadcast;
	}

	protected boolean isReceiveBufferSizeChanged() {
		return this.receiveBufferSize != -1;
	}

	protected boolean isReuseAddressChanged() {
		return this.reuseAddress;
	}

	protected boolean isSendBufferSizeChanged() {
		return this.sendBufferSize != -1;
	}

	protected boolean isTrafficClassChanged() {
		return this.trafficClass != 0;
	}
}