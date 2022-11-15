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
	public static TextureSample[] samples;
	
	@EventListener
	public void testClientEvent(AfterTextureLoadedEvent event) {
		BHAPI.log("Make Sample");
		samples = new TextureSample[] {
			Textures.getAtlas().getSample(Identifier.make("testblock")),
			Textures.getAtlas().getSample(Identifier.make("testblock2")),
			Textures.getAtlas().getSample(Identifier.make("testblock3")),
		};
	}
	
	@EventListener
	public void onTextureLoad(TextureLoadingEvent event) {
		BHAPI.log("Load custom textures");
		event.register(Identifier.make("testblock"), ImageUtil.load("/assets/bhapi/icon.png"));
		event.register(Identifier.make("testblock2"), ImageUtil.load("/assets/bhapi/unknown_tile_2.png"));
		event.register(Identifier.make("testblock3"), ImageUtil.load("/assets/bhapi/warped_cobble.png"));
	}
}
