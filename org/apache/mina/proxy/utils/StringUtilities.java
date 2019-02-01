/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.sasl.AuthenticationException;
import javax.security.sasl.SaslException;

public class StringUtilities {
	public static String getDirectiveValue(HashMap<String, String> directivesMap, String directive, boolean mandatory)
			throws AuthenticationException {
		String value = (String) directivesMap.get(directive);
		if (value == null) {
			if (mandatory) {
				throw new AuthenticationException("\"" + directive + "\" mandatory directive is missing");
			} else {
				return "";
			}
		} else {
			return value;
		}
	}

	public static void copyDirective(HashMap<String, String> directives, StringBuilder sb, String directive) {
		String directiveValue = (String) directives.get(directive);
		if (directiveValue != null) {
			sb.append(directive).append(" = \"").append(directiveValue).append("\", ");
		}

	}

	public static String copyDirective(HashMap<String, String> src, HashMap<String, String> dst, String directive) {
		String directiveValue = (String) src.get(directive);
		if (directiveValue != null) {
			dst.put(directive, directiveValue);
		}

		return directiveValue;
	}

	public static HashMap<String, String> parseDirectives(byte[] buf) throws SaslException {
		HashMap map = new HashMap();
		boolean gettingKey = true;
		boolean gettingQuotedValue = false;
		boolean expectSeparator = false;
		ByteArrayOutputStream key = new ByteArrayOutputStream(10);
		ByteArrayOutputStream value = new ByteArrayOutputStream(10);
		int i = skipLws(buf, 0);

		while (true) {
			while (i < buf.length) {
				byte bch = buf[i];
				if (gettingKey) {
					if (bch == 44) {
						if (key.size() != 0) {
							throw new SaslException("Directive key contains a \',\':" + key);
						}

						i = skipLws(buf, i + 1);
					} else if (bch == 61) {
						if (key.size() == 0) {
							throw new SaslException("Empty directive key");
						}

						gettingKey = false;
						i = skipLws(buf, i + 1);
						if (i >= buf.length) {
							throw new SaslException("Valueless directive found: " + key.toString());
						}

						if (buf[i] == 34) {
							gettingQuotedValue = true;
							++i;
						}
					} else if (isLws(bch)) {
						i = skipLws(buf, i + 1);
						if (i >= buf.length) {
							throw new SaslException("\'=\' expected after key: " + key.toString());
						}

						if (buf[i] != 61) {
							throw new SaslException("\'=\' expected after key: " + key.toString());
						}
					} else {
						key.write(bch);
						++i;
					}
				} else if (gettingQuotedValue) {
					if (bch == 92) {
						++i;
						if (i >= buf.length) {
							throw new SaslException("Unmatched quote found for directive: " + key.toString()
									+ " with value: " + value.toString());
						}

						value.write(buf[i]);
						++i;
					} else if (bch == 34) {
						++i;
						gettingQuotedValue = false;
						expectSeparator = true;
					} else {
						value.write(bch);
						++i;
					}
				} else if (!isLws(bch) && bch != 44) {
					if (expectSeparator) {
						throw new SaslException("Expecting comma or linear whitespace after quoted string: \""
								+ value.toString() + "\"");
					}

					value.write(bch);
					++i;
				} else {
					extractDirective(map, key.toString(), value.toString());
					key.reset();
					value.reset();
					gettingKey = true;
					expectSeparator = false;
					gettingQuotedValue = false;
					i = skipLws(buf, i + 1);
				}
			}

			if (gettingQuotedValue) {
				throw new SaslException(
						"Unmatched quote found for directive: " + key.toString() + " with value: " + value.toString());
			}

			if (key.size() > 0) {
				extractDirective(map, key.toString(), value.toString());
			}

			return map;
		}
	}

	private static void extractDirective(HashMap<String, String> map, String key, String value) throws SaslException {
		if (map.get(key) != null) {
			throw new SaslException("Peer sent more than one " + key + " directive");
		} else {
			map.put(key, value);
		}
	}

	public static boolean isLws(byte b) {
		switch (b) {
		case 9:
		case 10:
		case 13:
		case 32:
			return true;
		default:
			return false;
		}
	}

	private static int skipLws(byte[] buf, int start) {
		int i;
		for (i = start; i < buf.length; ++i) {
			if (!isLws(buf[i])) {
				return i;
			}
		}

		return i;
	}

	public static String stringTo8859_1(String str) throws UnsupportedEncodingException {
		return str == null ? "" : new String(str.getBytes("UTF8"), "8859_1");
	}

	public static String getSingleValuedHeader(Map<String, List<String>> headers, String key) {
		List values = (List) headers.get(key);
		if (values == null) {
			return null;
		} else if (values.size() > 1) {
			throw new IllegalArgumentException("Header with key [\"" + key + "\"] isn\'t single valued !");
		} else {
			return (String) values.get(0);
		}
	}

	public static void addValueToHeader(Map<String, List<String>> headers, String key, String value,
			boolean singleValued) {
		Object values = (List) headers.get(key);
		if (values == null) {
			values = new ArrayList(1);
			headers.put(key, values);
		}

		if (singleValued && ((List) values).size() == 1) {
			((List) values).set(0, value);
		} else {
			((List) values).add(value);
		}

	}
}