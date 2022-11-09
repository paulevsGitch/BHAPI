package net.bhapi.event;

import net.bhapi.registry.Registry;
import net.bhapi.util.Identifier;

public abstract class RegistryEvent <T> implements BHEvent {
	private final Registry<T> registry;
	
	public RegistryEvent(Registry<T> registry) {
		this.registry = registry;
	}
	
	public Registry<T> getRegistry() {
		return registry;
	}
	
	public void register(Identifier id, T value) {
		registry.register(id, value);
	}
}
