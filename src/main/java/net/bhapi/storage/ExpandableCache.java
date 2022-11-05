package net.bhapi.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ExpandableCache <T> {
	private final List<T> cache = new ArrayList<>();
	private final Supplier<T> constructor;
	private int index;
	
	public ExpandableCache(Supplier<T> constructor) {
		this.constructor = constructor;
	}
	
	public void clear() {
		index = 0;
	}
	
	public T get() {
		if (index < cache.size()) return cache.get(index++);
		T object = constructor.get();
		cache.add(object);
		index++;
		return object;
	}
}
