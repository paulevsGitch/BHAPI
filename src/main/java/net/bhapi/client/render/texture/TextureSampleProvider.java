package net.bhapi.client.render.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface TextureSampleProvider {
	@Environment(EnvType.CLIENT)
	TextureSample getTextureSample();
	
	@Environment(EnvType.CLIENT)
	void setTextureSample(TextureSample sample);
	
	static TextureSampleProvider cast(Object obj) {
		return (TextureSampleProvider) obj;
	}
}
