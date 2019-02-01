/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.handlers.socks;

public class SocksProxyConstants {
	public static final byte SOCKS_VERSION_4 = 4;
	public static final byte SOCKS_VERSION_5 = 5;
	public static final byte TERMINATOR = 0;
	public static final int SOCKS_4_RESPONSE_SIZE = 8;
	public static final byte[] FAKE_IP = new byte[] { 0, 0, 0, 10 };
	public static final byte ESTABLISH_TCPIP_STREAM = 1;
	public static final byte ESTABLISH_TCPIP_BIND = 2;
	public static final byte ESTABLISH_UDP_ASSOCIATE = 3;
	public static final byte V4_REPLY_REQUEST_GRANTED = 90;
	public static final byte V4_REPLY_REQUEST_REJECTED_OR_FAILED = 91;
	public static final byte V4_REPLY_REQUEST_FAILED_NO_IDENTD = 92;
	public static final byte V4_REPLY_REQUEST_FAILED_ID_NOT_CONFIRMED = 93;
	public static final byte V5_REPLY_SUCCEEDED = 0;
	public static final byte V5_REPLY_GENERAL_FAILURE = 1;
	public static final byte V5_REPLY_NOT_ALLOWED = 2;
	public static final byte V5_REPLY_NETWORK_UNREACHABLE = 3;
	public static final byte V5_REPLY_HOST_UNREACHABLE = 4;
	public static final byte V5_REPLY_CONNECTION_REFUSED = 5;
	public static final byte V5_REPLY_TTL_EXPIRED = 6;
	public static final byte V5_REPLY_COMMAND_NOT_SUPPORTED = 7;
	public static final byte V5_REPLY_ADDRESS_TYPE_NOT_SUPPORTED = 8;
	public static final byte IPV4_ADDRESS_TYPE = 1;
	public static final byte DOMAIN_NAME_ADDRESS_TYPE = 3;
	public static final byte IPV6_ADDRESS_TYPE = 4;
	public static final int SOCKS5_GREETING_STEP = 0;
	public static final int SOCKS5_AUTH_STEP = 1;
	public static final int SOCKS5_REQUEST_STEP = 2;
	public static final byte NO_AUTH = 0;
	public static final byte GSSAPI_AUTH = 1;
	public static final byte BASIC_AUTH = 2;
	public static final byte NO_ACCEPTABLE_AUTH_METHOD = -1;
	public static final byte[] SUPPORTED_AUTH_METHODS = new byte[] { 0, 1, 2 };
	public static final byte BASIC_AUTH_SUBNEGOTIATION_VERSION = 1;
	public static final byte GSSAPI_AUTH_SUBNEGOTIATION_VERSION = 1;
	public static final byte GSSAPI_MSG_TYPE = 1;
	public static final String KERBEROS_V5_OID = "1.2.840.113554.1.2.2";
	public static final String MS_KERBEROS_V5_OID = "1.2.840.48018.1.2.2";
	public static final String NTLMSSP_OID = "1.3.6.1.4.1.311.2.2.10";

	public static final String getReplyCodeAsString(byte code) {
		switch (code) {
		case 0:
			return "Request succeeded";
		case 1:
			return "Request failed: general SOCKS server failure";
		case 2:
			return "Request failed: connection not allowed by ruleset";
		case 3:
			return "Request failed: network unreachable";
		case 4:
			return "Request failed: host unreachable";
		case 5:
			return "Request failed: connection refused";
		case 6:
			return "Request failed: TTL expired";
		case 7:
			return "Request failed: command not supported";
		case 8:
			return "Request failed: address type not supported";
		case 90:
			return "Request granted";
		case 91:
			return "Request rejected or failed";
		case 92:
			return "Request failed because client is not running identd (or not reachable from the server)";
		case 93:
			return "Request failed because client\'s identd could not confirm the user ID string in the request";
		default:
			return "Unknown reply code";
		}
	}
}