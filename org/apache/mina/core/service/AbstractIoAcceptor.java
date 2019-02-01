/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.service;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.service.AbstractIoService.ServiceOperationFuture;
import org.apache.mina.core.session.IoSessionConfig;

public abstract class AbstractIoAcceptor extends AbstractIoService implements IoAcceptor {
	private final List<SocketAddress> defaultLocalAddresses = new ArrayList();
	private final List<SocketAddress> unmodifiableDefaultLocalAddresses;
	private final Set<SocketAddress> boundAddresses;
	private boolean disconnectOnUnbind;
	protected final Object bindLock;

	protected AbstractIoAcceptor(IoSessionConfig sessionConfig, Executor executor) {
		super(sessionConfig, executor);
		this.unmodifiableDefaultLocalAddresses = Collections.unmodifiableList(this.defaultLocalAddresses);
		this.boundAddresses = new HashSet();
		this.disconnectOnUnbind = true;
		this.bindLock = new Object();
		this.defaultLocalAddresses.add((Object) null);
	}

	public SocketAddress getLocalAddress() {
		Set localAddresses = this.getLocalAddresses();
		return localAddresses.isEmpty() ? null : (SocketAddress) localAddresses.iterator().next();
	}

	public final Set<SocketAddress> getLocalAddresses() {
		HashSet localAddresses = new HashSet();
		Set arg1 = this.boundAddresses;
		synchronized (this.boundAddresses) {
			localAddresses.addAll(this.boundAddresses);
			return localAddresses;
		}
	}

	public SocketAddress getDefaultLocalAddress() {
		return this.defaultLocalAddresses.isEmpty() ? null
				: (SocketAddress) this.defaultLocalAddresses.iterator().next();
	}

	public final void setDefaultLocalAddress(SocketAddress localAddress) {
		this.setDefaultLocalAddresses(localAddress, new SocketAddress[0]);
	}

	public final List<SocketAddress> getDefaultLocalAddresses() {
		return this.unmodifiableDefaultLocalAddresses;
	}

	public final void setDefaultLocalAddresses(List<? extends SocketAddress> localAddresses) {
		if (localAddresses == null) {
			throw new IllegalArgumentException("localAddresses");
		} else {
			this.setDefaultLocalAddresses((Iterable) localAddresses);
		}
	}

	public final void setDefaultLocalAddresses(Iterable<? extends SocketAddress> localAddresses) {
		if (localAddresses == null) {
			throw new IllegalArgumentException("localAddresses");
		} else {
			Object arg1 = this.bindLock;
			synchronized (this.bindLock) {
				Set arg2 = this.boundAddresses;
				synchronized (this.boundAddresses) {
					if (!this.boundAddresses.isEmpty()) {
						throw new IllegalStateException("localAddress can\'t be set while the acceptor is bound.");
					}

					ArrayList newLocalAddresses = new ArrayList();
					Iterator i$ = localAddresses.iterator();

					while (i$.hasNext()) {
						SocketAddress a = (SocketAddress) i$.next();
						this.checkAddressType(a);
						newLocalAddresses.add(a);
					}

					if (newLocalAddresses.isEmpty()) {
						throw new IllegalArgumentException("empty localAddresses");
					}

					this.defaultLocalAddresses.clear();
					this.defaultLocalAddresses.addAll(newLocalAddresses);
				}

			}
		}
	}

	public final void setDefaultLocalAddresses(SocketAddress firstLocalAddress, SocketAddress... otherLocalAddresses) {
		if (otherLocalAddresses == null) {
			otherLocalAddresses = new SocketAddress[0];
		}

		ArrayList newLocalAddresses = new ArrayList(otherLocalAddresses.length + 1);
		newLocalAddresses.add(firstLocalAddress);
		SocketAddress[] arr$ = otherLocalAddresses;
		int len$ = otherLocalAddresses.length;

		for (int i$ = 0; i$ < len$; ++i$) {
			SocketAddress a = arr$[i$];
			newLocalAddresses.add(a);
		}

		this.setDefaultLocalAddresses((Iterable) newLocalAddresses);
	}

