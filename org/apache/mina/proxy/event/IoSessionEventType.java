/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.event;

public enum IoSessionEventType {
	CREATED(1), OPENED(2), IDLE(3), CLOSED(4);

	private final int id;

	private IoSessionEventType(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public String toString() {
		switch (IoSessionEventType.SyntheticClass_1.$SwitchMap$org$apache$mina$proxy$event$IoSessionEventType[this
				.ordinal()]) {
		case 1:
			return "- CREATED event -";
		case 2:
			return "- OPENED event -";
		case 3:
			return "- IDLE event -";
		case 4:
			return "- CLOSED event -";
		default:
			return "- Event Id=" + this.id + " -";
		}
	}
}