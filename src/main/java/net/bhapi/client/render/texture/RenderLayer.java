package net.bhapi.client.render.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum RenderLayer {
	SOLID, TRANSPARENT, TRANSLUCENT;
	
	public static final RenderLayer[] VALUES = RenderLayer.values();
}
