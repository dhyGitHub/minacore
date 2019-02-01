/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.buffer;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.EnumSet;
import java.util.Set;
import org.apache.mina.core.buffer.IoBufferAllocator;
import org.apache.mina.core.buffer.SimpleBufferAllocator;

public abstract class IoBuffer implements Comparable<IoBuffer> {
	private static IoBufferAllocator allocator = new SimpleBufferAllocator();
	private static boolean useDirectBuffer = false;

	public static IoBufferAllocator getAllocator() {
		return allocator;
	}

	public static void setAllocator(IoBufferAllocator newAllocator) {
		if (newAllocator == null) {
			throw new IllegalArgumentException("allocator");
		} else {
			IoBufferAllocator oldAllocator = allocator;
			allocator = newAllocator;
			if (null != oldAllocator) {
				oldAllocator.dispose();
			}

		}
	}

	public static boolean isUseDirectBuffer() {
		return useDirectBuffer;
	}

	public static void setUseDirectBuffer(boolean useDirectBuffer) {
		IoBuffer.useDirectBuffer = useDirectBuffer;
	}

	public static IoBuffer allocate(int capacity) {
		return allocate(capacity, useDirectBuffer);
	}

	public static IoBuffer allocate(int capacity, boolean useDirectBuffer) {
		if (capacity < 0) {
			throw new IllegalArgumentException("capacity: " + capacity);
		} else {
			return allocator.allocate(capacity, useDirectBuffer);
		}
	}

	public static IoBuffer wrap(ByteBuffer nioBuffer) {
		return allocator.wrap(nioBuffer);
	}

	public static IoBuffer wrap(byte[] byteArray) {
		return wrap(ByteBuffer.wrap(byteArray));
	}

	public static IoBuffer wrap(byte[] byteArray, int offset, int length) {
		return wrap(ByteBuffer.wrap(byteArray, offset, length));
	}

	protected static int normalizeCapacity(int requestedCapacity) {
		if (requestedCapacity < 0) {
			return Integer.MAX_VALUE;
		} else {
			int newCapacity = Integer.highestOneBit(requestedCapacity);
			newCapacity <<= newCapacity < requestedCapacity ? 1 : 0;
			return newCapacity < 0 ? Integer.MAX_VALUE : newCapacity;
		}
	}

	public abstract void free();

	public abstract ByteBuffer buf();

	public abstract boolean isDirect();

	public abstract boolean isDerived();

	public abstract boolean isReadOnly();

	public abstract int minimumCapacity();

	public abstract IoBuffer minimumCapacity(int arg0);

	public abstract int capacity();

	public abstract IoBuffer capacity(int arg0);

	public abstract boolean isAutoExpand();

	public abstract IoBuffer setAutoExpand(boolean arg0);

	public abstract boolean isAutoShrink();

	public abstract IoBuffer setAutoShrink(boolean arg0);

	public abstract IoBuffer expand(int arg0);

	public abstract IoBuffer expand(int arg0, int arg1);

	public abstract IoBuffer shrink();

	public abstract int position();

	public abstract IoBuffer position(int arg0);

	public abstract int limit();

	public abstract IoBuffer limit(int arg0);

	public abstract IoBuffer mark();

	public abstract int markValue();

	public abstract IoBuffer reset();

	public abstract IoBuffer clear();

	public abstract IoBuffer sweep();

	public abstract IoBuffer sweep(byte arg0);

	public abstract IoBuffer flip();

	public abstract IoBuffer rewind();

	public abstract int remaining();

	public abstract boolean hasRemaining();

	public abstract IoBuffer duplicate();

	public abstract IoBuffer slice();

	public abstract IoBuffer asReadOnlyBuffer();

	public abstract boolean hasArray();

	public abstract byte[] array();

	public abstract int arrayOffset();

	public abstract byte get();

	public abstract short getUnsigned();

	public abstract IoBuffer put(byte arg0);

	public abstract byte get(int arg0);

	public abstract short getUnsigned(int arg0);

	public abstract IoBuffer put(int arg0, byte arg1);

	public abstract IoBuffer get(byte[] arg0, int arg1, int arg2);

	public abstract IoBuffer get(byte[] arg0);

	public abstract IoBuffer getSlice(int arg0, int arg1);

	public abstract IoBuffer getSlice(int arg0);

	public abstract IoBuffer put(ByteBuffer arg0);

	public abstract IoBuffer put(IoBuffer arg0);

