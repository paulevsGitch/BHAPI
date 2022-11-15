package net.bhapi.client.render.texture;

import net.bhapi.storage.Vec2F;
import net.bhapi.storage.Vec2I;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class UVPair {
	private final Vec2I pos;
	private final Vec2I size;
	private final Vec2F uv1;
	private final Vec2F uv2;
	
	public UVPair(Vec2I pos, Vec2I size, Vec2F uv1, Vec2F uv2) {
		this.pos = pos;
		this.size = size;
		this.uv1 = uv1;
		this.uv2 = uv2;
	}
	
	/*public void getUV(float u, float v, Vec2F out) {
		out.x = getU(u);
		out.y = getV(v);
	}*/
	
	public float getU(float delta) {
		return MathUtil.lerp(uv1.x, uv2.x, delta);
	}
	
	public float getV(float delta) {
		return MathUtil.lerp(uv1.y, uv2.y, delta);
	}
	
	public int getX() {
		return pos.x;
	}
	
	public int getY() {
		return pos.y;
	}
	
	public int getWidth() {
		return size.x;
	}
	
	public int getHeight() {
		return size.y;
	}
}
