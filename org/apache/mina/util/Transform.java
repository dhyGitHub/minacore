/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

public class Transform {
	private static final String CDATA_START = "<![CDATA[";
	private static final String CDATA_END = "]]>";
	private static final String CDATA_PSEUDO_END = "]]&gt;";
	private static final String CDATA_EMBEDED_END = "]]>]]&gt;<![CDATA[";
	private static final int CDATA_END_LEN = "]]>".length();

	public static String escapeTags(String input) {
		if (input == null || input.length() == 0 || input.indexOf(34) == -1 && input.indexOf(38) == -1
				&& input.indexOf(60) == -1 && input.indexOf(62) == -1) {
			return input;
		} else {
			StringBuilder buf = new StringBuilder(input.length() + 6);
			int len = input.length();

			for (int i = 0; i < len; ++i) {
				char ch = input.charAt(i);
				if (ch > 62) {
					buf.append(ch);
				} else if (ch == 60) {
					buf.append("&lt;");
				} else if (ch == 62) {
					buf.append("&gt;");
				} else if (ch == 38) {
					buf.append("&amp;");
				} else if (ch == 34) {
					buf.append("&quot;");
				} else {
					buf.append(ch);
				}
			}

			return buf.toString();
		}
	}

	public static void appendEscapingCDATA(StringBuffer buf, String str) {
		if (str != null) {
			int end = str.indexOf("]]>");
			if (end < 0) {
				buf.append(str);
			} else {
				int start;
				for (start = 0; end > -1; end = str.indexOf("]]>", start)) {
					buf.append(str.substring(start, end));
					buf.append("]]>]]&gt;<![CDATA[");
					start = end + CDATA_END_LEN;
					if (start >= str.length()) {
						return;
					}
				}

				buf.append(str.substring(start));
			}
		}

	}

	public static String[] getThrowableStrRep(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		pw.flush();
		LineNumberReader reader = new LineNumberReader(new StringReader(sw.toString()));
		ArrayList lines = new ArrayList();

		try {
			for (String rep = reader.readLine(); rep != null; rep = reader.readLine()) {
				lines.add(rep);
			}
		} catch (IOException arg5) {
			lines.add(arg5.toString());
		}

		String[] rep1 = new String[lines.size()];
		lines.toArray(rep1);
		return rep1;
	}
}