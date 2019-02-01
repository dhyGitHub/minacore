/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.firewall;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public class Subnet {
	private static final int IP_MASK_V4 = Integer.MIN_VALUE;
	private static final long IP_MASK_V6 = Long.MIN_VALUE;
	private static final int BYTE_MASK = 255;
	private InetAddress subnet;
	private int subnetInt;
	private long subnetLong;
	private long subnetMask;
	private int suffix;

	public Subnet(InetAddress subnet, int mask) {
		if (subnet == null) {
			throw new IllegalArgumentException("Subnet address can not be null");
		} else if (!(subnet instanceof Inet4Address) && !(subnet instanceof Inet6Address)) {
			throw new IllegalArgumentException("Only IPv4 and IPV6 supported");
		} else {
			if (subnet instanceof Inet4Address) {
				if (mask < 0 || mask > 32) {
					throw new IllegalArgumentException(
							"Mask has to be an integer between 0 and 32 for an IPV4 address");
				}

				this.subnet = subnet;
				this.subnetInt = this.toInt(subnet);
				this.suffix = mask;
				this.subnetMask = (long) (Integer.MIN_VALUE >> mask - 1);
			} else {
				if (mask < 0 || mask > 128) {
					throw new IllegalArgumentException(
							"Mask has to be an integer between 0 and 128 for an IPV6 address");
				}

				this.subnet = subnet;
				this.subnetLong = this.toLong(subnet);
				this.suffix = mask;
				this.subnetMask = Long.MIN_VALUE >> mask - 1;
			}

		}
	}

	private int toInt(InetAddress inetAddress) {
		byte[] address = inetAddress.getAddress();
		int result = 0;

		for (int i = 0; i < address.length; ++i) {
			result <<= 8;
			result |= address[i] & 255;
		}

		return result;
	}

	private long toLong(InetAddress inetAddress) {
		byte[] address = inetAddress.getAddress();
		long result = 0L;

		for (int i = 0; i < address.length; ++i) {
			result <<= 8;
			result |= (long) (address[i] & 255);
		}

		return result;
	}

	private long toSubnet(InetAddress address) {
		return address instanceof Inet4Address ? (long) (this.toInt(address) & (int) this.subnetMask)
				: this.toLong(address) & this.subnetMask;
	}

	public boolean inSubnet(InetAddress address) {
		return address.isAnyLocalAddress() ? true
				: (address instanceof Inet4Address ? (int) this.toSubnet(address) == this.subnetInt
						: this.toSubnet(address) == this.subnetLong);
	}

	public String toString() {
		return this.subnet.getHostAddress() + "/" + this.suffix;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Subnet)) {
			return false;
		} else {
			Subnet other = (Subnet) obj;
			return other.subnetInt == this.subnetInt && other.suffix == this.suffix;
		}
	}
}