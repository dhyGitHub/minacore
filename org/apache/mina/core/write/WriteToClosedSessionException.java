/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.write;

import java.util.Collection;
import org.apache.mina.core.write.WriteException;
import org.apache.mina.core.write.WriteRequest;

public class WriteToClosedSessionException extends WriteException {
	private static final long serialVersionUID = 5550204573739301393L;

	public WriteToClosedSessionException(Collection<WriteRequest> requests, String message, Throwable cause) {
		super(requests, message, cause);
	}

	public WriteToClosedSessionException(Collection<WriteRequest> requests, String s) {
		super(requests, s);
	}

	public WriteToClosedSessionException(Collection<WriteRequest> requests, Throwable cause) {
		super(requests, cause);
	}

	public WriteToClosedSessionException(Collection<WriteRequest> requests) {
		super(requests);
	}

	public WriteToClosedSessionException(WriteRequest request, String message, Throwable cause) {
		super(request, message, cause);
	}

	public WriteToClosedSessionException(WriteRequest request, String s) {
		super(request, s);
	}

	public WriteToClosedSessionException(WriteRequest request, Throwable cause) {
		super(request, cause);
	}

	public WriteToClosedSessionException(WriteRequest request) {
		super(request);
	}
}