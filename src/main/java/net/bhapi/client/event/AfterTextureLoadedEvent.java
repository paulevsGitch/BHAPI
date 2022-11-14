package net.bhapi.client.event;

import net.bhapi.event.BHEvent;
import net.bhapi.event.EventPriorities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AfterTextureLoadedEvent implements BHEvent {
	@Override
	public int getPriority() {
		return EventPriorities.AFTER_BLOCK_AND_ITEMS;
	}
}
