/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http.ntlm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.HttpProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpProxyResponse;
import org.apache.mina.proxy.handlers.http.ntlm.NTLMUtilities;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.StringUtilities;
import org.apache.mina.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpNTLMAuthLogicHandler extends AbstractAuthLogicHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpNTLMAuthLogicHandler.class);
	private byte[] challengePacket = null;

	public HttpNTLMAuthLogicHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
		super(proxyIoSession);
		((HttpProxyRequest) this.request)
				.checkRequiredProperties(new String[] { "USER", "PWD", "DOMAIN", "WORKSTATION" });
	}

	public void doHandshake(NextFilter nextFilter) throws ProxyAuthException {
		LOGGER.debug(" doHandshake()");
		if (this.step > 0 && this.challengePacket == null) {
			throw new IllegalStateException("NTLM Challenge packet not received");
		} else {
			HttpProxyRequest req = (HttpProxyRequest) this.request;
			Object headers = req.getHeaders() != null ? req.getHeaders() : new HashMap();
			String domain = (String) req.getProperties().get("DOMAIN");
			String workstation = (String) req.getProperties().get("WORKSTATION");
			byte[] negotiationPacket;
			if (this.step > 0) {
				LOGGER.debug("  sending NTLM challenge response");
				negotiationPacket = NTLMUtilities.extractChallengeFromType2Message(this.challengePacket);
				int serverFlags = NTLMUtilities.extractFlagsFromType2Message(this.challengePacket);
				String username = (String) req.getProperties().get("USER");
				String password = (String) req.getProperties().get("PWD");
				byte[] authenticationPacket = NTLMUtilities.createType3Message(username, password, negotiationPacket,
						domain, workstation, Integer.valueOf(serverFlags), (byte[]) null);
				StringUtilities.addValueToHeader((Map) headers, "Proxy-Authorization",
						"NTLM " + new String(Base64.encodeBase64(authenticationPacket)), true);
			} else {
				LOGGER.debug("  sending NTLM negotiation packet");
				negotiationPacket = NTLMUtilities.createType1Message(workstation, domain, (Integer) null,
						(byte[]) null);
				StringUtilities.addValueToHeader((Map) headers, "Proxy-Authorization",
						"NTLM " + new String(Base64.encodeBase64(negotiationPacket)), true);
			}

			addKeepAliveHeaders((Map) headers);
			req.setHeaders((Map) headers);
			this.writeRequest(nextFilter, req);
			++this.step;
		}
	}

	private String getNTLMHeader(HttpProxyResponse response) {
		List values = (List) response.getHeaders().get("Proxy-Authenticate");
		Iterator i$ = values.iterator();

		String s;
		do {
			if (!i$.hasNext()) {
				return null;
			}

			s = (String) i$.next();
		} while (!s.startsWith("NTLM"));

		return s;
	}

	public void handleResponse(HttpProxyResponse response) throws ProxyAuthException {
		String challengeResponse;
		if (this.step == 0) {
			challengeResponse = this.getNTLMHeader(response);
			this.step = 1;
			if (challengeResponse == null || challengeResponse.length() < 5) {
				return;
			}
		}

		if (this.step == 1) {
			challengeResponse = this.getNTLMHeader(response);
			if (challengeResponse != null && challengeResponse.length() >= 5) {
				try {
					this.challengePacket = Base64.decodeBase64(
							challengeResponse.substring(5).getBytes(this.proxyIoSession.getCharsetName()));
				} catch (IOException arg3) {
					throw new ProxyAuthException("Unable to decode the base64 encoded NTLM challenge", arg3);
				}

				this.step = 2;
			} else {
				throw new ProxyAuthException("Unexpected error while reading server challenge !");
			}
		} else {
			throw new ProxyAuthException("Received unexpected response code (" + response.getStatusLine() + ").");
		}
	}
}