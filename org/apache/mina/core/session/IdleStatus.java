/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

public class IdleStatus {
	public static final IdleStatus READER_IDLE = new IdleStatus("reader idle");
	public static final IdleStatus WRITER_IDLE = new IdleStatus("writer idle");
	public static final IdleStatus BOTH_IDLE = new IdleStatus("both idle");
	private final String strValue;

	private IdleStatus(String strValue) {
		this.strValue = strValue;
	}

	public String toString() {
		return this.strValue;
	}
}