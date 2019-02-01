/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util.byteaccess;

import org.apache.mina.util.byteaccess.ByteArray;
import org.apache.mina.util.byteaccess.ByteArray.Cursor;

abstract class AbstractByteArray implements ByteArray {
	public final int length() {
		return this.last() - this.first();
	}

	public final boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (!(other instanceof ByteArray)) {
			return false;
		} else {
			ByteArray otherByteArray = (ByteArray) other;
			if (this.first() == otherByteArray.first() && this.last() == otherByteArray.last()
					&& this.order().equals(otherByteArray.order())) {
				Cursor cursor = this.cursor();
				Cursor otherCursor = otherByteArray.cursor();
				int remaining = cursor.getRemaining();

				while (remaining > 0) {
					if (remaining >= 4) {
						int b = cursor.getInt();
						int otherB = otherCursor.getInt();
						if (b != otherB) {
							return false;
						}
					} else {
						byte b1 = cursor.get();
						byte otherB1 = otherCursor.get();
						if (b1 != otherB1) {
							return false;
						}
					}
				}

				return true;
			} else {
				return false;
			}
		}
	}
}