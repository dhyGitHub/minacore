/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util;

import java.security.InvalidParameterException;

public class Base64 {
	static final int CHUNK_SIZE = 76;
	static final byte[] CHUNK_SEPARATOR = "\r\n".getBytes();
	static final int BASELENGTH = 255;
	static final int LOOKUPLENGTH = 64;
	static final int EIGHTBIT = 8;
	static final int SIXTEENBIT = 16;
	static final int TWENTYFOURBITGROUP = 24;
	static final int FOURBYTE = 4;
	static final int SIGN = -128;
	static final byte PAD = 61;
	private static byte[] base64Alphabet = new byte[255];
	private static byte[] lookUpBase64Alphabet = new byte[64];

	private static boolean isBase64(byte octect) {
		return octect == 61 ? true : base64Alphabet[octect] != -1;
	}

	public static boolean isArrayByteBase64(byte[] arrayOctect) {
		arrayOctect = discardWhitespace(arrayOctect);
		int length = arrayOctect.length;
		if (length == 0) {
			return true;
		} else {
			for (int i = 0; i < length; ++i) {
				if (!isBase64(arrayOctect[i])) {
					return false;
				}
			}

			return true;
		}
	}

	public static byte[] encodeBase64(byte[] binaryData) {
		return encodeBase64(binaryData, false);
	}

	public static byte[] encodeBase64Chunked(byte[] binaryData) {
		return encodeBase64(binaryData, true);
	}

	public Object decode(Object pObject) {
		if (!(pObject instanceof byte[])) {
			throw new InvalidParameterException("Parameter supplied to Base64 decode is not a byte[]");
		} else {
			return this.decode((byte[]) ((byte[]) pObject));
		}
	}

	public byte[] decode(byte[] pArray) {
		return decodeBase64(pArray);
	}

	public static byte[] encodeBase64(byte[] binaryData, boolean isChunked) {
		int lengthDataBits = binaryData.length * 8;
		int fewerThan24bits = lengthDataBits % 24;
		int numberTriplets = lengthDataBits / 24;
		Object encodedData = null;
		boolean encodedDataLength = false;
		int nbrChunks = 0;
		int arg21;
		if (fewerThan24bits != 0) {
			arg21 = (numberTriplets + 1) * 4;
		} else {
			arg21 = numberTriplets * 4;
		}

		if (isChunked) {
			nbrChunks = CHUNK_SEPARATOR.length == 0 ? 0 : (int) Math.ceil((double) ((float) arg21 / 76.0F));
			arg21 += nbrChunks * CHUNK_SEPARATOR.length;
		}

		byte[] arg20 = new byte[arg21];
		boolean k = false;
		boolean l = false;
		boolean b1 = false;
		boolean b2 = false;
		boolean b3 = false;
		int encodedIndex = 0;
		boolean dataIndex = false;
		boolean i = false;
		int nextSeparatorIndex = 76;
		int chunksSoFar = 0;

		byte val1;
		byte val2;
		byte arg22;
		byte arg23;
		byte arg24;
		byte arg25;
		int arg27;
		int arg28;
		for (arg28 = 0; arg28 < numberTriplets; ++arg28) {
			arg27 = arg28 * 3;
			arg24 = binaryData[arg27];
			arg25 = binaryData[arg27 + 1];
			byte arg26 = binaryData[arg27 + 2];
			arg23 = (byte) (arg25 & 15);
			arg22 = (byte) (arg24 & 3);
			val1 = (arg24 & -128) == 0 ? (byte) (arg24 >> 2) : (byte) (arg24 >> 2 ^ 192);
			val2 = (arg25 & -128) == 0 ? (byte) (arg25 >> 4) : (byte) (arg25 >> 4 ^ 240);
			byte val3 = (arg26 & -128) == 0 ? (byte) (arg26 >> 6) : (byte) (arg26 >> 6 ^ 252);
			arg20[encodedIndex] = lookUpBase64Alphabet[val1];
			arg20[encodedIndex + 1] = lookUpBase64Alphabet[val2 | arg22 << 4];
			arg20[encodedIndex + 2] = lookUpBase64Alphabet[arg23 << 2 | val3];
			arg20[encodedIndex + 3] = lookUpBase64Alphabet[arg26 & 63];
			encodedIndex += 4;
			if (isChunked && encodedIndex == nextSeparatorIndex) {
				System.arraycopy(CHUNK_SEPARATOR, 0, arg20, encodedIndex, CHUNK_SEPARATOR.length);
				++chunksSoFar;
				nextSeparatorIndex = 76 * (chunksSoFar + 1) + chunksSoFar * CHUNK_SEPARATOR.length;
				encodedIndex += CHUNK_SEPARATOR.length;
			}
		}

		arg27 = arg28 * 3;
		if (fewerThan24bits == 8) {
			arg24 = binaryData[arg27];
			arg22 = (byte) (arg24 & 3);
			val1 = (arg24 & -128) == 0 ? (byte) (arg24 >> 2) : (byte) (arg24 >> 2 ^ 192);
			arg20[encodedIndex] = lookUpBase64Alphabet[val1];
			arg20[encodedIndex + 1] = lookUpBase64Alphabet[arg22 << 4];
			arg20[encodedIndex + 2] = 61;
			arg20[encodedIndex + 3] = 61;
		} else if (fewerThan24bits == 16) {
			arg24 = binaryData[arg27];
			arg25 = binaryData[arg27 + 1];
			arg23 = (byte) (arg25 & 15);
			arg22 = (byte) (arg24 & 3);
			val1 = (arg24 & -128) == 0 ? (byte) (arg24 >> 2) : (byte) (arg24 >> 2 ^ 192);
			val2 = (arg25 & -128) == 0 ? (byte) (arg25 >> 4) : (byte) (arg25 >> 4 ^ 240);
			arg20[encodedIndex] = lookUpBase64Alphabet[val1];
			arg20[encodedIndex + 1] = lookUpBase64Alphabet[val2 | arg22 << 4];
			arg20[encodedIndex + 2] = lookUpBase64Alphabet[arg23 << 2];
			arg20[encodedIndex + 3] = 61;
		}

		if (isChunked && chunksSoFar < nbrChunks) {
			System.arraycopy(CHUNK_SEPARATOR, 0, arg20, arg21 - CHUNK_SEPARATOR.length, CHUNK_SEPARATOR.length);
		}

		return arg20;
	}

