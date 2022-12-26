package net.bhapi.client.render.texture;

import net.bhapi.blockstate.BlockState;
import net.bhapi.storage.CircleCache;
import net.bhapi.storage.Vec2F;
import net.bhapi.util.ColorUtil;
import net.minecraft.level.BlockView;

public class TextureSample {
	private static final CircleCache<Vec2F> UV_CACHE = new CircleCache<Vec2F>(4).fill(Vec2F::new);
	private static final ColorProvider DEFAULT_PROVIDER = (view, x, y, z, state) -> ColorUtil.WHITE_COLOR;
	
	private final TextureAtlas atlas;
	private final RenderLayer layer;
	private final int id;
	
	private ColorProvider provider;
	private boolean mirrorU;
	private boolean mirrorV;
	private byte rotation;
	private float light;
	
	protected TextureSample(TextureAtlas atlas, int id, RenderLayer layer) {
		this.provider = DEFAULT_PROVIDER;
		this.atlas = atlas;
		this.layer = layer;
		this.id = id;
	}
	
	public int getTextureID() {
		return id;
	}
	
	public Vec2F getUV(float u, float v) {
		return getUV(u, v, UV_CACHE.get());
	}
	
	public Vec2F getUV(float u, float v, Vec2F out) {
		return getUV(out.set(u, v));
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
	
	public float getLight() {
		return light;
	}
	
	public void setLight(float light) {
		this.light = light;
	}
	
	public RenderLayer getLayer() {
		return layer;
	}
	
	public void setColorProvider(ColorProvider provider) {
		this.provider = provider;
	}
	
	public int getColorMultiplier(BlockView view, double x, double y, double z, BlockState state) {
		return provider.getColorMultiplier(view, x, y, z, state);
	}
	
	@Override
	public TextureSample clone() {
		return new TextureSample(atlas, id, layer);
	}
}
