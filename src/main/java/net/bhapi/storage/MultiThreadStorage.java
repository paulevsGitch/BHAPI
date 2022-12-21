package net.bhapi.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MultiThreadStorage <T> {
	private final Map<Long, T> objects = new HashMap<>();
	private final Supplier<T> constructor;
	
	public MultiThreadStorage(Supplier<T> constructor) {
		this.constructor = constructor;
	}
	
	public T get() {
		long id = Thread.currentThread().getId();
		T object;
		synchronized (objects) {
			object = objects.get(id);
			if (object == null) {
				object = constructor.get();
				objects.put(id, object);
			}
		}
		return object;
	}
	
	public void clear() {
		objects.clear();
	}
}
