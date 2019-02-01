/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.write;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.util.MapBackedSet;

public class WriteException extends IOException {
	private static final long serialVersionUID = -4174407422754524197L;
	private final List<WriteRequest> requests;

	public WriteException(WriteRequest request) {
		this.requests = asRequestList(request);
	}

	public WriteException(WriteRequest request, String message) {
		super(message);
		this.requests = asRequestList(request);
	}

	public WriteException(WriteRequest request, String message, Throwable cause) {
		super(message);
		this.initCause(cause);
		this.requests = asRequestList(request);
	}

	public WriteException(WriteRequest request, Throwable cause) {
		this.initCause(cause);
		this.requests = asRequestList(request);
	}

	public WriteException(Collection<WriteRequest> requests) {
		this.requests = asRequestList(requests);
	}

	public WriteException(Collection<WriteRequest> requests, String message) {
		super(message);
		this.requests = asRequestList(requests);
	}

	public WriteException(Collection<WriteRequest> requests, String message, Throwable cause) {
		super(message);
		this.initCause(cause);
		this.requests = asRequestList(requests);
	}

	public WriteException(Collection<WriteRequest> requests, Throwable cause) {
		this.initCause(cause);
		this.requests = asRequestList(requests);
	}

	public List<WriteRequest> getRequests() {
		return this.requests;
	}

	public WriteRequest getRequest() {
		return (WriteRequest) this.requests.get(0);
	}

	private static List<WriteRequest> asRequestList(Collection<WriteRequest> requests) {
		if (requests == null) {
			throw new IllegalArgumentException("requests");
		} else if (requests.isEmpty()) {
			throw new IllegalArgumentException("requests is empty.");
		} else {
			MapBackedSet newRequests = new MapBackedSet(new LinkedHashMap());
			Iterator i$ = requests.iterator();

			while (i$.hasNext()) {
				WriteRequest r = (WriteRequest) i$.next();
				newRequests.add(r.getOriginalRequest());
			}

			return Collections.unmodifiableList(new ArrayList(newRequests));
		}
	}

	private static List<WriteRequest> asRequestList(WriteRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request");
		} else {
			ArrayList requests = new ArrayList(1);
			requests.add(request.getOriginalRequest());
			return Collections.unmodifiableList(requests);
		}
	}
}