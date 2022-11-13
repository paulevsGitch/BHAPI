package net.bhapi.event;

public class StartupEvent implements BHEvent {
	@Override
	public int getPriority() {
		return EventPriorities.STARTUP;
	}
}
