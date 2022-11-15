package net.bhapi.client;

import net.bhapi.BHAPI;
import net.bhapi.client.event.AfterTextureLoadedEvent;
import net.bhapi.client.event.TextureLoadingEvent;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.event.EventListener;
import net.bhapi.util.Identifier;
import net.bhapi.util.ImageUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// TODO remove this
@Environment(EnvType.CLIENT)
public class TestClientEvent {
	public static TextureSample stone;
	public static TextureSample testblock;
	
	@EventListener
	public void testClientEvent(AfterTextureLoadedEvent event) {
		BHAPI.log("Make Sample");
		stone = Textures.getAtlas().getSample(Identifier.make("terrain_1"));
		System.out.println(stone.getUV());
		testblock = Textures.getAtlas().getSample(Identifier.make("testblock"));
	}
	
	@EventListener
	public void onTextureLoad(TextureLoadingEvent event) {
		BHAPI.log("Load custom textures");
		event.register(Identifier.make("testblock"), ImageUtil.load("/assets/bhapi/icon.png"));
	}
}
