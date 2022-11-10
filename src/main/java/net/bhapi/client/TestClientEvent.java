package net.bhapi.client;

import net.bhapi.BHAPI;
import net.bhapi.client.event.TextureLoadingEvent;
import net.bhapi.event.EventListener;

public class TestClientEvent {
	@EventListener
	public void testClientEvent(TextureLoadingEvent event) {
		BHAPI.log("Test client event");
	}
}
