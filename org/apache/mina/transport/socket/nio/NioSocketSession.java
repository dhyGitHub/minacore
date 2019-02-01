/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.AbstractSocketSessionConfig;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSession;

class NioSocketSession extends NioSession {
	static final TransportMetadata METADATA = new DefaultTransportMetadata("nio", "socket", false, true,
			InetSocketAddress.class, SocketSessionConfig.class, new Class[] { IoBuffer.class, FileRegion.class });

	public NioSocketSession(IoService service, IoProcessor<NioSession> processor, SocketChannel channel) {
		super(processor, service, channel);
		this.config = new NioSocketSession.SessionConfigImpl();
		this.config.setAll(service.getSessionConfig());
	}

	private Socket getSocket() {
		return ((SocketChannel) this.channel).socket();
	}

	public TransportMetadata getTransportMetadata() {
		return METADATA;
	}

	public SocketSessionConfig getConfig() {
		return (SocketSessionConfig) this.config;
	}

	SocketChannel getChannel() {
		return (SocketChannel) this.channel;
	}

	public InetSocketAddress getRemoteAddress() {
		if (this.channel == null) {
			return null;
		} else {
			Socket socket = this.getSocket();
			return socket == null ? null : (InetSocketAddress) socket.getRemoteSocketAddress();
		}
	}

	public InetSocketAddress getLocalAddress() {
		if (this.channel == null) {
			return null;
		} else {
			Socket socket = this.getSocket();
			return socket == null ? null : (InetSocketAddress) socket.getLocalSocketAddress();
		}
	}

	protected void destroy(NioSession session) throws IOException {
		ByteChannel ch = session.getChannel();
		SelectionKey key = session.getSelectionKey();
		if (key != null) {
			key.cancel();
		}

		ch.close();
	}

	public InetSocketAddress getServiceAddress() {
		return (InetSocketAddress) super.getServiceAddress();
	}

	public final boolean isSecured() {
		IoFilterChain chain = this.getFilterChain();
		IoFilter sslFilter = chain.get(SslFilter.class);
		return sslFilter != null ? ((SslFilter) sslFilter).isSslStarted(this) : false;
	}

	private class SessionConfigImpl extends AbstractSocketSessionConfig {
		private SessionConfigImpl() {
		}

		public boolean isKeepAlive() {
			try {
				return NioSocketSession.this.getSocket().getKeepAlive();
			} catch (SocketException arg1) {
				throw new RuntimeIoException(arg1);
			}
		}

		public void setKeepAlive(boolean on) {
			try {
				NioSocketSession.this.getSocket().setKeepAlive(on);
			} catch (SocketException arg2) {
				throw new RuntimeIoException(arg2);
			}
		}

		public boolean isOobInline() {
			try {
				return NioSocketSession.this.getSocket().getOOBInline();
			} catch (SocketException arg1) {
				throw new RuntimeIoException(arg1);
			}
		}

		public void setOobInline(boolean on) {
			try {
				NioSocketSession.this.getSocket().setOOBInline(on);
			} catch (SocketException arg2) {
				throw new RuntimeIoException(arg2);
			}
		}

		public boolean isReuseAddress() {
			try {
				return NioSocketSession.this.getSocket().getReuseAddress();
			} catch (SocketException arg1) {
				throw new RuntimeIoException(arg1);
			}
		}

		public void setReuseAddress(boolean on) {
			try {
				NioSocketSession.this.getSocket().setReuseAddress(on);
			} catch (SocketException arg2) {
				throw new RuntimeIoException(arg2);
			}
		}

		public int getSoLinger() {
			try {
				return NioSocketSession.this.getSocket().getSoLinger();
			} catch (SocketException arg1) {
				throw new RuntimeIoException(arg1);
			}
		}

		public void setSoLinger(int linger) {
			try {
				if (linger < 0) {
					NioSocketSession.this.getSocket().setSoLinger(false, 0);
				} else {
					NioSocketSession.this.getSocket().setSoLinger(true, linger);
				}

			} catch (SocketException arg2) {
				throw new RuntimeIoException(arg2);
			}
		}

		public boolean isTcpNoDelay() {
			if (!NioSocketSession.this.isConnected()) {
				return false;
			} else {
				try {
					return NioSocketSession.this.getSocket().getTcpNoDelay();
				} catch (SocketException arg1) {
					throw new RuntimeIoException(arg1);
				}
			}
		}

		public void setTcpNoDelay(boolean on) {
			try {
				NioSocketSession.this.getSocket().setTcpNoDelay(on);
			} catch (SocketException arg2) {
				throw new RuntimeIoException(arg2);
			}
		}

		public int getTrafficClass() {
			try {
				return NioSocketSession.this.getSocket().getTrafficClass();
			} catch (SocketException arg1) {
				throw new RuntimeIoException(arg1);
			}
		}

		public void setTrafficClass(int tc) {
			try {
				NioSocketSession.this.getSocket().setTrafficClass(tc);
			} catch (SocketException arg2) {
				throw new RuntimeIoException(arg2);
			}
		}

		public int getSendBufferSize() {
			try {
				return NioSocketSession.this.getSocket().getSendBufferSize();
			} catch (SocketException arg1) {
				throw new RuntimeIoException(arg1);
			}
		}

		public void setSendBufferSize(int size) {
			try {
				NioSocketSession.this.getSocket().setSendBufferSize(size);
			} catch (SocketException arg2) {
				throw new RuntimeIoException(arg2);
			}
		}

		public int getReceiveBufferSize() {
			try {
				return NioSocketSession.this.getSocket().getReceiveBufferSize();
			} catch (SocketException arg1) {
				throw new RuntimeIoException(arg1);
			}
		}

		public void setReceiveBufferSize(int size) {
			try {
				NioSocketSession.this.getSocket().setReceiveBufferSize(size);
			} catch (SocketException arg2) {
				throw new RuntimeIoException(arg2);
			}
		}
	}
}