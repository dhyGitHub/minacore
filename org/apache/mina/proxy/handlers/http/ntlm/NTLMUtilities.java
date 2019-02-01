/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http.ntlm;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import org.apache.mina.proxy.handlers.http.ntlm.NTLMConstants;
import org.apache.mina.proxy.handlers.http.ntlm.NTLMResponses;
import org.apache.mina.proxy.utils.ByteUtilities;

public class NTLMUtilities implements NTLMConstants {
	public static final byte[] writeSecurityBuffer(short length, int bufferOffset) {
		byte[] b = new byte[8];
		writeSecurityBuffer(length, length, bufferOffset, b, 0);
		return b;
	}

	public static final void writeSecurityBuffer(short length, short allocated, int bufferOffset, byte[] b,
			int offset) {
		ByteUtilities.writeShort(length, b, offset);
		ByteUtilities.writeShort(allocated, b, offset + 2);
		ByteUtilities.writeInt(bufferOffset, b, offset + 4);
	}

	public static final void writeOSVersion(byte majorVersion, byte minorVersion, short buildNumber, byte[] b,
			int offset) {
		b[offset] = majorVersion;
		b[offset + 1] = minorVersion;
		b[offset + 2] = (byte) buildNumber;
		b[offset + 3] = (byte) (buildNumber >> 8);
		b[offset + 4] = 0;
		b[offset + 5] = 0;
		b[offset + 6] = 0;
		b[offset + 7] = 15;
	}

	public static final byte[] getOsVersion() {
		String os = System.getProperty("os.name");
		if (os != null && os.toUpperCase().contains("WINDOWS")) {
			byte[] osVer = new byte[8];

			try {
				Process ex = Runtime.getRuntime().exec("cmd /C ver");
				BufferedReader ex21 = new BufferedReader(new InputStreamReader(ex.getInputStream()));
				ex.waitFor();

				String line;
				do {
					line = ex21.readLine();
				} while (line != null && line.length() != 0);

				ex21.close();
				if (line == null) {
					throw new Exception();
				}

				int pos = line.toLowerCase().indexOf("version");
				if (pos == -1) {
					throw new Exception();
				}

				pos += 8;
				line = line.substring(pos, line.indexOf(93));
				StringTokenizer tk = new StringTokenizer(line, ".");
				if (tk.countTokens() != 3) {
					throw new Exception();
				}

				writeOSVersion(Byte.parseByte(tk.nextToken()), Byte.parseByte(tk.nextToken()),
						Short.parseShort(tk.nextToken()), osVer, 0);
			} catch (Exception arg7) {
				try {
					String ex2 = System.getProperty("os.version");
					writeOSVersion(Byte.parseByte(ex2.substring(0, 1)), Byte.parseByte(ex2.substring(2, 3)), 0, osVer,
							0);
				} catch (Exception arg6) {
					return DEFAULT_OS_VERSION;
				}
			}

			return osVer;
		} else {
			return DEFAULT_OS_VERSION;
		}
	}

	public static final byte[] createType1Message(String workStation, String domain, Integer customFlags,
			byte[] osVersion) {
		Object msg = null;
		if (osVersion != null && osVersion.length != 8) {
			throw new IllegalArgumentException("osVersion parameter should be a 8 byte wide array");
		} else if (workStation != null && domain != null) {
			int flags = customFlags != null ? customFlags.intValue() | 8192 | 4096 : 12291;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try {
				baos.write(NTLM_SIGNATURE);
				baos.write(ByteUtilities.writeInt(1));
				baos.write(ByteUtilities.writeInt(flags));
				byte[] e = ByteUtilities.getOEMStringAsByteArray(domain);
				byte[] workStationData = ByteUtilities.getOEMStringAsByteArray(workStation);
				int pos = osVersion != null ? 40 : 32;
				baos.write(writeSecurityBuffer((short) e.length, pos + workStationData.length));
				baos.write(writeSecurityBuffer((short) workStationData.length, pos));
				if (osVersion != null) {
					baos.write(osVersion);
				}

				baos.write(workStationData);
				baos.write(e);
				byte[] msg1 = baos.toByteArray();
				baos.close();
				return msg1;
			} catch (IOException arg9) {
				return null;
			}
		} else {
			throw new IllegalArgumentException("workStation and domain must be non null");
		}
	}

	public static final int writeSecurityBufferAndUpdatePointer(ByteArrayOutputStream baos, short len, int pointer)
			throws IOException {
		baos.write(writeSecurityBuffer(len, pointer));
		return pointer + len;
	}

	public static final byte[] extractChallengeFromType2Message(byte[] msg) {
		byte[] challenge = new byte[8];
		System.arraycopy(msg, 24, challenge, 0, 8);
		return challenge;
	}

	public static final int extractFlagsFromType2Message(byte[] msg) {
		byte[] flagsBytes = new byte[4];
		System.arraycopy(msg, 20, flagsBytes, 0, 4);
		ByteUtilities.changeWordEndianess(flagsBytes, 0, 4);
		return ByteUtilities.makeIntFromByte4(flagsBytes);
	}

