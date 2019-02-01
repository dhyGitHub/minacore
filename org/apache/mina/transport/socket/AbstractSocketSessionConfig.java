/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket;

import org.apache.mina.core.session.AbstractIoSessionConfig;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.transport.socket.SocketSessionConfig;

public abstract class AbstractSocketSessionConfig extends AbstractIoSessionConfig implements SocketSessionConfig {
	public void setAll(IoSessionConfig config) {
		super.setAll(config);
		if (config instanceof SocketSessionConfig) {
			if (config instanceof AbstractSocketSessionConfig) {
				AbstractSocketSessionConfig cfg = (AbstractSocketSessionConfig) config;
				if (cfg.isKeepAliveChanged()) {
					this.setKeepAlive(cfg.isKeepAlive());
				}

				if (cfg.isOobInlineChanged()) {
					this.setOobInline(cfg.isOobInline());
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

				if (cfg.isSoLingerChanged()) {
					this.setSoLinger(cfg.getSoLinger());
				}

				if (cfg.isTcpNoDelayChanged()) {
					this.setTcpNoDelay(cfg.isTcpNoDelay());
				}

				if (cfg.isTrafficClassChanged() && this.getTrafficClass() != cfg.getTrafficClass()) {
					this.setTrafficClass(cfg.getTrafficClass());
				}
			} else {
				SocketSessionConfig cfg1 = (SocketSessionConfig) config;
				this.setKeepAlive(cfg1.isKeepAlive());
				this.setOobInline(cfg1.isOobInline());
				this.setReceiveBufferSize(cfg1.getReceiveBufferSize());
				this.setReuseAddress(cfg1.isReuseAddress());
				this.setSendBufferSize(cfg1.getSendBufferSize());
				this.setSoLinger(cfg1.getSoLinger());
				this.setTcpNoDelay(cfg1.isTcpNoDelay());
				if (this.getTrafficClass() != cfg1.getTrafficClass()) {
					this.setTrafficClass(cfg1.getTrafficClass());
				}
			}

		}
	}

	protected boolean isKeepAliveChanged() {
		return true;
	}

	protected boolean isOobInlineChanged() {
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

	protected boolean isSoLingerChanged() {
		return true;
	}

	protected boolean isTcpNoDelayChanged() {
		return true;
	}

	protected boolean isTrafficClassChanged() {
		return true;
	}
}