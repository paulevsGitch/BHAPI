package net.bhapi.storage;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnumArray <K extends Enum<K>, V> {
	private final V[] values;
	private final K[] keys;
	
	@SuppressWarnings("unchecked")
	public EnumArray(Class<K> enumClass) {
		keys = enumClass.getEnumConstants();
		values = (V[]) new Object[keys.length];
	}
	
	public V get(K key) {
		return values[key.ordinal()];
	}
	
	public V getOrCreate(K key, Function<K, V> constructor) {
		int index = key.ordinal();
		V value = values[index];
		if (value == null) {
			value = constructor.apply(key);
			values[index] = value;
		}
		return value;
	}
	
	public V getOrCreate(K key, Supplier<V> constructor) {
		int index = key.ordinal();
		V value = values[index];
		if (value == null) {
			value = constructor.get();
			values[index] = value;
		}
		return value;
	}
	
	public void set(K key, V value) {
		values[key.ordinal()] = value;
	}
	
	public void forEach(Consumer<V> consumer) {
		for (V v: values) if (v != null) consumer.accept(v);
	}
	
	public void forEach(BiConsumer<K, V> consumer) {
		for (int i = 0; i < keys.length; i++) {
			if (values[i] != null) consumer.accept(keys[i], values[i]);
		}
	}
}
