package net.bhapi.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BufferUtil {
	/**
	 * Create a new byte buffer with specified capacity and with native byte order.
	 * Buffer will be allocated directly.
	 * @param capacity buffer capacity
	 * @return new {@link ByteBuffer}
	 */
	public static ByteBuffer createByteBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
	}
	
	/**
	 * Create a new int buffer with specified capacity and with native byte order.
	 * Buffer will be allocated directly.
	 * @param capacity buffer capacity
	 * @return new {@link IntBuffer}
	 */
	public static IntBuffer createIntBuffer(int capacity) {
		return createByteBuffer(capacity << 2).asIntBuffer();
	}
	
	/**
	 * Create a new float buffer with specified capacity and with native byte order.
	 * Buffer will be allocated directly.
	 * @param capacity buffer capacity
	 * @return new {@link FloatBuffer}
	 */
	public static FloatBuffer createFloatBuffer(int capacity) {
		return createByteBuffer(capacity << 2).asFloatBuffer();
	}
}
