package net.bhapi.storage;

import java.util.function.Supplier;

public class CircleCache <T> {
	private final T[] data;
	private int index;
	
	@SuppressWarnings("unchecked")
	public CircleCache(int capacity) {
		data = (T[]) new Object[capacity];
	}
	
	public CircleCache<T> fill(Supplier<T> constructor) {
		for (int i = 0; i < data.length; i++) {
			data[i] = constructor.get();
		}
		return this;
	}
	
	public void set(int index, T value) {
		data[index] = value;
	}
	
	public T get() {
		T value = data[index];
		if (++index >= data.length) index = 0;
		return value;
	}
	
	public T get(int index) {
		return data[index];
	}
	
	public int size() {
		return data.length;
	}
	
	public void clear() {
		index = 0;
	}
}
