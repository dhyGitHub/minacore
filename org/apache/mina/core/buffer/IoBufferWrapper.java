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
import org.apache.mina.core.buffer.IoBuffer;

public class IoBufferWrapper extends IoBuffer {
	private final IoBuffer buf;

	protected IoBufferWrapper(IoBuffer buf) {
		if (buf == null) {
			throw new IllegalArgumentException("buf");
		} else {
			this.buf = buf;
		}
	}

	public IoBuffer getParentBuffer() {
		return this.buf;
	}

	public boolean isDirect() {
		return this.buf.isDirect();
	}

	public ByteBuffer buf() {
		return this.buf.buf();
	}

	public int capacity() {
		return this.buf.capacity();
	}

	public int position() {
		return this.buf.position();
	}

	public IoBuffer position(int newPosition) {
		this.buf.position(newPosition);
		return this;
	}

	public int limit() {
		return this.buf.limit();
	}

	public IoBuffer limit(int newLimit) {
		this.buf.limit(newLimit);
		return this;
	}

	public IoBuffer mark() {
		this.buf.mark();
		return this;
	}

	public IoBuffer reset() {
		this.buf.reset();
		return this;
	}

	public IoBuffer clear() {
		this.buf.clear();
		return this;
	}

	public IoBuffer sweep() {
		this.buf.sweep();
		return this;
	}

	public IoBuffer sweep(byte value) {
		this.buf.sweep(value);
		return this;
	}

	public IoBuffer flip() {
		this.buf.flip();
		return this;
	}

	public IoBuffer rewind() {
		this.buf.rewind();
		return this;
	}

	public int remaining() {
		return this.buf.remaining();
	}

	public boolean hasRemaining() {
		return this.buf.hasRemaining();
	}

	public byte get() {
		return this.buf.get();
	}

	public short getUnsigned() {
		return this.buf.getUnsigned();
	}

	public IoBuffer put(byte b) {
		this.buf.put(b);
		return this;
	}

	public byte get(int index) {
		return this.buf.get(index);
	}

	public short getUnsigned(int index) {
		return this.buf.getUnsigned(index);
	}

	public IoBuffer put(int index, byte b) {
		this.buf.put(index, b);
		return this;
	}

	public IoBuffer get(byte[] dst, int offset, int length) {
		this.buf.get(dst, offset, length);
		return this;
	}

	public IoBuffer getSlice(int index, int length) {
		return this.buf.getSlice(index, length);
	}

	public IoBuffer getSlice(int length) {
		return this.buf.getSlice(length);
	}

	public IoBuffer get(byte[] dst) {
		this.buf.get(dst);
		return this;
	}

	public IoBuffer put(IoBuffer src) {
		this.buf.put(src);
		return this;
	}

	public IoBuffer put(ByteBuffer src) {
		this.buf.put(src);
		return this;
	}

	public IoBuffer put(byte[] src, int offset, int length) {
		this.buf.put(src, offset, length);
		return this;
	}

	public IoBuffer put(byte[] src) {
		this.buf.put(src);
		return this;
	}

	public IoBuffer compact() {
		this.buf.compact();
		return this;
	}

	public String toString() {
		return this.buf.toString();
	}

	public int hashCode() {
		return this.buf.hashCode();
	}

	public boolean equals(Object ob) {
		return this.buf.equals(ob);
	}

	public int compareTo(IoBuffer that) {
		return this.buf.compareTo(that);
	}

	public ByteOrder order() {
		return this.buf.order();
	}

	public IoBuffer order(ByteOrder bo) {
		this.buf.order(bo);
		return this;
	}

	public char getChar() {
		return this.buf.getChar();
	}

	public IoBuffer putChar(char value) {
		this.buf.putChar(value);
		return this;
	}

	public char getChar(int index) {
		return this.buf.getChar(index);
	}

	public IoBuffer putChar(int index, char value) {
		this.buf.putChar(index, value);
		return this;
	}

	public CharBuffer asCharBuffer() {
		return this.buf.asCharBuffer();
	}

	public short getShort() {
		return this.buf.getShort();
	}

	public int getUnsignedShort() {
		return this.buf.getUnsignedShort();
	}

	public IoBuffer putShort(short value) {
		this.buf.putShort(value);
		return this;
	}

	public short getShort(int index) {
		return this.buf.getShort(index);
	}

	public int getUnsignedShort(int index) {
		return this.buf.getUnsignedShort(index);
	}

	public IoBuffer putShort(int index, short value) {
		this.buf.putShort(index, value);
		return this;
	}

	public ShortBuffer asShortBuffer() {
		return this.buf.asShortBuffer();
	}

	public int getInt() {
		return this.buf.getInt();
	}

	public long getUnsignedInt() {
		return this.buf.getUnsignedInt();
	}

