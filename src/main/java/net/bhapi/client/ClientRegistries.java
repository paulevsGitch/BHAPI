package net.bhapi.client;

import net.bhapi.client.event.TextureLoadingEvent;
import net.bhapi.event.BHEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ClientRegistries {
	public static final Map<Class<? extends BHEvent>, Supplier<? extends BHEvent>> EVENT_REGISTRY = new HashMap<>();
	
	public static void init() {
		initEvents();
	}
	
	private static void initEvents() {
		EVENT_REGISTRY.put(TextureLoadingEvent.class, TextureLoadingEvent::new);
	}
}
