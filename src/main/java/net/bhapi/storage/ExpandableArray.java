package net.bhapi.storage;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;

public class ExpandableArray <T> {
	private int freeID;
	private T[] data;
	
	@SuppressWarnings("unchecked")
	public ExpandableArray() {
		data = (T[]) new Object[64];
	}
	
	public void put(int index, T value) {
		if (index >= data.length) {
			data = Arrays.copyOf(data, index + 64);
		}
		data[index] = value;
	}
	
	public T get(int index) {
		return data[index];
	}
	
	public void clear() {
		Arrays.fill(data, null);
		freeID = 0;
	}
	
	public void putAll(Map<Integer, T> loadingObjects) {
		loadingObjects.forEach((id, obj) -> data[id] = obj);
	}
	
	public void forEach(BiConsumer<Integer, T> consumer) {
		for (int i = 0; i < data.length; i++) {
			if (data[i] != null) consumer.accept(i, data[i]);
		}
	}
	
	public int getFreeID() {
		while (freeID < data.length) {
			if (data[freeID] == null) return freeID;
			freeID++;
		}
		return freeID;
	}
}