	public static byte[] decodeBase64(byte[] base64Data) {
		base64Data = discardNonBase64(base64Data);
		if (base64Data.length == 0) {
			return new byte[0];
		} else {
			int numberQuadruple = base64Data.length / 4;
			Object decodedData = null;
			boolean b1 = false;
			boolean b2 = false;
			boolean b3 = false;
			boolean b4 = false;
			boolean marker0 = false;
			boolean marker1 = false;
			int encodedIndex = 0;
			boolean dataIndex = false;
			int i = base64Data.length;

			while (base64Data[i - 1] == 61) {
				--i;
				if (i == 0) {
					return new byte[0];
				}
			}

			byte[] arg11 = new byte[i - numberQuadruple];

			for (i = 0; i < numberQuadruple; ++i) {
				int arg18 = i * 4;
				byte arg16 = base64Data[arg18 + 2];
				byte arg17 = base64Data[arg18 + 3];
				byte arg12 = base64Alphabet[base64Data[arg18]];
				byte arg13 = base64Alphabet[base64Data[arg18 + 1]];
				byte arg14;
				if (arg16 != 61 && arg17 != 61) {
					arg14 = base64Alphabet[arg16];
					byte arg15 = base64Alphabet[arg17];
					arg11[encodedIndex] = (byte) (arg12 << 2 | arg13 >> 4);
					arg11[encodedIndex + 1] = (byte) ((arg13 & 15) << 4 | arg14 >> 2 & 15);
					arg11[encodedIndex + 2] = (byte) (arg14 << 6 | arg15);
				} else if (arg16 == 61) {
					arg11[encodedIndex] = (byte) (arg12 << 2 | arg13 >> 4);
				} else if (arg17 == 61) {
					arg14 = base64Alphabet[arg16];
					arg11[encodedIndex] = (byte) (arg12 << 2 | arg13 >> 4);
					arg11[encodedIndex + 1] = (byte) ((arg13 & 15) << 4 | arg14 >> 2 & 15);
				}

				encodedIndex += 3;
			}

			return arg11;
		}
	}

	static byte[] discardWhitespace(byte[] data) {
		byte[] groomedData = new byte[data.length];
		int bytesCopied = 0;
		int packedData = 0;

		while (packedData < data.length) {
			switch (data[packedData]) {
			default:
				groomedData[bytesCopied++] = data[packedData];
			case 9:
			case 10:
			case 13:
			case 32:
				++packedData;
			}
		}

		byte[] arg3 = new byte[bytesCopied];
		System.arraycopy(groomedData, 0, arg3, 0, bytesCopied);
		return arg3;
	}

	static byte[] discardNonBase64(byte[] data) {
		byte[] groomedData = new byte[data.length];
		int bytesCopied = 0;

		for (int packedData = 0; packedData < data.length; ++packedData) {
			if (isBase64(data[packedData])) {
				groomedData[bytesCopied++] = data[packedData];
			}
		}

		byte[] arg3 = new byte[bytesCopied];
		System.arraycopy(groomedData, 0, arg3, 0, bytesCopied);
		return arg3;
	}

	public Object encode(Object pObject) {
		if (!(pObject instanceof byte[])) {
			throw new InvalidParameterException("Parameter supplied to Base64 encode is not a byte[]");
		} else {
			return this.encode((byte[]) ((byte[]) pObject));
		}
	}

	public byte[] encode(byte[] pArray) {
		return encodeBase64(pArray, false);
	}

	static {
		int i;
		for (i = 0; i < 255; ++i) {
			base64Alphabet[i] = -1;
		}

		for (i = 90; i >= 65; --i) {
			base64Alphabet[i] = (byte) (i - 65);
		}

		for (i = 122; i >= 97; --i) {
			base64Alphabet[i] = (byte) (i - 97 + 26);
		}

		for (i = 57; i >= 48; --i) {
			base64Alphabet[i] = (byte) (i - 48 + 52);
		}

		base64Alphabet[43] = 62;
		base64Alphabet[47] = 63;

		for (i = 0; i <= 25; ++i) {
			lookUpBase64Alphabet[i] = (byte) (65 + i);
		}

		i = 26;

		int j;
		for (j = 0; i <= 51; ++j) {
			lookUpBase64Alphabet[i] = (byte) (97 + j);
			++i;
		}

		i = 52;

		for (j = 0; i <= 61; ++j) {
			lookUpBase64Alphabet[i] = (byte) (48 + j);
			++i;
		}

		lookUpBase64Alphabet[62] = 43;
		lookUpBase64Alphabet[63] = 47;
	}
}