/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.stream;

import java.io.IOException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.filter.stream.AbstractStreamWriteFilter;

public class FileRegionWriteFilter extends AbstractStreamWriteFilter<FileRegion> {
	protected Class<FileRegion> getMessageClass() {
		return FileRegion.class;
	}

	protected IoBuffer getNextBuffer(FileRegion fileRegion) throws IOException {
		if (fileRegion.getRemainingBytes() <= 0L) {
			return null;
		} else {
			int bufferSize = (int) Math.min((long) this.getWriteBufferSize(), fileRegion.getRemainingBytes());
			IoBuffer buffer = IoBuffer.allocate(bufferSize);
			int bytesRead = fileRegion.getFileChannel().read(buffer.buf(), fileRegion.getPosition());
			fileRegion.update((long) bytesRead);
			buffer.flip();
			return buffer;
		}
	}
}