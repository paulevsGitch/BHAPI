package net.bhapi.event;

import net.bhapi.BHAPI;

import java.util.Map;
import java.util.function.Supplier;

public class EventRegistrationEvent implements BHEvent {
	private final Map<Class<? extends BHEvent>, Supplier<? extends BHEvent>> registry;
	
	public EventRegistrationEvent(Map<Class<? extends BHEvent>, Supplier<? extends BHEvent>> registry) {
		this.registry = registry;
		BHAPI.log("Register Custom Events");
	}
	
	@Override
	public int getPriority() {
		return Integer.MIN_VALUE;
	}
	
	public void register(Class<? extends BHEvent> eventClass, Supplier<? extends BHEvent> eventConstructor) {
		registry.put(eventClass, eventConstructor);
	}
}
