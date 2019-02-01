/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.socks;

import java.util.Arrays;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.proxy.handlers.socks.AbstractSocksLogicHandler;
import org.apache.mina.proxy.handlers.socks.SocksProxyConstants;
import org.apache.mina.proxy.handlers.socks.SocksProxyRequest;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.ByteUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Socks4LogicHandler extends AbstractSocksLogicHandler {
	private static final Logger logger = LoggerFactory.getLogger(Socks4LogicHandler.class);

	public Socks4LogicHandler(ProxyIoSession proxyIoSession) {
		super(proxyIoSession);
	}

	public void doHandshake(NextFilter nextFilter) {
		logger.debug(" doHandshake()");
		this.writeRequest(nextFilter, this.request);
	}

	protected void writeRequest(NextFilter nextFilter, SocksProxyRequest request) {
		try {
			boolean ex = Arrays.equals(request.getIpAddress(), SocksProxyConstants.FAKE_IP);
			byte[] userID = request.getUserName().getBytes("ASCII");
			byte[] host = ex ? request.getHost().getBytes("ASCII") : null;
			int len = 9 + userID.length;
			if (ex) {
				len += host.length + 1;
			}

			IoBuffer buf = IoBuffer.allocate(len);
			buf.put(request.getProtocolVersion());
			buf.put(request.getCommandCode());
			buf.put(request.getPort());
			buf.put(request.getIpAddress());
			buf.put(userID);
			buf.put(0);
			if (ex) {
				buf.put(host);
				buf.put(0);
			}

			if (ex) {
				logger.debug("  sending SOCKS4a request");
			} else {
				logger.debug("  sending SOCKS4 request");
			}

			buf.flip();
			this.writeData(nextFilter, buf);
		} catch (Exception arg7) {
			this.closeSession("Unable to send Socks request: ", arg7);
		}

	}

	public void messageReceived(NextFilter nextFilter, IoBuffer buf) {
		try {
			if (buf.remaining() >= 8) {
				this.handleResponse(buf);
			}
		} catch (Exception arg3) {
			this.closeSession("Proxy handshake failed: ", arg3);
		}

	}

	protected void handleResponse(IoBuffer buf) throws Exception {
		byte first = buf.get(0);
		if (first != 0) {
			throw new Exception("Socks response seems to be malformed");
		} else {
			byte status = buf.get(1);
			buf.position(buf.position() + 8);
			if (status == 90) {
				this.setHandshakeComplete();
			} else {
				throw new Exception("Proxy handshake failed - Code: 0x" + ByteUtilities.asHex(new byte[] { status })
						+ " (" + SocksProxyConstants.getReplyCodeAsString(status) + ")");
			}
		}
	}
}