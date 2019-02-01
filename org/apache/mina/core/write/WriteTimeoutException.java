/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.write;

import java.util.Collection;
import org.apache.mina.core.write.WriteException;
import org.apache.mina.core.write.WriteRequest;

public class WriteTimeoutException extends WriteException {
	private static final long serialVersionUID = 3906931157944579121L;

	public WriteTimeoutException(Collection<WriteRequest> requests, String message, Throwable cause) {
		super(requests, message, cause);
	}

	public WriteTimeoutException(Collection<WriteRequest> requests, String s) {
		super(requests, s);
	}

	public WriteTimeoutException(Collection<WriteRequest> requests, Throwable cause) {
		super(requests, cause);
	}

	public WriteTimeoutException(Collection<WriteRequest> requests) {
		super(requests);
	}

	public WriteTimeoutException(WriteRequest request, String message, Throwable cause) {
		super(request, message, cause);
	}

	public WriteTimeoutException(WriteRequest request, String s) {
		super(request, s);
	}

	public WriteTimeoutException(WriteRequest request, Throwable cause) {
		super(request, cause);
	}

	public WriteTimeoutException(WriteRequest request) {
		super(request);
	}
}