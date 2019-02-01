/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.util;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestWrapper;

public abstract class WriteRequestFilter extends IoFilterAdapter {
	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		Object filteredMessage = this.doFilterWrite(nextFilter, session, writeRequest);
		if (filteredMessage != null && filteredMessage != writeRequest.getMessage()) {
			nextFilter.filterWrite(session, new WriteRequestFilter.FilteredWriteRequest(filteredMessage, writeRequest));
		} else {
			nextFilter.filterWrite(session, writeRequest);
		}

	}

	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		if (writeRequest instanceof WriteRequestFilter.FilteredWriteRequest) {
			WriteRequestFilter.FilteredWriteRequest req = (WriteRequestFilter.FilteredWriteRequest) writeRequest;
			if (req.getParent() == this) {
				nextFilter.messageSent(session, req.getParentRequest());
				return;
			}
		}

		nextFilter.messageSent(session, writeRequest);
	}

	protected abstract Object doFilterWrite(NextFilter arg0, IoSession arg1, WriteRequest arg2) throws Exception;

	private class FilteredWriteRequest extends WriteRequestWrapper {
		private final Object filteredMessage;

		public FilteredWriteRequest(Object filteredMessage, WriteRequest writeRequest) {
			super(writeRequest);
			if (filteredMessage == null) {
				throw new IllegalArgumentException("filteredMessage");
			} else {
				this.filteredMessage = filteredMessage;
			}
		}

		public WriteRequestFilter getParent() {
			return WriteRequestFilter.this;
		}

		public Object getMessage() {
			return this.filteredMessage;
		}
	}
}