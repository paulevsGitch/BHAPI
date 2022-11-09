package net.bhapi.event_old.context;

import net.bhapi.registry.Registry;

public class RegistryContext <T> implements EventContext {
	private final Registry<T> registry;
	
	public RegistryContext(Registry<T> registry) {
		this.registry = registry;
	}
	
	public Registry<T> getRegistry() {
		return registry;
	}
}
