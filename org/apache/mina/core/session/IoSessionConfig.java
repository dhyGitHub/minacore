/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import org.apache.mina.core.session.IdleStatus;

public interface IoSessionConfig {
	int getReadBufferSize();

	void setReadBufferSize(int arg0);

	int getMinReadBufferSize();

	void setMinReadBufferSize(int arg0);

	int getMaxReadBufferSize();

	void setMaxReadBufferSize(int arg0);

	int getThroughputCalculationInterval();

	long getThroughputCalculationIntervalInMillis();

	void setThroughputCalculationInterval(int arg0);

	int getIdleTime(IdleStatus arg0);

	long getIdleTimeInMillis(IdleStatus arg0);

	void setIdleTime(IdleStatus arg0, int arg1);

	int getReaderIdleTime();

	long getReaderIdleTimeInMillis();

	void setReaderIdleTime(int arg0);

	int getWriterIdleTime();

	long getWriterIdleTimeInMillis();

	void setWriterIdleTime(int arg0);

	int getBothIdleTime();

	long getBothIdleTimeInMillis();

	void setBothIdleTime(int arg0);

	int getWriteTimeout();

	long getWriteTimeoutInMillis();

	void setWriteTimeout(int arg0);

	boolean isUseReadOperation();

	void setUseReadOperation(boolean arg0);

	void setAll(IoSessionConfig arg0);
}