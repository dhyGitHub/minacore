/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import java.io.Serializable;

public final class AttributeKey implements Serializable {
	private static final long serialVersionUID = -583377473376683096L;
	private final String name;

	public AttributeKey(Class<?> source, String name) {
		this.name = source.getName() + '.' + name + '@' + Integer.toHexString(this.hashCode());
	}

	public String toString() {
		return this.name;
	}

	public int hashCode() {
		int h = 629 + (this.name == null ? 0 : this.name.hashCode());
		return h;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof AttributeKey)) {
			return false;
		} else {
			AttributeKey other = (AttributeKey) obj;
			return this.name.equals(other.name);
		}
	}
}