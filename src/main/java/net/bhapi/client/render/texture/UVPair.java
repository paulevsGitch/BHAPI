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
	private final Vec2F center;
	
	public UVPair(Vec2I pos, Vec2I size, Vec2F uv1, Vec2F uv2) {
		this.pos = pos;
		this.size = size;
		this.uv1 = uv1;
		this.uv2 = uv2;
		this.center = MathUtil.lerp(uv1, uv2, 0.5F);
	}
	
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
	
	public void moveByCenter(Vec2F uv, boolean subtract) {
		if (subtract) uv.subtract(center);
		else uv.add(center);
	}
}