	public abstract IoBuffer put(byte[] arg0, int arg1, int arg2);

	public abstract IoBuffer put(byte[] arg0);

	public abstract IoBuffer compact();

	public abstract ByteOrder order();

	public abstract IoBuffer order(ByteOrder arg0);

	public abstract char getChar();

	public abstract IoBuffer putChar(char arg0);

	public abstract char getChar(int arg0);

	public abstract IoBuffer putChar(int arg0, char arg1);

	public abstract CharBuffer asCharBuffer();

	public abstract short getShort();

	public abstract int getUnsignedShort();

	public abstract IoBuffer putShort(short arg0);

	public abstract short getShort(int arg0);

	public abstract int getUnsignedShort(int arg0);

	public abstract IoBuffer putShort(int arg0, short arg1);

	public abstract ShortBuffer asShortBuffer();

	public abstract int getInt();

	public abstract long getUnsignedInt();

	public abstract int getMediumInt();

	public abstract int getUnsignedMediumInt();

	public abstract int getMediumInt(int arg0);

	public abstract int getUnsignedMediumInt(int arg0);

	public abstract IoBuffer putMediumInt(int arg0);

	public abstract IoBuffer putMediumInt(int arg0, int arg1);

	public abstract IoBuffer putInt(int arg0);

	public abstract IoBuffer putUnsigned(byte arg0);

	public abstract IoBuffer putUnsigned(int arg0, byte arg1);

	public abstract IoBuffer putUnsigned(short arg0);

	public abstract IoBuffer putUnsigned(int arg0, short arg1);

	public abstract IoBuffer putUnsigned(int arg0);

	public abstract IoBuffer putUnsigned(int arg0, int arg1);

	public abstract IoBuffer putUnsigned(long arg0);

	public abstract IoBuffer putUnsigned(int arg0, long arg1);

	public abstract IoBuffer putUnsignedInt(byte arg0);

	public abstract IoBuffer putUnsignedInt(int arg0, byte arg1);

	public abstract IoBuffer putUnsignedInt(short arg0);

	public abstract IoBuffer putUnsignedInt(int arg0, short arg1);

	public abstract IoBuffer putUnsignedInt(int arg0);

	public abstract IoBuffer putUnsignedInt(int arg0, int arg1);

	public abstract IoBuffer putUnsignedInt(long arg0);

	public abstract IoBuffer putUnsignedInt(int arg0, long arg1);

	public abstract IoBuffer putUnsignedShort(byte arg0);

	public abstract IoBuffer putUnsignedShort(int arg0, byte arg1);

	public abstract IoBuffer putUnsignedShort(short arg0);

	public abstract IoBuffer putUnsignedShort(int arg0, short arg1);

	public abstract IoBuffer putUnsignedShort(int arg0);

	public abstract IoBuffer putUnsignedShort(int arg0, int arg1);

	public abstract IoBuffer putUnsignedShort(long arg0);

	public abstract IoBuffer putUnsignedShort(int arg0, long arg1);

	public abstract int getInt(int arg0);

	public abstract long getUnsignedInt(int arg0);

	public abstract IoBuffer putInt(int arg0, int arg1);

	public abstract IntBuffer asIntBuffer();

	public abstract long getLong();

	public abstract IoBuffer putLong(long arg0);

	public abstract long getLong(int arg0);

	public abstract IoBuffer putLong(int arg0, long arg1);

	public abstract LongBuffer asLongBuffer();

	public abstract float getFloat();

	public abstract IoBuffer putFloat(float arg0);

	public abstract float getFloat(int arg0);

	public abstract IoBuffer putFloat(int arg0, float arg1);

	public abstract FloatBuffer asFloatBuffer();

	public abstract double getDouble();

	public abstract IoBuffer putDouble(double arg0);

	public abstract double getDouble(int arg0);

	public abstract IoBuffer putDouble(int arg0, double arg1);

	public abstract DoubleBuffer asDoubleBuffer();

	public abstract InputStream asInputStream();

	public abstract OutputStream asOutputStream();

	public abstract String getHexDump();

	public abstract String getHexDump(int arg0);

	public abstract String getString(CharsetDecoder arg0) throws CharacterCodingException;

	public abstract String getString(int arg0, CharsetDecoder arg1) throws CharacterCodingException;

	public abstract IoBuffer putString(CharSequence arg0, CharsetEncoder arg1) throws CharacterCodingException;

