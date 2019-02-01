/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.buffer;

import org.apache.mina.core.buffer.IoBuffer;

class IoBufferHexDumper {
	private static final byte[] highDigits;
	private static final byte[] lowDigits;

	public static String getHexdump(IoBuffer in, int lengthLimit) {
		if (lengthLimit == 0) {
			throw new IllegalArgumentException("lengthLimit: " + lengthLimit + " (expected: 1+)");
		} else {
			boolean truncate = in.remaining() > lengthLimit;
			int size;
			if (truncate) {
				size = lengthLimit;
			} else {
				size = in.remaining();
			}

			if (size == 0) {
				return "empty";
			} else {
				StringBuilder out = new StringBuilder(size * 3 + 3);
				int mark = in.position();
				int byteValue = in.get() & 255;
				out.append((char) highDigits[byteValue]);
				out.append((char) lowDigits[byteValue]);
				--size;

				while (size > 0) {
					out.append(' ');
					byteValue = in.get() & 255;
					out.append((char) highDigits[byteValue]);
					out.append((char) lowDigits[byteValue]);
					--size;
				}

				in.position(mark);
				if (truncate) {
					out.append("...");
				}

				return out.toString();
			}
		}
	}

	static {
		byte[] digits = new byte[] { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70 };
		byte[] high = new byte[256];
		byte[] low = new byte[256];

		for (int i = 0; i < 256; ++i) {
			high[i] = digits[i >>> 4];
			low[i] = digits[i & 15];
		}

		highDigits = high;
		lowDigits = low;
	}
}