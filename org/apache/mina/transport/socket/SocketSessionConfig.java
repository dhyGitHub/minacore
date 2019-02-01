/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket;

import org.apache.mina.core.session.IoSessionConfig;

public interface SocketSessionConfig extends IoSessionConfig {
	boolean isReuseAddress();

	void setReuseAddress(boolean arg0);

	int getReceiveBufferSize();

	void setReceiveBufferSize(int arg0);

	int getSendBufferSize();

	void setSendBufferSize(int arg0);

	int getTrafficClass();

	void setTrafficClass(int arg0);

	boolean isKeepAlive();

	void setKeepAlive(boolean arg0);

	boolean isOobInline();

	void setOobInline(boolean arg0);

	int getSoLinger();

	void setSoLinger(int arg0);

	boolean isTcpNoDelay();

	void setTcpNoDelay(boolean arg0);
}