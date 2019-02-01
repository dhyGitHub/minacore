/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http.digest;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.proxy.ProxyAuthException;
import org.apache.mina.proxy.handlers.http.AbstractAuthLogicHandler;
import org.apache.mina.proxy.handlers.http.HttpProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpProxyResponse;
import org.apache.mina.proxy.handlers.http.digest.DigestUtilities;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.StringUtilities;
import org.apache.mina.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDigestAuthLogicHandler extends AbstractAuthLogicHandler {
	private static final Logger logger = LoggerFactory.getLogger(HttpDigestAuthLogicHandler.class);
	private HashMap<String, String> directives = null;
	private HttpProxyResponse response;
	private static SecureRandom rnd;

	public HttpDigestAuthLogicHandler(ProxyIoSession proxyIoSession) throws ProxyAuthException {
		super(proxyIoSession);
		((HttpProxyRequest) this.request).checkRequiredProperties(new String[] { "USER", "PWD" });
	}

	public void doHandshake(NextFilter nextFilter) throws ProxyAuthException {
		logger.debug(" doHandshake()");
		if (this.step > 0 && this.directives == null) {
			throw new ProxyAuthException("Authentication challenge not received");
		} else {
			HttpProxyRequest req = (HttpProxyRequest) this.request;
			Object headers = req.getHeaders() != null ? req.getHeaders() : new HashMap();
			if (this.step > 0) {
				logger.debug("  sending DIGEST challenge response");
				HashMap map = new HashMap();
				map.put("username", req.getProperties().get("USER"));
				StringUtilities.copyDirective(this.directives, map, "realm");
				StringUtilities.copyDirective(this.directives, map, "uri");
				StringUtilities.copyDirective(this.directives, map, "opaque");
				StringUtilities.copyDirective(this.directives, map, "nonce");
				String algorithm = StringUtilities.copyDirective(this.directives, map, "algorithm");
				if (algorithm != null && !"md5".equalsIgnoreCase(algorithm)
						&& !"md5-sess".equalsIgnoreCase(algorithm)) {
					throw new ProxyAuthException("Unknown algorithm required by server");
				}

				String qop = (String) this.directives.get("qop");
				if (qop != null) {
					StringTokenizer sb = new StringTokenizer(qop, ",");
					String addSeparator = null;

					while (sb.hasMoreTokens()) {
						String i$ = sb.nextToken();
						if ("auth".equalsIgnoreCase(addSeparator)) {
							break;
						}

						int entry = Arrays.binarySearch(DigestUtilities.SUPPORTED_QOPS, i$);
						if (entry > -1) {
							addSeparator = i$;
						}
					}

					if (addSeparator == null) {
						throw new ProxyAuthException("No supported qop option available");
					}

					map.put("qop", addSeparator);
					byte[] i$1 = new byte[8];
					rnd.nextBytes(i$1);

					try {
						String entry1 = new String(Base64.encodeBase64(i$1), this.proxyIoSession.getCharsetName());
						map.put("cnonce", entry1);
					} catch (UnsupportedEncodingException arg13) {
						throw new ProxyAuthException("Unable to encode cnonce", arg13);
					}
				}

				map.put("nc", "00000001");
				map.put("uri", req.getHttpURI());

				try {
					map.put("response",
							DigestUtilities.computeResponseValue(this.proxyIoSession.getSession(), map,
									req.getHttpVerb().toUpperCase(), (String) req.getProperties().get("PWD"),
									this.proxyIoSession.getCharsetName(), this.response.getBody()));
				} catch (Exception arg12) {
					throw new ProxyAuthException("Digest response computing failed", arg12);
				}

				StringBuilder sb1 = new StringBuilder("Digest ");
				boolean addSeparator1 = false;
				Iterator i$2 = map.entrySet().iterator();

				while (i$2.hasNext()) {
					Entry entry2 = (Entry) i$2.next();
					String key = (String) entry2.getKey();
					if (addSeparator1) {
						sb1.append(", ");
					} else {
						addSeparator1 = true;
					}

					boolean quotedValue = !"qop".equals(key) && !"nc".equals(key);
					sb1.append(key);
					if (quotedValue) {
						sb1.append("=\"").append((String) entry2.getValue()).append('\"');
					} else {
						sb1.append('=').append((String) entry2.getValue());
					}
				}

				StringUtilities.addValueToHeader((Map) headers, "Proxy-Authorization", sb1.toString(), true);
			}

			addKeepAliveHeaders((Map) headers);
			req.setHeaders((Map) headers);
			this.writeRequest(nextFilter, req);
			++this.step;
		}
	}

	public void handleResponse(HttpProxyResponse response) throws ProxyAuthException {
		this.response = response;
		if (this.step != 0) {
			throw new ProxyAuthException("Received unexpected response code (" + response.getStatusLine() + ").");
		} else if (response.getStatusCode() != 401 && response.getStatusCode() != 407) {
			throw new ProxyAuthException("Received unexpected response code (" + response.getStatusLine() + ").");
		} else {
			List values = (List) response.getHeaders().get("Proxy-Authenticate");
			String challengeResponse = null;
			Iterator e = values.iterator();

			while (e.hasNext()) {
				String s = (String) e.next();
				if (s.startsWith("Digest")) {
					challengeResponse = s;
					break;
				}
			}

			if (challengeResponse == null) {
				throw new ProxyAuthException("Server doesn\'t support digest authentication method !");
			} else {
				try {
					this.directives = StringUtilities.parseDirectives(
							challengeResponse.substring(7).getBytes(this.proxyIoSession.getCharsetName()));
				} catch (Exception arg5) {
					throw new ProxyAuthException("Parsing of server digest directives failed", arg5);
				}

				this.step = 1;
			}
		}
	}

	static {
		try {
			rnd = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException arg0) {
			throw new RuntimeException(arg0);
		}
	}
}