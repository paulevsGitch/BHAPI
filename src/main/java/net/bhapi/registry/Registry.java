package net.bhapi.registry;

import net.bhapi.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Registry<T> {
	private final Map<Identifier, T> idToObj = new HashMap<>();
	private final Map<T, Identifier> objToId = new HashMap<>();
	
	public T register(Identifier id, T object) {
		idToObj.put(id, object);
		objToId.put(object, id);
		return object;
	}
	
	public boolean contains(Identifier id) {
		return idToObj.containsKey(id);
	}
	
	@Nullable
	public T get(Identifier id) {
		return idToObj.get(id);
	}
	
	@Nullable
	public Identifier getID(T object) {
		return objToId.get(object);
	}
	
	public Collection<T> values() {
		return idToObj.values();
	}
	
	public void forEach(Consumer<T> consumer) {
		objToId.keySet().forEach(consumer);
	}
	
	public void forEach(BiConsumer<Identifier, T> consumer) {
		idToObj.forEach(consumer);
	}
}