	public IoBuffer putInt(int value) {
		this.buf.putInt(value);
		return this;
	}

	public IoBuffer putUnsignedInt(byte value) {
		this.buf.putUnsignedInt(value);
		return this;
	}

	public IoBuffer putUnsignedInt(int index, byte value) {
		this.buf.putUnsignedInt(index, value);
		return this;
	}

	public IoBuffer putUnsignedInt(short value) {
		this.buf.putUnsignedInt(value);
		return this;
	}

	public IoBuffer putUnsignedInt(int index, short value) {
		this.buf.putUnsignedInt(index, value);
		return this;
	}

	public IoBuffer putUnsignedInt(int value) {
		this.buf.putUnsignedInt(value);
		return this;
	}

	public IoBuffer putUnsignedInt(int index, int value) {
		this.buf.putUnsignedInt(index, value);
		return this;
	}

	public IoBuffer putUnsignedInt(long value) {
		this.buf.putUnsignedInt(value);
		return this;
	}

	public IoBuffer putUnsignedInt(int index, long value) {
		this.buf.putUnsignedInt(index, value);
		return this;
	}

	public IoBuffer putUnsignedShort(byte value) {
		this.buf.putUnsignedShort(value);
		return this;
	}

	public IoBuffer putUnsignedShort(int index, byte value) {
		this.buf.putUnsignedShort(index, value);
		return this;
	}

	public IoBuffer putUnsignedShort(short value) {
		this.buf.putUnsignedShort(value);
		return this;
	}

	public IoBuffer putUnsignedShort(int index, short value) {
		this.buf.putUnsignedShort(index, value);
		return this;
	}

	public IoBuffer putUnsignedShort(int value) {
		this.buf.putUnsignedShort(value);
		return this;
	}

	public IoBuffer putUnsignedShort(int index, int value) {
		this.buf.putUnsignedShort(index, value);
		return this;
	}

	public IoBuffer putUnsignedShort(long value) {
		this.buf.putUnsignedShort(value);
		return this;
	}

	public IoBuffer putUnsignedShort(int index, long value) {
		this.buf.putUnsignedShort(index, value);
		return this;
	}

	public int getInt(int index) {
		return this.buf.getInt(index);
	}

	public long getUnsignedInt(int index) {
		return this.buf.getUnsignedInt(index);
	}

	public IoBuffer putInt(int index, int value) {
		this.buf.putInt(index, value);
		return this;
	}

	public IntBuffer asIntBuffer() {
		return this.buf.asIntBuffer();
	}

	public long getLong() {
		return this.buf.getLong();
	}

	public IoBuffer putLong(long value) {
		this.buf.putLong(value);
		return this;
	}

	public long getLong(int index) {
		return this.buf.getLong(index);
	}

	public IoBuffer putLong(int index, long value) {
		this.buf.putLong(index, value);
		return this;
	}

	public LongBuffer asLongBuffer() {
		return this.buf.asLongBuffer();
	}

	public float getFloat() {
		return this.buf.getFloat();
	}

	public IoBuffer putFloat(float value) {
		this.buf.putFloat(value);
		return this;
	}

	public float getFloat(int index) {
		return this.buf.getFloat(index);
	}

	public IoBuffer putFloat(int index, float value) {
		this.buf.putFloat(index, value);
		return this;
	}

	public FloatBuffer asFloatBuffer() {
		return this.buf.asFloatBuffer();
	}

	public double getDouble() {
		return this.buf.getDouble();
	}

	public IoBuffer putDouble(double value) {
		this.buf.putDouble(value);
		return this;
	}

	public double getDouble(int index) {
		return this.buf.getDouble(index);
	}

	public IoBuffer putDouble(int index, double value) {
		this.buf.putDouble(index, value);
		return this;
	}

	public DoubleBuffer asDoubleBuffer() {
		return this.buf.asDoubleBuffer();
	}

	public String getHexDump() {
		return this.buf.getHexDump();
	}

	public String getString(int fieldSize, CharsetDecoder decoder) throws CharacterCodingException {
		return this.buf.getString(fieldSize, decoder);
	}

	public String getString(CharsetDecoder decoder) throws CharacterCodingException {
		return this.buf.getString(decoder);
	}

	public String getPrefixedString(CharsetDecoder decoder) throws CharacterCodingException {
		return this.buf.getPrefixedString(decoder);
	}

	public String getPrefixedString(int prefixLength, CharsetDecoder decoder) throws CharacterCodingException {
		return this.buf.getPrefixedString(prefixLength, decoder);
	}

	public IoBuffer putString(CharSequence in, int fieldSize, CharsetEncoder encoder) throws CharacterCodingException {
		this.buf.putString(in, fieldSize, encoder);
		return this;
	}

	public IoBuffer putString(CharSequence in, CharsetEncoder encoder) throws CharacterCodingException {
		this.buf.putString(in, encoder);
		return this;
	}

