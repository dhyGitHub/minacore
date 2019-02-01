/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import java.net.SocketAddress;
import org.apache.mina.core.session.IoSession;

public interface IoSessionRecycler {
	IoSessionRecycler NOOP = new IoSessionRecycler() {
		public void put(IoSession session) {
		}

		public IoSession recycle(SocketAddress remoteAddress) {
			return null;
		}

		public void remove(IoSession session) {
		}
	};

	void put(IoSession arg0);

	IoSession recycle(SocketAddress arg0);

	void remove(IoSession arg0);
}