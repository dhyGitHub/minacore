/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.write;

import java.net.SocketAddress;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.write.WriteRequest;

public class WriteRequestWrapper implements WriteRequest {
	private final WriteRequest parentRequest;

	public WriteRequestWrapper(WriteRequest parentRequest) {
		if (parentRequest == null) {
			throw new IllegalArgumentException("parentRequest");
		} else {
			this.parentRequest = parentRequest;
		}
	}

	public SocketAddress getDestination() {
		return this.parentRequest.getDestination();
	}

	public WriteFuture getFuture() {
		return this.parentRequest.getFuture();
	}

	public Object getMessage() {
		return this.parentRequest.getMessage();
	}

	public WriteRequest getOriginalRequest() {
		return this.parentRequest.getOriginalRequest();
	}

	public WriteRequest getParentRequest() {
		return this.parentRequest;
	}

	public String toString() {
		return "WR Wrapper" + this.parentRequest.toString();
	}

	public boolean isEncoded() {
		return false;
	}
}