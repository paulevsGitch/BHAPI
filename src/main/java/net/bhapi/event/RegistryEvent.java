package net.bhapi.event;

import java.util.function.BiConsumer;

public abstract class RegistryEvent <I, T> implements BHEvent {
	private final BiConsumer<I, T> registry;
	
	public RegistryEvent(BiConsumer<I, T> registry) {
		this.registry = registry;
	}
	
	public void register(I id, T value) {
		registry.accept(id, value);
	}
}
