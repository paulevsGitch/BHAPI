package net.bhapi.client.render.texture;

import net.bhapi.storage.Vec2F;

public class TextureSample {
	private static final Vec2F[] UV_CACHE;
	private final TextureAtlas atlas;
	private final int id;
	private byte rotation;
	private byte index;
	private boolean mirrorU;
	private boolean mirrorV;
	
	protected TextureSample(TextureAtlas atlas, int id) {
		this.atlas = atlas;
		this.id = id;
	}
	
	public int getTextureID() {
		return id;
	}
	
	public Vec2F getUV(float u, float v) {
		Vec2F uv = UV_CACHE[index];
		index = (byte) ((index + 1) & 3);
		return getUV(uv.set(u, v));
	}
	
	public Vec2F getUV(Vec2F out) {
		if (rotation > 0) {
			out.subtract(0.5F);
			out.rotateCW(rotation);
			out.add(0.5F);
		}
		if (mirrorU) out.x = 1 - out.x;
		if (mirrorV) out.y = 1 - out.y;
		UVPair pair = atlas.getUV(id);
		out.x = pair.getU(out.x);
		out.y = pair.getV(out.y);
		return out;
	}
	
	@Deprecated(forRemoval = true)
	public float getU(float delta) {
		return atlas.getUV(id).getU(delta);
	}
	
	@Deprecated(forRemoval = true)
	public float getV(float delta) {
		return atlas.getUV(id).getV(delta);
	}
	
	public void setRotation(int rotation) {
		this.rotation = (byte) (rotation & 3);
	}
	
	public void setMirrorU(boolean mirror) {
		mirrorU = mirror;
	}
	
	public void setMirrorV(boolean mirror) {
		mirrorV = mirror;
	}
	
	public int getWidth() {
		return atlas.getUV(id).getWidth();
	}
	
	public int getHeight() {
		return atlas.getUV(id).getHeight();
	}
	
	public int getX() {
		return atlas.getUV(id).getX();
	}
	
	public int getY() {
		return atlas.getUV(id).getY();
	}
	
	static {
		UV_CACHE = new Vec2F[4];
		for (byte i = 0; i < 4; i++) {
			UV_CACHE[i] = new Vec2F();
		}
	}
}
