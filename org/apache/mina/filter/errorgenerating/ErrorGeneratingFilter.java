/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.errorgenerating;

import java.util.Random;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorGeneratingFilter extends IoFilterAdapter {
	private int removeByteProbability = 0;
	private int insertByteProbability = 0;
	private int changeByteProbability = 0;
	private int removePduProbability = 0;
	private int duplicatePduProbability = 0;
	private int resendPduLasterProbability = 0;
	private int maxInsertByte = 10;
	private boolean manipulateWrites = false;
	private boolean manipulateReads = false;
	private Random rng = new Random();
	private final Logger logger = LoggerFactory.getLogger(ErrorGeneratingFilter.class);

	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		if (this.manipulateWrites) {
			if (((WriteRequest) writeRequest).getMessage() instanceof IoBuffer) {
				this.manipulateIoBuffer(session, (IoBuffer) ((WriteRequest) writeRequest).getMessage());
				IoBuffer buffer = this.insertBytesToNewIoBuffer(session,
						(IoBuffer) ((WriteRequest) writeRequest).getMessage());
				if (buffer != null) {
					writeRequest = new DefaultWriteRequest(buffer, ((WriteRequest) writeRequest).getFuture(),
							((WriteRequest) writeRequest).getDestination());
				}
			} else {
				if (this.duplicatePduProbability > this.rng.nextInt()) {
					nextFilter.filterWrite(session, (WriteRequest) writeRequest);
				}

				if (this.resendPduLasterProbability > this.rng.nextInt()) {
					;
				}

				if (this.removePduProbability > this.rng.nextInt()) {
					return;
				}
			}
		}

		nextFilter.filterWrite(session, (WriteRequest) writeRequest);
	}

	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		if (this.manipulateReads && message instanceof IoBuffer) {
			this.manipulateIoBuffer(session, (IoBuffer) message);
			IoBuffer buffer = this.insertBytesToNewIoBuffer(session, (IoBuffer) message);
			if (buffer != null) {
				message = buffer;
			}
		}

		nextFilter.messageReceived(session, message);
	}

	private IoBuffer insertBytesToNewIoBuffer(IoSession session, IoBuffer buffer) {
		if (this.insertByteProbability <= this.rng.nextInt(1000)) {
			return null;
		} else {
			this.logger.info(buffer.getHexDump());
			int pos = this.rng.nextInt(buffer.remaining()) - 1;
			int count = this.rng.nextInt(this.maxInsertByte - 1) + 1;
			IoBuffer newBuff = IoBuffer.allocate(buffer.remaining() + count);

			int i;
			for (i = 0; i < pos; ++i) {
				newBuff.put(buffer.get());
			}

			for (i = 0; i < count; ++i) {
				newBuff.put((byte) this.rng.nextInt(256));
			}

			while (buffer.remaining() > 0) {
				newBuff.put(buffer.get());
			}

			newBuff.flip();
			this.logger.info("Inserted " + count + " bytes.");
			this.logger.info(newBuff.getHexDump());
			return newBuff;
		}
	}

	private void manipulateIoBuffer(IoSession session, IoBuffer buffer) {
		int count;
		int pos;
		if (buffer.remaining() > 0 && this.removeByteProbability > this.rng.nextInt(1000)) {
			this.logger.info(buffer.getHexDump());
			count = this.rng.nextInt(buffer.remaining());
			int values = this.rng.nextInt(buffer.remaining() - count) + 1;
			if (values == buffer.remaining()) {
				values = buffer.remaining() - 1;
			}

			IoBuffer i = IoBuffer.allocate(buffer.remaining() - values);

			for (pos = 0; pos < count; ++pos) {
				i.put(buffer.get());
			}

			buffer.skip(values);

			while (i.remaining() > 0) {
				i.put(buffer.get());
			}

			i.flip();
			buffer.rewind();
			buffer.put(i);
			buffer.flip();
			this.logger.info("Removed " + values + " bytes at position " + count + ".");
			this.logger.info(buffer.getHexDump());
		}

		if (buffer.remaining() > 0 && this.changeByteProbability > this.rng.nextInt(1000)) {
			this.logger.info(buffer.getHexDump());
			count = this.rng.nextInt(buffer.remaining() - 1) + 1;
			byte[] arg6 = new byte[count];
			this.rng.nextBytes(arg6);

			for (int arg7 = 0; arg7 < arg6.length; ++arg7) {
				pos = this.rng.nextInt(buffer.remaining());
				buffer.put(pos, arg6[arg7]);
			}

			this.logger.info("Modified " + count + " bytes.");
			this.logger.info(buffer.getHexDump());
		}

	}

	public int getChangeByteProbability() {
		return this.changeByteProbability;
	}

	public void setChangeByteProbability(int changeByteProbability) {
		this.changeByteProbability = changeByteProbability;
	}

	public int getDuplicatePduProbability() {
		return this.duplicatePduProbability;
	}

	public void setDuplicatePduProbability(int duplicatePduProbability) {
		this.duplicatePduProbability = duplicatePduProbability;
	}

	public int getInsertByteProbability() {
		return this.insertByteProbability;
	}

	public void setInsertByteProbability(int insertByteProbability) {
		this.insertByteProbability = insertByteProbability;
	}

	public boolean isManipulateReads() {
		return this.manipulateReads;
	}

	public void setManipulateReads(boolean manipulateReads) {
		this.manipulateReads = manipulateReads;
	}

	public boolean isManipulateWrites() {
		return this.manipulateWrites;
	}

	public void setManipulateWrites(boolean manipulateWrites) {
		this.manipulateWrites = manipulateWrites;
	}

	public int getRemoveByteProbability() {
		return this.removeByteProbability;
	}

	public void setRemoveByteProbability(int removeByteProbability) {
		this.removeByteProbability = removeByteProbability;
	}

	public int getRemovePduProbability() {
		return this.removePduProbability;
	}

	public void setRemovePduProbability(int removePduProbability) {
		this.removePduProbability = removePduProbability;
	}

	public int getResendPduLasterProbability() {
		return this.resendPduLasterProbability;
	}

	public void setResendPduLasterProbability(int resendPduLasterProbability) {
		this.resendPduLasterProbability = resendPduLasterProbability;
	}

	public int getMaxInsertByte() {
		return this.maxInsertByte;
	}

	public void setMaxInsertByte(int maxInsertByte) {
		this.maxInsertByte = maxInsertByte;
	}
}