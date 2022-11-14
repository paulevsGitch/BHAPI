package net.bhapi.client;

import net.bhapi.BHAPI;
import net.bhapi.client.event.TextureLoadingEvent;
import net.bhapi.event.EventListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TestClientEvent {
	@EventListener
	public void testClientEvent(TextureLoadingEvent event) {
		BHAPI.log("Test client event");
	}
}
