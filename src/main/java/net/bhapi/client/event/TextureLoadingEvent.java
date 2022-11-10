package net.bhapi.client.event;

import net.bhapi.BHAPI;
import net.bhapi.event.BHEvent;

public class TextureLoadingEvent implements BHEvent {
	public TextureLoadingEvent() {
		BHAPI.log("Load Textures");
	}
	
	@Override
	public int getPriority() {
		return 0;
	}
}
