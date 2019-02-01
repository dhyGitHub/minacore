/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.file;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.apache.mina.core.file.DefaultFileRegion;

public class FilenameFileRegion extends DefaultFileRegion {
	private final File file;

	public FilenameFileRegion(File file, FileChannel channel) throws IOException {
		this(file, channel, 0L, file.length());
	}

	public FilenameFileRegion(File file, FileChannel channel, long position, long remainingBytes) {
		super(channel, position, remainingBytes);
		if (file == null) {
			throw new IllegalArgumentException("file can not be null");
		} else {
			this.file = file;
		}
	}

	public String getFilename() {
		return this.file.getAbsolutePath();
	}
}