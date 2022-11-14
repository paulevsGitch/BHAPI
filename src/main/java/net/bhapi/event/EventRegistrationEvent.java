package net.bhapi.event;

import java.util.Map;
import java.util.function.Supplier;

public class EventRegistrationEvent extends RegistryEvent<Class<? extends BHEvent>, Supplier<? extends BHEvent>> {
	public EventRegistrationEvent(Map<Class<? extends BHEvent>, Supplier<? extends BHEvent>> registry) {
		super(registry::put);
	}
	
	@Override
	public int getPriority() {
		return Integer.MIN_VALUE;
	}
}
