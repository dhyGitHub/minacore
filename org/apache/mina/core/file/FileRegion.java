/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.file;

import java.nio.channels.FileChannel;

public interface FileRegion {
	FileChannel getFileChannel();

	long getPosition();

	void update(long arg0);

	long getRemainingBytes();

	long getWrittenBytes();

	String getFilename();
}