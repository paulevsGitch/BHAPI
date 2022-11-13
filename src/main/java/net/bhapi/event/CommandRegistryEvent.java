package net.bhapi.event;

import net.bhapi.command.BHCommand;

import java.util.Map;

public class CommandRegistryEvent implements BHEvent {
	private final Map<String, BHCommand> registry;
	
	public CommandRegistryEvent(Map<String, BHCommand> registry) {
		this.registry = registry;
	}
	
	public void register(String name, BHCommand command) {
		registry.put(name, command);
	}
	
	@Override
	public int getPriority() {
		return 2;
	}
}
