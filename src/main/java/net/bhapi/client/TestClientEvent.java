package net.bhapi.client;

import net.bhapi.BHAPI;
import net.bhapi.client.event.AfterTextureLoadedEvent;
import net.bhapi.client.event.TextureLoadingEvent;
import net.bhapi.client.event.TexturesReloadEvent;
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
			Textures.getAtlas().getSample(Identifier.make("bhapi", "block/cobblestone")),
			Textures.getAtlas().getSample(Identifier.make("bhapi", "block/unknown_tile_2")),
			Textures.getAtlas().getSample(Identifier.make("bhapi", "block/warped_cobble")),
		};
	}
	
	@EventListener
	public void onTextureLoad(TextureLoadingEvent event) {
		BHAPI.log("Load custom textures");
		event.registerAll(ImageUtil.loadTexturesFromPathDir(Identifier.make("bhapi", "block")));
	}
	
	@EventListener
	public void onTextureReload(TexturesReloadEvent event) {
		BHAPI.log("Load custom textures");
		event.registerAll(ImageUtil.loadTexturesFromPathDir(Identifier.make("bhapi", "block")));
	}
}
