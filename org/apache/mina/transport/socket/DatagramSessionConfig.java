/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.transport.socket;

import org.apache.mina.core.session.IoSessionConfig;

public interface DatagramSessionConfig extends IoSessionConfig {
	boolean isBroadcast();

	void setBroadcast(boolean arg0);

	boolean isReuseAddress();

	void setReuseAddress(boolean arg0);

	int getReceiveBufferSize();

	void setReceiveBufferSize(int arg0);

	int getSendBufferSize();

	void setSendBufferSize(int arg0);

	int getTrafficClass();

	void setTrafficClass(int arg0);

	boolean isCloseOnPortUnreachable();

	void setCloseOnPortUnreachable(boolean arg0);
}