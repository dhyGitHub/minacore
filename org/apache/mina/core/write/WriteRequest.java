/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.write;

import java.net.SocketAddress;
import org.apache.mina.core.future.WriteFuture;

public interface WriteRequest {
	WriteRequest getOriginalRequest();

	WriteFuture getFuture();

	Object getMessage();

	SocketAddress getDestination();

	boolean isEncoded();
}