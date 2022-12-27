package net.bhapi.client.render.texture;

import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ParticleTextures {
	public static final TextureSample[] GENERIC = new TextureSample[8];
	public static final TextureSample[] SPLASH = new TextureSample[8];
	public static final TextureSample BUBBLE = Textures.getAtlas().getSample(Identifier.make("particle/bubble"));
	public static final TextureSample FISHING_HOOK = Textures.getAtlas().getSample(Identifier.make("particle/fishing_hook"));
	public static final TextureSample FLAME = Textures.getAtlas().getSample(Identifier.make("particle/flame"));
	public static final TextureSample LAVA = Textures.getAtlas().getSample(Identifier.make("particle/lava"));
	public static final TextureSample NOTE = Textures.getAtlas().getSample(Identifier.make("particle/note"));
	public static final TextureSample HEART = Textures.getAtlas().getSample(Identifier.make("particle/heart"));
	
	static {
		for (byte i = 0; i < 8; i++) {
			GENERIC[i] = Textures.getAtlas().getSample(Identifier.make("particle/generic_" + i));
		}
		for (byte i = 0; i < 4; i++) {
			SPLASH[i] = Textures.getAtlas().getSample(Identifier.make("particle/splash_" + i));
		}
	}
}