	public IoBuffer putPrefixedString(CharSequence in, CharsetEncoder encoder) throws CharacterCodingException {
		this.buf.putPrefixedString(in, encoder);
		return this;
	}

	public IoBuffer putPrefixedString(CharSequence in, int prefixLength, CharsetEncoder encoder)
			throws CharacterCodingException {
		this.buf.putPrefixedString(in, prefixLength, encoder);
		return this;
	}

	public IoBuffer putPrefixedString(CharSequence in, int prefixLength, int padding, CharsetEncoder encoder)
			throws CharacterCodingException {
		this.buf.putPrefixedString(in, prefixLength, padding, encoder);
		return this;
	}

	public IoBuffer putPrefixedString(CharSequence in, int prefixLength, int padding, byte padValue,
			CharsetEncoder encoder) throws CharacterCodingException {
		this.buf.putPrefixedString(in, prefixLength, padding, padValue, encoder);
		return this;
	}

	public IoBuffer skip(int size) {
		this.buf.skip(size);
		return this;
	}

	public IoBuffer fill(byte value, int size) {
		this.buf.fill(value, size);
		return this;
	}

	public IoBuffer fillAndReset(byte value, int size) {
		this.buf.fillAndReset(value, size);
		return this;
	}

	public IoBuffer fill(int size) {
		this.buf.fill(size);
		return this;
	}

	public IoBuffer fillAndReset(int size) {
		this.buf.fillAndReset(size);
		return this;
	}

	public boolean isAutoExpand() {
		return this.buf.isAutoExpand();
	}

	public IoBuffer setAutoExpand(boolean autoExpand) {
		this.buf.setAutoExpand(autoExpand);
		return this;
	}

	public IoBuffer expand(int pos, int expectedRemaining) {
		this.buf.expand(pos, expectedRemaining);
		return this;
	}

	public IoBuffer expand(int expectedRemaining) {
		this.buf.expand(expectedRemaining);
		return this;
	}

	public Object getObject() throws ClassNotFoundException {
		return this.buf.getObject();
	}

	public Object getObject(ClassLoader classLoader) throws ClassNotFoundException {
		return this.buf.getObject(classLoader);
	}

	public IoBuffer putObject(Object o) {
		this.buf.putObject(o);
		return this;
	}

	public InputStream asInputStream() {
		return this.buf.asInputStream();
	}

	public OutputStream asOutputStream() {
		return this.buf.asOutputStream();
	}

	public IoBuffer duplicate() {
		return this.buf.duplicate();
	}

	public IoBuffer slice() {
		return this.buf.slice();
	}

	public IoBuffer asReadOnlyBuffer() {
		return this.buf.asReadOnlyBuffer();
	}

	public byte[] array() {
		return this.buf.array();
	}

	public int arrayOffset() {
		return this.buf.arrayOffset();
	}

	public int minimumCapacity() {
		return this.buf.minimumCapacity();
	}

	public IoBuffer minimumCapacity(int minimumCapacity) {
		this.buf.minimumCapacity(minimumCapacity);
		return this;
	}

	public IoBuffer capacity(int newCapacity) {
		this.buf.capacity(newCapacity);
		return this;
	}

	public boolean isReadOnly() {
		return this.buf.isReadOnly();
	}

	public int markValue() {
		return this.buf.markValue();
	}

	public boolean hasArray() {
		return this.buf.hasArray();
	}

	public void free() {
		this.buf.free();
	}

	public boolean isDerived() {
		return this.buf.isDerived();
	}

	public boolean isAutoShrink() {
		return this.buf.isAutoShrink();
	}

	public IoBuffer setAutoShrink(boolean autoShrink) {
		this.buf.setAutoShrink(autoShrink);
		return this;
	}

	public IoBuffer shrink() {
		this.buf.shrink();
		return this;
	}

	public int getMediumInt() {
		return this.buf.getMediumInt();
	}

	public int getUnsignedMediumInt() {
		return this.buf.getUnsignedMediumInt();
	}

	public int getMediumInt(int index) {
		return this.buf.getMediumInt(index);
	}

	public int getUnsignedMediumInt(int index) {
		return this.buf.getUnsignedMediumInt(index);
	}

	public IoBuffer putMediumInt(int value) {
		this.buf.putMediumInt(value);
		return this;
	}

	public IoBuffer putMediumInt(int index, int value) {
		this.buf.putMediumInt(index, value);
		return this;
	}

	public String getHexDump(int lengthLimit) {
		return this.buf.getHexDump(lengthLimit);
	}

	public boolean prefixedDataAvailable(int prefixLength) {
		return this.buf.prefixedDataAvailable(prefixLength);
	}

	public boolean prefixedDataAvailable(int prefixLength, int maxDataLength) {
		return this.buf.prefixedDataAvailable(prefixLength, maxDataLength);
	}

