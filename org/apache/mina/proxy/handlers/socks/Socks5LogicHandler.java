/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.socks;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.proxy.handlers.socks.AbstractSocksLogicHandler;
import org.apache.mina.proxy.handlers.socks.SocksProxyConstants;
import org.apache.mina.proxy.handlers.socks.SocksProxyRequest;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.ByteUtilities;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Socks5LogicHandler extends AbstractSocksLogicHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(Socks5LogicHandler.class);
	private static final String SELECTED_AUTH_METHOD = Socks5LogicHandler.class.getName() + ".SelectedAuthMethod";
	private static final String HANDSHAKE_STEP = Socks5LogicHandler.class.getName() + ".HandshakeStep";
	private static final String GSS_CONTEXT = Socks5LogicHandler.class.getName() + ".GSSContext";
	private static final String GSS_TOKEN = Socks5LogicHandler.class.getName() + ".GSSToken";

	public Socks5LogicHandler(ProxyIoSession proxyIoSession) {
		super(proxyIoSession);
		this.getSession().setAttribute(HANDSHAKE_STEP, Integer.valueOf(0));
	}

	public synchronized void doHandshake(NextFilter nextFilter) {
		LOGGER.debug(" doHandshake()");
		this.writeRequest(nextFilter, this.request,
				((Integer) this.getSession().getAttribute(HANDSHAKE_STEP)).intValue());
	}

	private IoBuffer encodeInitialGreetingPacket(SocksProxyRequest request) {
		byte nbMethods = (byte) SocksProxyConstants.SUPPORTED_AUTH_METHODS.length;
		IoBuffer buf = IoBuffer.allocate(2 + nbMethods);
		buf.put(request.getProtocolVersion());
		buf.put(nbMethods);
		buf.put(SocksProxyConstants.SUPPORTED_AUTH_METHODS);
		return buf;
	}

	private IoBuffer encodeProxyRequestPacket(SocksProxyRequest request) throws UnsupportedEncodingException {
		int len = 6;
		InetSocketAddress adr = request.getEndpointAddress();
		byte addressType = 0;
		byte[] host = null;
		if (adr != null && !adr.isUnresolved()) {
			if (adr.getAddress() instanceof Inet6Address) {
				len += 16;
				addressType = 4;
			} else if (adr.getAddress() instanceof Inet4Address) {
				len += 4;
				addressType = 1;
			}
		} else {
			host = request.getHost() != null ? request.getHost().getBytes("ASCII") : null;
			if (host == null) {
				throw new IllegalArgumentException("SocksProxyRequest object has no suitable endpoint information");
			}

			len += 1 + host.length;
			addressType = 3;
		}

		IoBuffer buf = IoBuffer.allocate(len);
		buf.put(request.getProtocolVersion());
		buf.put(request.getCommandCode());
		buf.put(0);
		buf.put(addressType);
		if (host == null) {
			buf.put(request.getIpAddress());
		} else {
			buf.put((byte) host.length);
			buf.put(host);
		}

		buf.put(request.getPort());
		return buf;
	}

	private IoBuffer encodeAuthenticationPacket(SocksProxyRequest request)
			throws UnsupportedEncodingException, GSSException {
		byte method = ((Byte) this.getSession().getAttribute(SELECTED_AUTH_METHOD)).byteValue();
		switch (method) {
		case 0:
			this.getSession().setAttribute(HANDSHAKE_STEP, Integer.valueOf(2));
		default:
			return null;
		case 1:
			return this.encodeGSSAPIAuthenticationPacket(request);
		case 2:
			byte[] user = request.getUserName().getBytes("ASCII");
			byte[] pwd = request.getPassword().getBytes("ASCII");
			IoBuffer buf = IoBuffer.allocate(3 + user.length + pwd.length);
			buf.put(1);
			buf.put((byte) user.length);
			buf.put(user);
			buf.put((byte) pwd.length);
			buf.put(pwd);
			return buf;
		}
	}

	private IoBuffer encodeGSSAPIAuthenticationPacket(SocksProxyRequest request) throws GSSException {
		GSSContext ctx = (GSSContext) this.getSession().getAttribute(GSS_CONTEXT);
		if (ctx == null) {
			GSSManager token = GSSManager.getInstance();
			GSSName buf = token.createName(request.getServiceKerberosName(), (Oid) null);
			Oid krb5OID = new Oid("1.2.840.113554.1.2.2");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Available mechs:");
				Oid[] arr$ = token.getMechs();
				int len$ = arr$.length;

				for (int i$ = 0; i$ < len$; ++i$) {
					Oid o = arr$[i$];
					if (o.equals(krb5OID)) {
						LOGGER.debug("Found Kerberos V OID available");
					}

					LOGGER.debug("{} with oid = {}", token.getNamesForMech(o), o);
				}
			}

			ctx = token.createContext(buf, krb5OID, (GSSCredential) null, 0);
			ctx.requestMutualAuth(true);
			ctx.requestConf(false);
			ctx.requestInteg(false);
			this.getSession().setAttribute(GSS_CONTEXT, ctx);
		}

		byte[] arg9 = (byte[]) ((byte[]) this.getSession().getAttribute(GSS_TOKEN));
		if (arg9 != null) {
			LOGGER.debug("  Received Token[{}] = {}", Integer.valueOf(arg9.length), ByteUtilities.asHex(arg9));
		}

		IoBuffer arg10 = null;
		if (!ctx.isEstablished()) {
			if (arg9 == null) {
				arg9 = new byte[32];
			}

			arg9 = ctx.initSecContext(arg9, 0, arg9.length);
			if (arg9 != null) {
				LOGGER.debug("  Sending Token[{}] = {}", Integer.valueOf(arg9.length), ByteUtilities.asHex(arg9));
				this.getSession().setAttribute(GSS_TOKEN, arg9);
				arg10 = IoBuffer.allocate(4 + arg9.length);
				arg10.put(new byte[] { 1, 1 });
				arg10.put(ByteUtilities.intToNetworkByteOrder(arg9.length, 2));
				arg10.put(arg9);
			}
		}

		return arg10;
	}

	private void writeRequest(NextFilter nextFilter, SocksProxyRequest request, int step) {
		try {
			IoBuffer ex = null;
			if (step == 0) {
				ex = this.encodeInitialGreetingPacket(request);
			} else if (step == 1) {
				ex = this.encodeAuthenticationPacket(request);
				if (ex == null) {
					step = 2;
				}
			}

			if (step == 2) {
				ex = this.encodeProxyRequestPacket(request);
			}

			ex.flip();
			this.writeData(nextFilter, ex);
		} catch (Exception arg4) {
			this.closeSession("Unable to send Socks request: ", arg4);
		}

	}

	public synchronized void messageReceived(NextFilter nextFilter, IoBuffer buf) {
		try {
			int ex = ((Integer) this.getSession().getAttribute(HANDSHAKE_STEP)).intValue();
			if (ex == 0 && buf.get(0) != 5) {
				throw new IllegalStateException("Wrong socks version running on server");
			}

			if ((ex == 0 || ex == 1) && buf.remaining() >= 2) {
				this.handleResponse(nextFilter, buf, ex);
			} else if (ex == 2 && buf.remaining() >= 5) {
				this.handleResponse(nextFilter, buf, ex);
			}
		} catch (Exception arg3) {
			this.closeSession("Proxy handshake failed: ", arg3);
		}

	}

	protected void handleResponse(NextFilter nextFilter, IoBuffer buf, int step) throws Exception {
		byte len = 2;
		byte isAuthenticating;
		byte arg11;
		if (step == 0) {
			isAuthenticating = buf.get(1);
			if (isAuthenticating == -1) {
				throw new IllegalStateException(
						"No acceptable authentication method to use with the socks proxy server");
			}

			this.getSession().setAttribute(SELECTED_AUTH_METHOD, Byte.valueOf(isAuthenticating));
		} else if (step == 1) {
			isAuthenticating = ((Byte) this.getSession().getAttribute(SELECTED_AUTH_METHOD)).byteValue();
			if (isAuthenticating == 1) {
				int method = buf.position();
				if (buf.get(0) != 1) {
					throw new IllegalStateException("Authentication failed");
				}

				if ((buf.get(1) & 255) == 255) {
					throw new IllegalStateException("Authentication failed: GSS API Security Context Failure");
				}

				if (buf.remaining() < 2) {
					buf.position(method);
					return;
				}

				byte[] ctx = new byte[2];
				buf.get(ctx);
				int s = ByteUtilities.makeIntFromByte2(ctx);
				if (buf.remaining() < s) {
					return;
				}

				byte[] token = new byte[s];
				buf.get(token);
				this.getSession().setAttribute(GSS_TOKEN, token);
				len = 0;
			} else if (buf.get(1) != 0) {
				throw new IllegalStateException("Authentication failed");
			}
		} else if (step == 2) {
			isAuthenticating = buf.get(3);
			len = 6;
			int arg9;
			if (isAuthenticating == 4) {
				arg9 = len + 16;
			} else if (isAuthenticating == 1) {
				arg9 = len + 4;
			} else {
				if (isAuthenticating != 3) {
					throw new IllegalStateException("Unknwon address type");
				}

				arg9 = len + 1 + buf.get(4);
			}

			if (buf.remaining() >= arg9) {
				arg11 = buf.get(1);
				LOGGER.debug("  response status: {}", SocksProxyConstants.getReplyCodeAsString(arg11));
				if (arg11 == 0) {
					buf.position(buf.position() + arg9);
					this.setHandshakeComplete();
					return;
				}

				throw new Exception("Proxy handshake failed - Code: 0x" + ByteUtilities.asHex(new byte[] { arg11 }));
			}

			return;
		}

		if (len > 0) {
			buf.position(buf.position() + len);
		}

		boolean arg10 = false;
		if (step == 1) {
			arg11 = ((Byte) this.getSession().getAttribute(SELECTED_AUTH_METHOD)).byteValue();
			if (arg11 == 1) {
				GSSContext arg12 = (GSSContext) this.getSession().getAttribute(GSS_CONTEXT);
				if (arg12 == null || !arg12.isEstablished()) {
					arg10 = true;
				}
			}
		}

		if (!arg10) {
			IoSession arg9999 = this.getSession();
			++step;
			arg9999.setAttribute(HANDSHAKE_STEP, Integer.valueOf(step));
		}

		this.doHandshake(nextFilter);
	}

	protected void closeSession(String message) {
		GSSContext ctx = (GSSContext) this.getSession().getAttribute(GSS_CONTEXT);
		if (ctx != null) {
			try {
				ctx.dispose();
			} catch (GSSException arg3) {
				arg3.printStackTrace();
				super.closeSession(message, arg3);
				return;
			}
		}

		super.closeSession(message);
	}
}