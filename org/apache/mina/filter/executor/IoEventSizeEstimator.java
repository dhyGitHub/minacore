/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.executor;

import org.apache.mina.core.session.IoEvent;

public interface IoEventSizeEstimator {
	int estimateSize(IoEvent arg0);
}