	public final boolean isCloseOnDeactivation() {
		return this.disconnectOnUnbind;
	}

	public final void setCloseOnDeactivation(boolean disconnectClientsOnUnbind) {
		this.disconnectOnUnbind = disconnectClientsOnUnbind;
	}

	public final void bind() throws IOException {
		this.bind((Iterable) this.getDefaultLocalAddresses());
	}

	public final void bind(SocketAddress localAddress) throws IOException {
		if (localAddress == null) {
			throw new IllegalArgumentException("localAddress");
		} else {
			ArrayList localAddresses = new ArrayList(1);
			localAddresses.add(localAddress);
			this.bind((Iterable) localAddresses);
		}
	}

	public final void bind(SocketAddress... addresses) throws IOException {
		if (addresses != null && addresses.length != 0) {
			ArrayList localAddresses = new ArrayList(2);
			SocketAddress[] arr$ = addresses;
			int len$ = addresses.length;

			for (int i$ = 0; i$ < len$; ++i$) {
				SocketAddress address = arr$[i$];
				localAddresses.add(address);
			}

			this.bind((Iterable) localAddresses);
		} else {
			this.bind((Iterable) this.getDefaultLocalAddresses());
		}
	}

	public final void bind(SocketAddress firstLocalAddress, SocketAddress... addresses) throws IOException {
		if (firstLocalAddress == null) {
			this.bind((Iterable) this.getDefaultLocalAddresses());
		}

		if (addresses != null && addresses.length != 0) {
			ArrayList localAddresses = new ArrayList(2);
			localAddresses.add(firstLocalAddress);
			SocketAddress[] arr$ = addresses;
			int len$ = addresses.length;

			for (int i$ = 0; i$ < len$; ++i$) {
				SocketAddress address = arr$[i$];
				localAddresses.add(address);
			}

			this.bind((Iterable) localAddresses);
		} else {
			this.bind((Iterable) this.getDefaultLocalAddresses());
		}
	}

	public final void bind(Iterable<? extends SocketAddress> localAddresses) throws IOException {
		if (this.isDisposing()) {
			throw new IllegalStateException("The Accpetor disposed is being disposed.");
		} else if (localAddresses == null) {
			throw new IllegalArgumentException("localAddresses");
		} else {
			ArrayList localAddressesCopy = new ArrayList();
			Iterator activate = localAddresses.iterator();

			while (activate.hasNext()) {
				SocketAddress a = (SocketAddress) activate.next();
				this.checkAddressType(a);
				localAddressesCopy.add(a);
			}

			if (localAddressesCopy.isEmpty()) {
				throw new IllegalArgumentException("localAddresses is empty.");
			} else {
				boolean activate1 = false;
				Object a1 = this.bindLock;
				synchronized (this.bindLock) {
					Set e = this.boundAddresses;
					synchronized (this.boundAddresses) {
						if (this.boundAddresses.isEmpty()) {
							activate1 = true;
						}
					}

					if (this.getHandler() == null) {
						throw new IllegalStateException("handler is not set.");
					}

					try {
						e = this.bindInternal(localAddressesCopy);
						Set arg5 = this.boundAddresses;
						synchronized (this.boundAddresses) {
							this.boundAddresses.addAll(e);
						}
					} catch (IOException arg9) {
						throw arg9;
					} catch (RuntimeException arg10) {
						throw arg10;
					} catch (Exception arg11) {
						throw new RuntimeIoException("Failed to bind to: " + this.getLocalAddresses(), arg11);
					}
				}

				if (activate1) {
					this.getListeners().fireServiceActivated();
				}

			}
		}
	}

	public final void unbind() {
		this.unbind((Iterable) this.getLocalAddresses());
	}

	public final void unbind(SocketAddress localAddress) {
		if (localAddress == null) {
			throw new IllegalArgumentException("localAddress");
		} else {
			ArrayList localAddresses = new ArrayList(1);
			localAddresses.add(localAddress);
			this.unbind((Iterable) localAddresses);
		}
	}

