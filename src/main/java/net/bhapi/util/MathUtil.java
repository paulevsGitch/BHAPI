package net.bhapi.util;

public class MathUtil {
	public static int clamp(int x, int min, int max) {
		return x < min ? min : x > max ? max : x;
	}
	
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
}
