/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

public class AvailablePortFinder {
	public static final int MIN_PORT_NUMBER = 1;
	public static final int MAX_PORT_NUMBER = 49151;

	public static Set<Integer> getAvailablePorts() {
		return getAvailablePorts(1, '?');
	}

	public static int getNextAvailable() {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(0);
			int ioe = serverSocket.getLocalPort();
			serverSocket.close();
			return ioe;
		} catch (IOException arg1) {
			throw new NoSuchElementException(arg1.getMessage());
		}
	}

	public static int getNextAvailable(int fromPort) {
		if (fromPort >= 1 && fromPort <= '?') {
			for (int i = fromPort; i <= '?'; ++i) {
				if (available(i)) {
					return i;
				}
			}

			throw new NoSuchElementException("Could not find an available port above " + fromPort);
		} else {
			throw new IllegalArgumentException("Invalid start port: " + fromPort);
		}
	}

	public static boolean available(int port) {
		if (port >= 1 && port <= '?') {
			ServerSocket ss = null;
			DatagramSocket ds = null;

			try {
				ss = new ServerSocket(port);
				ss.setReuseAddress(true);
				ds = new DatagramSocket(port);
				ds.setReuseAddress(true);
				boolean e = true;
				return e;
			} catch (IOException arg12) {
				;
			} finally {
				if (ds != null) {
					ds.close();
				}

				if (ss != null) {
					try {
						ss.close();
					} catch (IOException arg11) {
						;
					}
				}

			}

			return false;
		} else {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}
	}

	public static Set<Integer> getAvailablePorts(int fromPort, int toPort) {
		if (fromPort >= 1 && toPort <= '?' && fromPort <= toPort) {
			TreeSet result = new TreeSet();

			for (int i = fromPort; i <= toPort; ++i) {
				ServerSocket s = null;

				try {
					s = new ServerSocket(i);
					result.add(Integer.valueOf(i));
				} catch (IOException arg13) {
					;
				} finally {
					if (s != null) {
						try {
							s.close();
						} catch (IOException arg12) {
							;
						}
					}

				}
			}

			return result;
		} else {
			throw new IllegalArgumentException("Invalid port range: " + fromPort + " ~ " + toPort);
		}
	}
}