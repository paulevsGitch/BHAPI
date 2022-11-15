package net.bhapi.client.event;

import net.bhapi.event.EventPriorities;
import net.bhapi.event.RegistryEvent;
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.awt.image.BufferedImage;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class TextureLoadingEvent extends RegistryEvent<Identifier, BufferedImage> {
	public TextureLoadingEvent(Map<Identifier, BufferedImage> registry) {
		super(registry::put);
	}
	
	@Override
	public int getPriority() {
		return EventPriorities.TEXTURE_LOADING;
	}
}
