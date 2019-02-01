/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket;

import org.apache.mina.core.service.IoService;
import org.apache.mina.transport.socket.AbstractSocketSessionConfig;
import org.apache.mina.transport.socket.SocketAcceptor;

public class DefaultSocketSessionConfig extends AbstractSocketSessionConfig {
	private static final boolean DEFAULT_REUSE_ADDRESS = false;
	private static final int DEFAULT_TRAFFIC_CLASS = 0;
	private static final boolean DEFAULT_KEEP_ALIVE = false;
	private static final boolean DEFAULT_OOB_INLINE = false;
	private static final int DEFAULT_SO_LINGER = -1;
	private static final boolean DEFAULT_TCP_NO_DELAY = false;
	protected IoService parent;
	private boolean defaultReuseAddress;
	private boolean reuseAddress;
	private int receiveBufferSize = -1;
	private int sendBufferSize = -1;
	private int trafficClass = 0;
	private boolean keepAlive = false;
	private boolean oobInline = false;
	private int soLinger = -1;
	private boolean tcpNoDelay = false;

	public void init(IoService parent) {
		this.parent = parent;
		if (parent instanceof SocketAcceptor) {
			this.defaultReuseAddress = true;
		} else {
			this.defaultReuseAddress = false;
		}

		this.reuseAddress = this.defaultReuseAddress;
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

	public boolean isKeepAlive() {
		return this.keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public boolean isOobInline() {
		return this.oobInline;
	}

	public void setOobInline(boolean oobInline) {
		this.oobInline = oobInline;
	}

	public int getSoLinger() {
		return this.soLinger;
	}

	public void setSoLinger(int soLinger) {
		this.soLinger = soLinger;
	}

	public boolean isTcpNoDelay() {
		return this.tcpNoDelay;
	}

	public void setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}

	protected boolean isKeepAliveChanged() {
		return this.keepAlive;
	}

	protected boolean isOobInlineChanged() {
		return this.oobInline;
	}

	protected boolean isReceiveBufferSizeChanged() {
		return this.receiveBufferSize != -1;
	}

	protected boolean isReuseAddressChanged() {
		return this.reuseAddress != this.defaultReuseAddress;
	}

	protected boolean isSendBufferSizeChanged() {
		return this.sendBufferSize != -1;
	}

	protected boolean isSoLingerChanged() {
		return this.soLinger != -1;
	}

	protected boolean isTcpNoDelayChanged() {
		return this.tcpNoDelay;
	}

	protected boolean isTrafficClassChanged() {
		return this.trafficClass != 0;
	}
}