	public abstract IoBuffer putString(CharSequence arg0, int arg1, CharsetEncoder arg2)
			throws CharacterCodingException;

	public abstract String getPrefixedString(CharsetDecoder arg0) throws CharacterCodingException;

	public abstract String getPrefixedString(int arg0, CharsetDecoder arg1) throws CharacterCodingException;

	public abstract IoBuffer putPrefixedString(CharSequence arg0, CharsetEncoder arg1) throws CharacterCodingException;

	public abstract IoBuffer putPrefixedString(CharSequence arg0, int arg1, CharsetEncoder arg2)
			throws CharacterCodingException;

	public abstract IoBuffer putPrefixedString(CharSequence arg0, int arg1, int arg2, CharsetEncoder arg3)
			throws CharacterCodingException;

	public abstract IoBuffer putPrefixedString(CharSequence arg0, int arg1, int arg2, byte arg3, CharsetEncoder arg4)
			throws CharacterCodingException;

	public abstract Object getObject() throws ClassNotFoundException;

	public abstract Object getObject(ClassLoader arg0) throws ClassNotFoundException;

	public abstract IoBuffer putObject(Object arg0);

	public abstract boolean prefixedDataAvailable(int arg0);

	public abstract boolean prefixedDataAvailable(int arg0, int arg1);

	public abstract int indexOf(byte arg0);

	public abstract IoBuffer skip(int arg0);

	public abstract IoBuffer fill(byte arg0, int arg1);

	public abstract IoBuffer fillAndReset(byte arg0, int arg1);

	public abstract IoBuffer fill(int arg0);

	public abstract IoBuffer fillAndReset(int arg0);

	public abstract <E extends Enum<E>> E getEnum(Class<E> arg0);

	public abstract <E extends Enum<E>> E getEnum(int arg0, Class<E> arg1);

	public abstract <E extends Enum<E>> E getEnumShort(Class<E> arg0);

	public abstract <E extends Enum<E>> E getEnumShort(int arg0, Class<E> arg1);

	public abstract <E extends Enum<E>> E getEnumInt(Class<E> arg0);

	public abstract <E extends Enum<E>> E getEnumInt(int arg0, Class<E> arg1);

	public abstract IoBuffer putEnum(Enum<?> arg0);

	public abstract IoBuffer putEnum(int arg0, Enum<?> arg1);

	public abstract IoBuffer putEnumShort(Enum<?> arg0);

	public abstract IoBuffer putEnumShort(int arg0, Enum<?> arg1);

	public abstract IoBuffer putEnumInt(Enum<?> arg0);

	public abstract IoBuffer putEnumInt(int arg0, Enum<?> arg1);

	public abstract <E extends Enum<E>> EnumSet<E> getEnumSet(Class<E> arg0);

	public abstract <E extends Enum<E>> EnumSet<E> getEnumSet(int arg0, Class<E> arg1);

	public abstract <E extends Enum<E>> EnumSet<E> getEnumSetShort(Class<E> arg0);

	public abstract <E extends Enum<E>> EnumSet<E> getEnumSetShort(int arg0, Class<E> arg1);

	public abstract <E extends Enum<E>> EnumSet<E> getEnumSetInt(Class<E> arg0);

	public abstract <E extends Enum<E>> EnumSet<E> getEnumSetInt(int arg0, Class<E> arg1);

	public abstract <E extends Enum<E>> EnumSet<E> getEnumSetLong(Class<E> arg0);

	public abstract <E extends Enum<E>> EnumSet<E> getEnumSetLong(int arg0, Class<E> arg1);

	public abstract <E extends Enum<E>> IoBuffer putEnumSet(Set<E> arg0);

	public abstract <E extends Enum<E>> IoBuffer putEnumSet(int arg0, Set<E> arg1);

	public abstract <E extends Enum<E>> IoBuffer putEnumSetShort(Set<E> arg0);

	public abstract <E extends Enum<E>> IoBuffer putEnumSetShort(int arg0, Set<E> arg1);

	public abstract <E extends Enum<E>> IoBuffer putEnumSetInt(Set<E> arg0);

	public abstract <E extends Enum<E>> IoBuffer putEnumSetInt(int arg0, Set<E> arg1);

	public abstract <E extends Enum<E>> IoBuffer putEnumSetLong(Set<E> arg0);

	public abstract <E extends Enum<E>> IoBuffer putEnumSetLong(int arg0, Set<E> arg1);
}