package net.bhapi.client;

import net.bhapi.BHAPI;
import net.bhapi.client.event.AfterTextureLoadedEvent;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.event.EventListener;
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TestClientEvent {
	public static TextureSample stone;
	
	@EventListener
	public void testClientEvent(AfterTextureLoadedEvent event) {
		BHAPI.log("Make Sample");
		stone = Textures.getAtlas().getSample(Identifier.make("terrain_1"));
		System.out.println(stone.getUV());
	}
}
