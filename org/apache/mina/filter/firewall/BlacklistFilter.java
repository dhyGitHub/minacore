/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.firewall;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.firewall.Subnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlacklistFilter extends IoFilterAdapter {
	private final List<Subnet> blacklist = new CopyOnWriteArrayList();
	private static final Logger LOGGER = LoggerFactory.getLogger(BlacklistFilter.class);

	public void setBlacklist(InetAddress[] addresses) {
		if (addresses == null) {
			throw new IllegalArgumentException("addresses");
		} else {
			this.blacklist.clear();

			for (int i = 0; i < addresses.length; ++i) {
				InetAddress addr = addresses[i];
				this.block(addr);
			}

		}
	}

	public void setSubnetBlacklist(Subnet[] subnets) {
		if (subnets == null) {
			throw new IllegalArgumentException("Subnets must not be null");
		} else {
			this.blacklist.clear();
			Subnet[] arr$ = subnets;
			int len$ = subnets.length;

			for (int i$ = 0; i$ < len$; ++i$) {
				Subnet subnet = arr$[i$];
				this.block(subnet);
			}

		}
	}

	public void setBlacklist(Iterable<InetAddress> addresses) {
		if (addresses == null) {
			throw new IllegalArgumentException("addresses");
		} else {
			this.blacklist.clear();
			Iterator i$ = addresses.iterator();

			while (i$.hasNext()) {
				InetAddress address = (InetAddress) i$.next();
				this.block(address);
			}

		}
	}

	public void setSubnetBlacklist(Iterable<Subnet> subnets) {
		if (subnets == null) {
			throw new IllegalArgumentException("Subnets must not be null");
		} else {
			this.blacklist.clear();
			Iterator i$ = subnets.iterator();

			while (i$.hasNext()) {
				Subnet subnet = (Subnet) i$.next();
				this.block(subnet);
			}

		}
	}

	public void block(InetAddress address) {
		if (address == null) {
			throw new IllegalArgumentException("Adress to block can not be null");
		} else {
			this.block(new Subnet(address, 32));
		}
	}

	public void block(Subnet subnet) {
		if (subnet == null) {
			throw new IllegalArgumentException("Subnet can not be null");
		} else {
			this.blacklist.add(subnet);
		}
	}

	public void unblock(InetAddress address) {
		if (address == null) {
			throw new IllegalArgumentException("Adress to unblock can not be null");
		} else {
			this.unblock(new Subnet(address, 32));
		}
	}

	public void unblock(Subnet subnet) {
		if (subnet == null) {
			throw new IllegalArgumentException("Subnet can not be null");
		} else {
			this.blacklist.remove(subnet);
		}
	}

	public void sessionCreated(NextFilter nextFilter, IoSession session) {
		if (!this.isBlocked(session)) {
			nextFilter.sessionCreated(session);
		} else {
			this.blockSession(session);
		}

	}

	public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		if (!this.isBlocked(session)) {
			nextFilter.sessionOpened(session);
		} else {
			this.blockSession(session);
		}

	}

	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		if (!this.isBlocked(session)) {
			nextFilter.sessionClosed(session);
		} else {
			this.blockSession(session);
		}

	}

	public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
		if (!this.isBlocked(session)) {
			nextFilter.sessionIdle(session, status);
		} else {
			this.blockSession(session);
		}

	}

	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) {
		if (!this.isBlocked(session)) {
			nextFilter.messageReceived(session, message);
		} else {
			this.blockSession(session);
		}

	}

	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		if (!this.isBlocked(session)) {
			nextFilter.messageSent(session, writeRequest);
		} else {
			this.blockSession(session);
		}

	}

	private void blockSession(IoSession session) {
		LOGGER.warn("Remote address in the blacklist; closing.");
		session.closeNow();
	}

	private boolean isBlocked(IoSession session) {
		SocketAddress remoteAddress = session.getRemoteAddress();
		if (remoteAddress instanceof InetSocketAddress) {
			InetAddress address = ((InetSocketAddress) remoteAddress).getAddress();
			Iterator i$ = this.blacklist.iterator();

			while (i$.hasNext()) {
				Subnet subnet = (Subnet) i$.next();
				if (subnet.inSubnet(address)) {
					return true;
				}
			}
		}

		return false;
	}
}