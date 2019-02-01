/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.keepalive;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface KeepAliveRequestTimeoutHandler {
	KeepAliveRequestTimeoutHandler NOOP = new KeepAliveRequestTimeoutHandler() {
		public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
		}
	};
	KeepAliveRequestTimeoutHandler LOG = new KeepAliveRequestTimeoutHandler() {
		private final Logger LOGGER = LoggerFactory.getLogger(KeepAliveFilter.class);

		public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
			this.LOGGER.warn("A keep-alive response message was not received within {} second(s).",
					Integer.valueOf(filter.getRequestTimeout()));
		}
	};
	KeepAliveRequestTimeoutHandler EXCEPTION = new KeepAliveRequestTimeoutHandler() {
		public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
			throw new KeepAliveRequestTimeoutException("A keep-alive response message was not received within "
					+ filter.getRequestTimeout() + " second(s).");
		}
	};
	KeepAliveRequestTimeoutHandler CLOSE = new KeepAliveRequestTimeoutHandler() {
		private final Logger LOGGER = LoggerFactory.getLogger(KeepAliveFilter.class);

		public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
			this.LOGGER.warn(
					"Closing the session because a keep-alive response message was not received within {} second(s).",
					Integer.valueOf(filter.getRequestTimeout()));
			session.closeNow();
		}
	};
	KeepAliveRequestTimeoutHandler DEAF_SPEAKER = new KeepAliveRequestTimeoutHandler() {
		public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
			throw new Error("Shouldn\'t be invoked.  Please file a bug report.");
		}
	};

	void keepAliveRequestTimedOut(KeepAliveFilter arg0, IoSession arg1) throws Exception;
}