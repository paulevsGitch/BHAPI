package net.bhapi.event;

import net.bhapi.command.BHCommand;

import java.util.Map;

public class CommandRegistryEvent extends RegistryEvent<String, BHCommand> {
	public CommandRegistryEvent(Map<String, BHCommand> registry) {
		super(registry::put);
	}
	
	@Override
	public int getPriority() {
		return EventPriorities.COMMAND_REGISTRY;
	}
}
