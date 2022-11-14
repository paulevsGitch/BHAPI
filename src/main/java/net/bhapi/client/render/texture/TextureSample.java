package net.bhapi.client.render.texture;

public class TextureSample {
	private final TextureAtlas atlas;
	private final int id;
	
	protected TextureSample(TextureAtlas atlas, int id) {
		this.atlas = atlas;
		this.id = id;
	}
	
	public UVPair getUV() {
		return atlas.getUV(id);
	}
}
