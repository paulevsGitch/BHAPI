package net.bhapi.storage;

public class EnumArray <A, B extends Enum<B>> {
	private final A[] values;
	
	@SuppressWarnings("unchecked")
	public EnumArray(Class<B> enumClass) {
		int count = enumClass.getEnumConstants().length;
		values = (A[]) new Object[count];
	}
	
	public A get(B key) {
		return values[key.ordinal()];
	}
	
	public void set(B key, A value) {
		values[key.ordinal()] = value;
	}
}
