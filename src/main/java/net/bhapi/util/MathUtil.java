package net.bhapi.util;

import java.util.HashMap;
import java.util.Map;

public class MathUtil {
	public static int writeInt(byte[] data, int value, int index) {
		data[index++] = (byte) (value & 255);
		data[index++] = (byte) ((value >> 8) & 255);
		data[index++] = (byte) ((value >> 16) & 255);
		data[index++] = (byte) ((value >> 24) & 255);
		return index;
	}
	
	public static int readInt(byte[] data, int index) {
		int value = data[index++] & 255;
		value |= (data[index++] & 255) << 8;
		value |= (data[index++] & 255) << 16;
		value |= (data[index] & 255) << 24;
		return value;
	}
	
	public static int clamp(int value, int min, int max) {
		return value < min ? min : value > max ? max : value;
	}
	
	public static float clamp(float value, float min, float max) {
		return value < min ? min : value > max ? max : value;
	}
	
	public static double clamp(double value, double min, double max) {
		return value < min ? min : value > max ? max : value;
	}
	
	public static float lerp(float a, float b, float delta) {
		return a + delta * (b - a);
	}
	
	public static double lerp(double a, double b, double delta) {
		return a + delta * (b - a);
	}
	
	/**
	 * Find the largest positive bit (greater or equal).
	 * Values below zero will be ignored.
	 * @param value source number.
	 */
	public static int getCeilBitIndex(int value) {
		if (value <= 0) return 0;
		byte index = 0;
		byte count = 0;
		for (byte i = 0; i < 32; i++) {
			byte bit = (byte) (value & 1);
			if (bit == 1) {
				index = i;
				count++;
			}
			value >>>= 1;
		}
		return count == 1 ? index : index + 1;
	}
	
	/**
	 * Calculates closets power of two value to the given number (greater or equal).
	 * Values below zero will be ignored.
	 * Example output: 756 -> 1024, 186 -> 256.
	 * @param value source number.
	 */
	public static int getClosestPowerOfTwo(int value) {
		return 1 << getCeilBitIndex(value);
	}
	
	public static int wrap(int value, int side) {
		int offset = value / side * side;
		if (offset > value) offset -= side;
		float delta = (float) (value - offset) / side;
		return (int) (delta * side);
	}
	
	public static <A, B> Map<A, B> invertMap(Map<B, A> map) {
		Map<A, B> result = new HashMap<>();
		map.forEach((b, a) -> result.put(a, b));
		return result;
	}
	
	public static <T> boolean contains(T[] array, T value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return true;
		}
		return false;
	}
	
	public static <T> boolean contains(int[] array, int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return true;
		}
		return false;
	}
}
