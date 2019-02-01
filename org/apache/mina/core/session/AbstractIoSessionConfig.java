/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSessionConfig;

public abstract class AbstractIoSessionConfig implements IoSessionConfig {
	private int minReadBufferSize = 64;
	private int readBufferSize = 2048;
	private int maxReadBufferSize = 65536;
	private int idleTimeForRead;
	private int idleTimeForWrite;
	private int idleTimeForBoth;
	private int writeTimeout = 60;
	private boolean useReadOperation;
	private int throughputCalculationInterval = 3;

	public void setAll(IoSessionConfig config) {
		if (config == null) {
			throw new IllegalArgumentException("config");
		} else {
			this.setReadBufferSize(config.getReadBufferSize());
			this.setMinReadBufferSize(config.getMinReadBufferSize());
			this.setMaxReadBufferSize(config.getMaxReadBufferSize());
			this.setIdleTime(IdleStatus.BOTH_IDLE, config.getIdleTime(IdleStatus.BOTH_IDLE));
			this.setIdleTime(IdleStatus.READER_IDLE, config.getIdleTime(IdleStatus.READER_IDLE));
			this.setIdleTime(IdleStatus.WRITER_IDLE, config.getIdleTime(IdleStatus.WRITER_IDLE));
			this.setWriteTimeout(config.getWriteTimeout());
			this.setUseReadOperation(config.isUseReadOperation());
			this.setThroughputCalculationInterval(config.getThroughputCalculationInterval());
		}
	}

	public int getReadBufferSize() {
		return this.readBufferSize;
	}

	public void setReadBufferSize(int readBufferSize) {
		if (readBufferSize <= 0) {
			throw new IllegalArgumentException("readBufferSize: " + readBufferSize + " (expected: 1+)");
		} else {
			this.readBufferSize = readBufferSize;
		}
	}

	public int getMinReadBufferSize() {
		return this.minReadBufferSize;
	}

	public void setMinReadBufferSize(int minReadBufferSize) {
		if (minReadBufferSize <= 0) {
			throw new IllegalArgumentException("minReadBufferSize: " + minReadBufferSize + " (expected: 1+)");
		} else if (minReadBufferSize > this.maxReadBufferSize) {
			throw new IllegalArgumentException("minReadBufferSize: " + minReadBufferSize + " (expected: smaller than "
					+ this.maxReadBufferSize + ')');
		} else {
			this.minReadBufferSize = minReadBufferSize;
		}
	}

	public int getMaxReadBufferSize() {
		return this.maxReadBufferSize;
	}

	public void setMaxReadBufferSize(int maxReadBufferSize) {
		if (maxReadBufferSize <= 0) {
			throw new IllegalArgumentException("maxReadBufferSize: " + maxReadBufferSize + " (expected: 1+)");
		} else if (maxReadBufferSize < this.minReadBufferSize) {
			throw new IllegalArgumentException("maxReadBufferSize: " + maxReadBufferSize + " (expected: greater than "
					+ this.minReadBufferSize + ')');
		} else {
			this.maxReadBufferSize = maxReadBufferSize;
		}
	}

	public int getIdleTime(IdleStatus status) {
		if (status == IdleStatus.BOTH_IDLE) {
			return this.idleTimeForBoth;
		} else if (status == IdleStatus.READER_IDLE) {
			return this.idleTimeForRead;
		} else if (status == IdleStatus.WRITER_IDLE) {
			return this.idleTimeForWrite;
		} else {
			throw new IllegalArgumentException("Unknown idle status: " + status);
		}
	}

	public long getIdleTimeInMillis(IdleStatus status) {
		return (long) this.getIdleTime(status) * 1000L;
	}

	public void setIdleTime(IdleStatus status, int idleTime) {
		if (idleTime < 0) {
			throw new IllegalArgumentException("Illegal idle time: " + idleTime);
		} else {
			if (status == IdleStatus.BOTH_IDLE) {
				this.idleTimeForBoth = idleTime;
			} else if (status == IdleStatus.READER_IDLE) {
				this.idleTimeForRead = idleTime;
			} else {
				if (status != IdleStatus.WRITER_IDLE) {
					throw new IllegalArgumentException("Unknown idle status: " + status);
				}

				this.idleTimeForWrite = idleTime;
			}

		}
	}

	public final int getBothIdleTime() {
		return this.getIdleTime(IdleStatus.BOTH_IDLE);
	}

	public final long getBothIdleTimeInMillis() {
		return this.getIdleTimeInMillis(IdleStatus.BOTH_IDLE);
	}

	public final int getReaderIdleTime() {
		return this.getIdleTime(IdleStatus.READER_IDLE);
	}

	public final long getReaderIdleTimeInMillis() {
		return this.getIdleTimeInMillis(IdleStatus.READER_IDLE);
	}

	public final int getWriterIdleTime() {
		return this.getIdleTime(IdleStatus.WRITER_IDLE);
	}

	public final long getWriterIdleTimeInMillis() {
		return this.getIdleTimeInMillis(IdleStatus.WRITER_IDLE);
	}

	public void setBothIdleTime(int idleTime) {
		this.setIdleTime(IdleStatus.BOTH_IDLE, idleTime);
	}

	public void setReaderIdleTime(int idleTime) {
		this.setIdleTime(IdleStatus.READER_IDLE, idleTime);
	}

	public void setWriterIdleTime(int idleTime) {
		this.setIdleTime(IdleStatus.WRITER_IDLE, idleTime);
	}

	public int getWriteTimeout() {
		return this.writeTimeout;
	}

	public long getWriteTimeoutInMillis() {
		return (long) this.writeTimeout * 1000L;
	}

	public void setWriteTimeout(int writeTimeout) {
		if (writeTimeout < 0) {
			throw new IllegalArgumentException("Illegal write timeout: " + writeTimeout);
		} else {
			this.writeTimeout = writeTimeout;
		}
	}

	public boolean isUseReadOperation() {
		return this.useReadOperation;
	}

	public void setUseReadOperation(boolean useReadOperation) {
		this.useReadOperation = useReadOperation;
	}

	public int getThroughputCalculationInterval() {
		return this.throughputCalculationInterval;
	}

	public void setThroughputCalculationInterval(int throughputCalculationInterval) {
		if (throughputCalculationInterval < 0) {
			throw new IllegalArgumentException("throughputCalculationInterval: " + throughputCalculationInterval);
		} else {
			this.throughputCalculationInterval = throughputCalculationInterval;
		}
	}

	public long getThroughputCalculationIntervalInMillis() {
		return (long) this.throughputCalculationInterval * 1000L;
	}
}