	public int indexOf(byte b) {
		return this.buf.indexOf(b);
	}

	public <E extends Enum<E>> E getEnum(Class<E> enumClass) {
		return this.buf.getEnum(enumClass);
	}

	public <E extends Enum<E>> E getEnum(int index, Class<E> enumClass) {
		return this.buf.getEnum(index, enumClass);
	}

	public <E extends Enum<E>> E getEnumShort(Class<E> enumClass) {
		return this.buf.getEnumShort(enumClass);
	}

	public <E extends Enum<E>> E getEnumShort(int index, Class<E> enumClass) {
		return this.buf.getEnumShort(index, enumClass);
	}

	public <E extends Enum<E>> E getEnumInt(Class<E> enumClass) {
		return this.buf.getEnumInt(enumClass);
	}

	public <E extends Enum<E>> E getEnumInt(int index, Class<E> enumClass) {
		return this.buf.getEnumInt(index, enumClass);
	}

	public IoBuffer putEnum(Enum<?> e) {
		this.buf.putEnum(e);
		return this;
	}

	public IoBuffer putEnum(int index, Enum<?> e) {
		this.buf.putEnum(index, e);
		return this;
	}

	public IoBuffer putEnumShort(Enum<?> e) {
		this.buf.putEnumShort(e);
		return this;
	}

	public IoBuffer putEnumShort(int index, Enum<?> e) {
		this.buf.putEnumShort(index, e);
		return this;
	}

	public IoBuffer putEnumInt(Enum<?> e) {
		this.buf.putEnumInt(e);
		return this;
	}

	public IoBuffer putEnumInt(int index, Enum<?> e) {
		this.buf.putEnumInt(index, e);
		return this;
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSet(Class<E> enumClass) {
		return this.buf.getEnumSet(enumClass);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSet(int index, Class<E> enumClass) {
		return this.buf.getEnumSet(index, enumClass);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetShort(Class<E> enumClass) {
		return this.buf.getEnumSetShort(enumClass);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetShort(int index, Class<E> enumClass) {
		return this.buf.getEnumSetShort(index, enumClass);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetInt(Class<E> enumClass) {
		return this.buf.getEnumSetInt(enumClass);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetInt(int index, Class<E> enumClass) {
		return this.buf.getEnumSetInt(index, enumClass);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetLong(Class<E> enumClass) {
		return this.buf.getEnumSetLong(enumClass);
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSetLong(int index, Class<E> enumClass) {
		return this.buf.getEnumSetLong(index, enumClass);
	}

	public <E extends Enum<E>> IoBuffer putEnumSet(Set<E> set) {
		this.buf.putEnumSet(set);
		return this;
	}

	public <E extends Enum<E>> IoBuffer putEnumSet(int index, Set<E> set) {
		this.buf.putEnumSet(index, set);
		return this;
	}

	public <E extends Enum<E>> IoBuffer putEnumSetShort(Set<E> set) {
		this.buf.putEnumSetShort(set);
		return this;
	}

	public <E extends Enum<E>> IoBuffer putEnumSetShort(int index, Set<E> set) {
		this.buf.putEnumSetShort(index, set);
		return this;
	}

	public <E extends Enum<E>> IoBuffer putEnumSetInt(Set<E> set) {
		this.buf.putEnumSetInt(set);
		return this;
	}

	public <E extends Enum<E>> IoBuffer putEnumSetInt(int index, Set<E> set) {
		this.buf.putEnumSetInt(index, set);
		return this;
	}

	public <E extends Enum<E>> IoBuffer putEnumSetLong(Set<E> set) {
		this.buf.putEnumSetLong(set);
		return this;
	}

	public <E extends Enum<E>> IoBuffer putEnumSetLong(int index, Set<E> set) {
		this.buf.putEnumSetLong(index, set);
		return this;
	}

	public IoBuffer putUnsigned(byte value) {
		this.buf.putUnsigned(value);
		return this;
	}

	public IoBuffer putUnsigned(int index, byte value) {
		this.buf.putUnsigned(index, value);
		return this;
	}

	public IoBuffer putUnsigned(short value) {
		this.buf.putUnsigned(value);
		return this;
	}

	public IoBuffer putUnsigned(int index, short value) {
		this.buf.putUnsigned(index, value);
		return this;
	}

	public IoBuffer putUnsigned(int value) {
		this.buf.putUnsigned(value);
		return this;
	}

	public IoBuffer putUnsigned(int index, int value) {
		this.buf.putUnsigned(index, value);
		return this;
	}

	public IoBuffer putUnsigned(long value) {
		this.buf.putUnsigned(value);
		return this;
	}

	public IoBuffer putUnsigned(int index, long value) {
		this.buf.putUnsigned(index, value);
		return this;
	}
}