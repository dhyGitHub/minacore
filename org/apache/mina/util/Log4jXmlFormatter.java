/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import org.apache.mina.util.Transform;
import org.slf4j.MDC;

public class Log4jXmlFormatter extends Formatter {
	private final int DEFAULT_SIZE = 256;
	private final int UPPER_LIMIT = 2048;
	private StringBuffer buf = new StringBuffer(256);
	private boolean locationInfo = false;
	private boolean properties = false;

	public void setLocationInfo(boolean flag) {
		this.locationInfo = flag;
	}

	public boolean getLocationInfo() {
		return this.locationInfo;
	}

	public void setProperties(boolean flag) {
		this.properties = flag;
	}

	public boolean getProperties() {
		return this.properties;
	}

	public String format(LogRecord record) {
		if (this.buf.capacity() > 2048) {
			this.buf = new StringBuffer(256);
		} else {
			this.buf.setLength(0);
		}

		this.buf.append("<log4j:event logger=\"");
		this.buf.append(Transform.escapeTags(record.getLoggerName()));
		this.buf.append("\" timestamp=\"");
		this.buf.append(record.getMillis());
		this.buf.append("\" level=\"");
		this.buf.append(Transform.escapeTags(record.getLevel().getName()));
		this.buf.append("\" thread=\"");
		this.buf.append(String.valueOf(record.getThreadID()));
		this.buf.append("\">\r\n");
		this.buf.append("<log4j:message><![CDATA[");
		Transform.appendEscapingCDATA(this.buf, record.getMessage());
		this.buf.append("]]></log4j:message>\r\n");
		if (record.getThrown() != null) {
			String[] contextMap = Transform.getThrowableStrRep(record.getThrown());
			if (contextMap != null) {
				this.buf.append("<log4j:throwable><![CDATA[");
				String[] keySet = contextMap;
				int keys = contextMap.length;

				for (int arr$ = 0; arr$ < keys; ++arr$) {
					String len$ = keySet[arr$];
					Transform.appendEscapingCDATA(this.buf, len$);
					this.buf.append("\r\n");
				}

				this.buf.append("]]></log4j:throwable>\r\n");
			}
		}

		if (this.locationInfo) {
			this.buf.append("<log4j:locationInfo class=\"");
			this.buf.append(Transform.escapeTags(record.getSourceClassName()));
			this.buf.append("\" method=\"");
			this.buf.append(Transform.escapeTags(record.getSourceMethodName()));
			this.buf.append("\" file=\"?\" line=\"?\"/>\r\n");
		}

		if (this.properties) {
			Map arg10 = MDC.getCopyOfContextMap();
			if (arg10 != null) {
				Set arg11 = arg10.keySet();
				if (arg11 != null && arg11.size() > 0) {
					this.buf.append("<log4j:properties>\r\n");
					Object[] arg12 = arg11.toArray();
					Arrays.sort(arg12);
					Object[] arg13 = arg12;
					int arg14 = arg12.length;

					for (int i$ = 0; i$ < arg14; ++i$) {
						Object key1 = arg13[i$];
						String key = key1 == null ? "" : key1.toString();
						Object val = arg10.get(key);
						if (val != null) {
							this.buf.append("<log4j:data name=\"");
							this.buf.append(Transform.escapeTags(key));
							this.buf.append("\" value=\"");
							this.buf.append(Transform.escapeTags(String.valueOf(val)));
							this.buf.append("\"/>\r\n");
						}
					}

					this.buf.append("</log4j:properties>\r\n");
				}
			}
		}

		this.buf.append("</log4j:event>\r\n\r\n");
		return this.buf.toString();
	}
}