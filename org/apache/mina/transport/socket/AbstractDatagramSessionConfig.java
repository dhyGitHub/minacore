/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket;

import org.apache.mina.core.session.AbstractIoSessionConfig;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.transport.socket.DatagramSessionConfig;

public abstract class AbstractDatagramSessionConfig extends AbstractIoSessionConfig implements DatagramSessionConfig {
	private boolean closeOnPortUnreachable = true;

	public void setAll(IoSessionConfig config) {
		super.setAll(config);
		if (config instanceof DatagramSessionConfig) {
			if (config instanceof AbstractDatagramSessionConfig) {
				AbstractDatagramSessionConfig cfg = (AbstractDatagramSessionConfig) config;
				if (cfg.isBroadcastChanged()) {
					this.setBroadcast(cfg.isBroadcast());
				}

				if (cfg.isReceiveBufferSizeChanged()) {
					this.setReceiveBufferSize(cfg.getReceiveBufferSize());
				}

				if (cfg.isReuseAddressChanged()) {
					this.setReuseAddress(cfg.isReuseAddress());
				}

				if (cfg.isSendBufferSizeChanged()) {
					this.setSendBufferSize(cfg.getSendBufferSize());
				}

				if (cfg.isTrafficClassChanged() && this.getTrafficClass() != cfg.getTrafficClass()) {
					this.setTrafficClass(cfg.getTrafficClass());
				}
			} else {
				DatagramSessionConfig cfg1 = (DatagramSessionConfig) config;
				this.setBroadcast(cfg1.isBroadcast());
				this.setReceiveBufferSize(cfg1.getReceiveBufferSize());
				this.setReuseAddress(cfg1.isReuseAddress());
				this.setSendBufferSize(cfg1.getSendBufferSize());
				if (this.getTrafficClass() != cfg1.getTrafficClass()) {
					this.setTrafficClass(cfg1.getTrafficClass());
				}
			}

		}
	}

	protected boolean isBroadcastChanged() {
		return true;
	}

	protected boolean isReceiveBufferSizeChanged() {
		return true;
	}

	protected boolean isReuseAddressChanged() {
		return true;
	}

	protected boolean isSendBufferSizeChanged() {
		return true;
	}

	protected boolean isTrafficClassChanged() {
		return true;
	}

	public boolean isCloseOnPortUnreachable() {
		return this.closeOnPortUnreachable;
	}

	public void setCloseOnPortUnreachable(boolean closeOnPortUnreachable) {
		this.closeOnPortUnreachable = closeOnPortUnreachable;
	}
}