	public static final byte[] readSecurityBufferTarget(byte[] msg, int securityBufferOffset) {
		byte[] securityBuffer = new byte[8];
		System.arraycopy(msg, securityBufferOffset, securityBuffer, 0, 8);
		ByteUtilities.changeWordEndianess(securityBuffer, 0, 8);
		int length = ByteUtilities.makeIntFromByte2(securityBuffer);
		int offset = ByteUtilities.makeIntFromByte4(securityBuffer, 4);
		byte[] secBufValue = new byte[length];
		System.arraycopy(msg, offset, secBufValue, 0, length);
		return secBufValue;
	}

	public static final String extractTargetNameFromType2Message(byte[] msg, Integer msgFlags)
			throws UnsupportedEncodingException {
		byte[] targetName = readSecurityBufferTarget(msg, 12);
		int flags = msgFlags == null ? extractFlagsFromType2Message(msg) : msgFlags.intValue();
		return ByteUtilities.isFlagSet(flags, 1) ? new String(targetName, "UTF-16LE") : new String(targetName, "ASCII");
	}

	public static final byte[] extractTargetInfoFromType2Message(byte[] msg, Integer msgFlags) {
		int flags = msgFlags == null ? extractFlagsFromType2Message(msg) : msgFlags.intValue();
		if (!ByteUtilities.isFlagSet(flags, 8388608)) {
			return null;
		} else {
			byte pos = 40;
			return readSecurityBufferTarget(msg, pos);
		}
	}

	public static final void printTargetInformationBlockFromType2Message(byte[] msg, Integer msgFlags, PrintWriter out)
			throws UnsupportedEncodingException {
		int flags = msgFlags == null ? extractFlagsFromType2Message(msg) : msgFlags.intValue();
		byte[] infoBlock = extractTargetInfoFromType2Message(msg, Integer.valueOf(flags));
		if (infoBlock == null) {
			out.println("No target information block found !");
		} else {
			int pos = 0;

			while (infoBlock[pos] != 0) {
				out.print("---\nType " + infoBlock[pos] + ": ");
				switch (infoBlock[pos]) {
				case 1:
					out.println("Server name");
					break;
				case 2:
					out.println("Domain name");
					break;
				case 3:
					out.println("Fully qualified DNS hostname");
					break;
				case 4:
					out.println("DNS domain name");
					break;
				case 5:
					out.println("Parent DNS domain name");
				}

				byte[] len = new byte[2];
				System.arraycopy(infoBlock, pos + 2, len, 0, 2);
				ByteUtilities.changeByteEndianess(len, 0, 2);
				int length = ByteUtilities.makeIntFromByte2(len, 0);
				out.println("Length: " + length + " bytes");
				out.print("Data: ");
				if (ByteUtilities.isFlagSet(flags, 1)) {
					out.println(new String(infoBlock, pos + 4, length, "UTF-16LE"));
				} else {
					out.println(new String(infoBlock, pos + 4, length, "ASCII"));
				}

				pos += 4 + length;
				out.flush();
			}
		}

	}

	public static final byte[] createType3Message(String user, String password, byte[] challenge, String target,
			String workstation, Integer serverFlags, byte[] osVersion) {
		Object msg = null;
		if (challenge != null && challenge.length == 8) {
			if (osVersion != null && osVersion.length != 8) {
				throw new IllegalArgumentException("osVersion should be a 8 byte wide array");
			} else {
				int flags = serverFlags != null ? serverFlags.intValue() : 12291;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				try {
					baos.write(NTLM_SIGNATURE);
					baos.write(ByteUtilities.writeInt(3));
					byte[] e = NTLMResponses.getLMResponse(password, challenge);
					byte[] dataNTLMResponse = NTLMResponses.getNTLMResponse(password, challenge);
					boolean useUnicode = ByteUtilities.isFlagSet(flags, 1);
					byte[] targetName = ByteUtilities.encodeString(target, useUnicode);
					byte[] userName = ByteUtilities.encodeString(user, useUnicode);
					byte[] workstationName = ByteUtilities.encodeString(workstation, useUnicode);
					int pos = osVersion != null ? 72 : 64;
					int responsePos = pos + targetName.length + userName.length + workstationName.length;
					responsePos = writeSecurityBufferAndUpdatePointer(baos, (short) e.length, responsePos);
					writeSecurityBufferAndUpdatePointer(baos, (short) dataNTLMResponse.length, responsePos);
					pos = writeSecurityBufferAndUpdatePointer(baos, (short) targetName.length, pos);
					pos = writeSecurityBufferAndUpdatePointer(baos, (short) userName.length, pos);
					writeSecurityBufferAndUpdatePointer(baos, (short) workstationName.length, pos);
					baos.write(new byte[] { 0, 0, 0, 0, -102, 0, 0, 0 });
					baos.write(ByteUtilities.writeInt(flags));
					if (osVersion != null) {
						baos.write(osVersion);
					}

					baos.write(targetName);
					baos.write(userName);
					baos.write(workstationName);
					baos.write(e);
					baos.write(dataNTLMResponse);
					byte[] msg1 = baos.toByteArray();
					baos.close();
					return msg1;
				} catch (Exception arg17) {
					arg17.printStackTrace();
					return null;
				}
			}
		} else {
			throw new IllegalArgumentException("challenge[] should be a 8 byte wide array");
		}
	}
}