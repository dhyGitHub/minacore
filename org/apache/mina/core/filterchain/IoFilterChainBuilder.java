/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.filterchain;

import org.apache.mina.core.filterchain.IoFilterChain;

public interface IoFilterChainBuilder {
	IoFilterChainBuilder NOOP = new IoFilterChainBuilder() {
		public void buildFilterChain(IoFilterChain chain) throws Exception {
		}

		public String toString() {
			return "NOOP";
		}
	};

	void buildFilterChain(IoFilterChain arg0) throws Exception;
}