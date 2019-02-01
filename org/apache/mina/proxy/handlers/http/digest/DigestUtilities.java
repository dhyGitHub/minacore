/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http.digest;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import javax.security.sasl.AuthenticationException;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.apache.mina.proxy.utils.ByteUtilities;
import org.apache.mina.proxy.utils.StringUtilities;

public class DigestUtilities {
	public static final String SESSION_HA1 = DigestUtilities.class + ".SessionHA1";
	private static MessageDigest md5;
	public static final String[] SUPPORTED_QOPS;

	public static String computeResponseValue(IoSession session, HashMap<String, String> map, String method, String pwd,
			String charsetName, String body) throws AuthenticationException, UnsupportedEncodingException {
		boolean isMD5Sess = "md5-sess".equalsIgnoreCase(StringUtilities.getDirectiveValue(map, "algorithm", false));
		byte[] hA1;
		StringBuilder sb;
		String qop;
		byte[] hA2;
		MessageDigest hFinal;
		if (isMD5Sess && session.getAttribute(SESSION_HA1) != null) {
			hA1 = (byte[]) ((byte[]) session.getAttribute(SESSION_HA1));
		} else {
			sb = new StringBuilder();
			sb.append(StringUtilities.stringTo8859_1(StringUtilities.getDirectiveValue(map, "username", true)))
					.append(':');
			qop = StringUtilities.stringTo8859_1(StringUtilities.getDirectiveValue(map, "realm", false));
			if (qop != null) {
				sb.append(qop);
			}

			sb.append(':').append(pwd);
			if (isMD5Sess) {
				hFinal = md5;
				synchronized (md5) {
					md5.reset();
					hA2 = md5.digest(sb.toString().getBytes(charsetName));
				}

				sb = new StringBuilder();
				sb.append(ByteUtilities.asHex(hA2));
				sb.append(':')
						.append(StringUtilities.stringTo8859_1(StringUtilities.getDirectiveValue(map, "nonce", true)));
				sb.append(':')
						.append(StringUtilities.stringTo8859_1(StringUtilities.getDirectiveValue(map, "cnonce", true)));
				hFinal = md5;
				synchronized (md5) {
					md5.reset();
					hA1 = md5.digest(sb.toString().getBytes(charsetName));
				}

				session.setAttribute(SESSION_HA1, hA1);
			} else {
				MessageDigest hA21 = md5;
				synchronized (md5) {
					md5.reset();
					hA1 = md5.digest(sb.toString().getBytes(charsetName));
				}
			}
		}

		sb = new StringBuilder(method);
		sb.append(':');
		sb.append(StringUtilities.getDirectiveValue(map, "uri", false));
		qop = StringUtilities.getDirectiveValue(map, "qop", false);
		MessageDigest arg11;
		byte[] hFinal1;
		if ("auth-int".equalsIgnoreCase(qop)) {
			ProxyIoSession hA22 = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
			arg11 = md5;
			synchronized (md5) {
				md5.reset();
				hFinal1 = md5.digest(body.getBytes(hA22.getCharsetName()));
			}

			sb.append(':').append(hFinal1);
		}

		hFinal = md5;
		synchronized (md5) {
			md5.reset();
			hA2 = md5.digest(sb.toString().getBytes(charsetName));
		}

		sb = new StringBuilder();
		sb.append(ByteUtilities.asHex(hA1));
		sb.append(':').append(StringUtilities.getDirectiveValue(map, "nonce", true));
		sb.append(":00000001:");
		sb.append(StringUtilities.getDirectiveValue(map, "cnonce", true));
		sb.append(':').append(qop).append(':');
		sb.append(ByteUtilities.asHex(hA2));
		arg11 = md5;
		synchronized (md5) {
			md5.reset();
			hFinal1 = md5.digest(sb.toString().getBytes(charsetName));
		}

		return ByteUtilities.asHex(hFinal1);
	}

	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException arg0) {
			throw new RuntimeException(arg0);
		}

		SUPPORTED_QOPS = new String[] { "auth", "auth-int" };
	}
}