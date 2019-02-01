/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.http.ntlm;

public interface NTLMConstants {
	byte[] NTLM_SIGNATURE = new byte[] { 78, 84, 76, 77, 83, 83, 80, 0 };
	byte[] DEFAULT_OS_VERSION = new byte[] { 5, 1, 40, 10, 0, 0, 0, 15 };
	int MESSAGE_TYPE_1 = 1;
	int MESSAGE_TYPE_2 = 2;
	int MESSAGE_TYPE_3 = 3;
	int FLAG_NEGOTIATE_UNICODE = 1;
	int FLAG_NEGOTIATE_OEM = 2;
	int FLAG_REQUEST_SERVER_AUTH_REALM = 4;
	int FLAG_NEGOTIATE_SIGN = 16;
	int FLAG_NEGOTIATE_SEAL = 32;
	int FLAG_NEGOTIATE_DATAGRAM_STYLE = 64;
	int FLAG_NEGOTIATE_LAN_MANAGER_KEY = 128;
	int FLAG_NEGOTIATE_NTLM = 512;
	int FLAG_NEGOTIATE_ANONYMOUS = 2048;
	int FLAG_NEGOTIATE_DOMAIN_SUPPLIED = 4096;
	int FLAG_NEGOTIATE_WORKSTATION_SUPPLIED = 8192;
	int FLAG_NEGOTIATE_LOCAL_CALL = 16384;
	int FLAG_NEGOTIATE_ALWAYS_SIGN = 32768;
	int FLAG_TARGET_TYPE_DOMAIN = 65536;
	int FLAG_TARGET_TYPE_SERVER = 131072;
	int FLAG_TARGET_TYPE_SHARE = 262144;
	int FLAG_NEGOTIATE_NTLM2 = 524288;
	int FLAG_NEGOTIATE_TARGET_INFO = 8388608;
	int FLAG_NEGOTIATE_128_BIT_ENCRYPTION = 536870912;
	int FLAG_NEGOTIATE_KEY_EXCHANGE = 1073741824;
	int FLAG_NEGOTIATE_56_BIT_ENCRYPTION = Integer.MIN_VALUE;
	int FLAG_UNIDENTIFIED_1 = 8;
	int FLAG_UNIDENTIFIED_2 = 256;
	int FLAG_UNIDENTIFIED_3 = 1024;
	int FLAG_UNIDENTIFIED_4 = 1048576;
	int FLAG_UNIDENTIFIED_5 = 2097152;
	int FLAG_UNIDENTIFIED_6 = 4194304;
	int FLAG_UNIDENTIFIED_7 = 16777216;
	int FLAG_UNIDENTIFIED_8 = 33554432;
	int FLAG_UNIDENTIFIED_9 = 67108864;
	int FLAG_UNIDENTIFIED_10 = 134217728;
	int FLAG_UNIDENTIFIED_11 = 268435456;
	int DEFAULT_FLAGS = 12291;
	short TARGET_INFORMATION_SUBBLOCK_TERMINATOR_TYPE = 0;
	short TARGET_INFORMATION_SUBBLOCK_SERVER_TYPE = 256;
	short TARGET_INFORMATION_SUBBLOCK_DOMAIN_TYPE = 512;
	short TARGET_INFORMATION_SUBBLOCK_FQDNS_HOSTNAME_TYPE = 768;
	short TARGET_INFORMATION_SUBBLOCK_DNS_DOMAIN_NAME_TYPE = 1024;
	short TARGET_INFORMATION_SUBBLOCK_PARENT_DNS_DOMAIN_NAME_TYPE = 1280;
}