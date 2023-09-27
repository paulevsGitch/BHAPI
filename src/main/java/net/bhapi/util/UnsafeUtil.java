package net.bhapi.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;

public class UnsafeUtil {
	private static final Unsafe UNSAFE;
	
	@SuppressWarnings("unchecked")
	public static <T> T copyObject(T obj) {
		if (obj == null) return null;
		
		Class<?> objClass = obj.getClass();
		if (objClass == String.class) return obj;
		if (objClass == Boolean.class) return obj;
		if (objClass.getSuperclass() == Number.class) return obj;
		
		if (objClass.isArray()) {
			switch (objClass.getName()) {
				case "[B" -> {
					byte[] src = (byte[]) obj;
					return (T) Arrays.copyOf(src, src.length);
				}
				case "S[" -> {
					short[] src = (short[]) obj;
					return (T) Arrays.copyOf(src, src.length);
				}
				case "C[" -> {
					char[] src = (char[]) obj;
					return (T) Arrays.copyOf(src, src.length);
				}
				case "I[" -> {
					int[] src = (int[]) obj;
					return (T) Arrays.copyOf(src, src.length);
				}
				case "J[" -> {
					long[] src = (long[]) obj;
					return (T) Arrays.copyOf(src, src.length);
				}
				case "F[" -> {
					float[] src = (float[]) obj;
					return (T) Arrays.copyOf(src, src.length);
				}
				case "D[", "[D" -> {
					double[] src = (double[]) obj;
					return (T) Arrays.copyOf(src, src.length);
				}
				case "Z[" -> {
					boolean[] src = (boolean[]) obj;
					return (T) Arrays.copyOf(src, src.length);
				}
				default -> {
					T[] src = (T[]) obj;
					return (T) Arrays.copyOf(src, src.length);
				}
			}
		}
		
		T result = null;
		
		try {
			result = (T) UNSAFE.allocateInstance(objClass);
		}
		catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		for (Field field : objClass.getDeclaredFields()) {
			long offset = UNSAFE.objectFieldOffset(field);
			Class<?> fieldClass = field.getType();
			switch (fieldClass.getName()) {
				case "byte" -> UNSAFE.putByte(result, offset, UNSAFE.getByte(obj, offset));
				case "short" -> UNSAFE.putShort(result, offset, UNSAFE.getShort(obj, offset));
				case "char" -> UNSAFE.putChar(result, offset, UNSAFE.getChar(obj, offset));
				case "int" -> UNSAFE.putInt(result, offset, UNSAFE.getInt(obj, offset));
				case "long" -> UNSAFE.putLong(result, offset, UNSAFE.getLong(obj, offset));
				case "float" -> UNSAFE.putFloat(result, offset, UNSAFE.getFloat(obj, offset));
				case "double" -> UNSAFE.putDouble(result, offset, UNSAFE.getDouble(obj, offset));
				case "boolean" -> UNSAFE.putBoolean(result, offset, UNSAFE.getBoolean(obj, offset));
				default -> UNSAFE.putObject(result, offset, copyObject(UNSAFE.getObject(obj, offset)));
			}
		}
		
		return result;
	}
	
	static {
		Unsafe unsafe = null;
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		UNSAFE = unsafe;
	}
}