	public final void unbind(SocketAddress firstLocalAddress, SocketAddress... otherLocalAddresses) {
		if (firstLocalAddress == null) {
			throw new IllegalArgumentException("firstLocalAddress");
		} else if (otherLocalAddresses == null) {
			throw new IllegalArgumentException("otherLocalAddresses");
		} else {
			ArrayList localAddresses = new ArrayList();
			localAddresses.add(firstLocalAddress);
			Collections.addAll(localAddresses, otherLocalAddresses);
			this.unbind((Iterable) localAddresses);
		}
	}

	public final void unbind(Iterable<? extends SocketAddress> localAddresses) {
		if (localAddresses == null) {
			throw new IllegalArgumentException("localAddresses");
		} else {
			boolean deactivate = false;
			Object arg2 = this.bindLock;
			synchronized (this.bindLock) {
				label70: {
					Set arg3 = this.boundAddresses;
					synchronized (this.boundAddresses) {
						if (!this.boundAddresses.isEmpty()) {
							ArrayList localAddressesCopy = new ArrayList();
							int specifiedAddressCount = 0;
							Iterator e = localAddresses.iterator();

							while (e.hasNext()) {
								SocketAddress a = (SocketAddress) e.next();
								++specifiedAddressCount;
								if (a != null && this.boundAddresses.contains(a)) {
									localAddressesCopy.add(a);
								}
							}

							if (specifiedAddressCount == 0) {
								throw new IllegalArgumentException("localAddresses is empty.");
							}

							if (!localAddressesCopy.isEmpty()) {
								try {
									this.unbind0(localAddressesCopy);
								} catch (RuntimeException arg10) {
									throw arg10;
								} catch (Exception arg11) {
									throw new RuntimeIoException("Failed to unbind from: " + this.getLocalAddresses(),
											arg11);
								}

								this.boundAddresses.removeAll(localAddressesCopy);
								if (this.boundAddresses.isEmpty()) {
									deactivate = true;
								}
							}
							break label70;
						}
					}

					return;
				}
			}

			if (deactivate) {
				this.getListeners().fireServiceDeactivated();
			}

		}
	}

	protected abstract Set<SocketAddress> bindInternal(List<? extends SocketAddress> arg0) throws Exception;

	protected abstract void unbind0(List<? extends SocketAddress> arg0) throws Exception;

	public String toString() {
		TransportMetadata m = this.getTransportMetadata();
		return '(' + m.getProviderName() + ' ' + m.getName() + " acceptor: " + (this.isActive() ? "localAddress(es): "
				+ this.getLocalAddresses() + ", managedSessionCount: " + this.getManagedSessionCount() : "not bound")
				+ ')';
	}

	private void checkAddressType(SocketAddress a) {
		if (a != null && !this.getTransportMetadata().getAddressType().isAssignableFrom(a.getClass())) {
			throw new IllegalArgumentException("localAddress type: " + a.getClass().getSimpleName() + " (expected: "
					+ this.getTransportMetadata().getAddressType().getSimpleName() + ")");
		}
	}

	public static class AcceptorOperationFuture extends ServiceOperationFuture {
		private final List<SocketAddress> localAddresses;

		public AcceptorOperationFuture(List<? extends SocketAddress> localAddresses) {
			this.localAddresses = new ArrayList(localAddresses);
		}

		public final List<SocketAddress> getLocalAddresses() {
			return Collections.unmodifiableList(this.localAddresses);
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Acceptor operation : ");
			if (this.localAddresses != null) {
				boolean isFirst = true;

				SocketAddress address;
				for (Iterator i$ = this.localAddresses.iterator(); i$.hasNext(); sb.append(address)) {
					address = (SocketAddress) i$.next();
					if (isFirst) {
						isFirst = false;
					} else {
						sb.append(", ");
					}
				}
			}

			return sb.toString();
		}
	}
}