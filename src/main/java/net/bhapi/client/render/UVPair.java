package net.bhapi.client.render;

import net.bhapi.storage.Vec2F;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class UVPair {
	private final Vec2F uv1;
	private final Vec2F uv2;
	
	public UVPair(Vec2F uv1, Vec2F uv2) {
		this.uv1 = uv1;
		this.uv2 = uv2;
	}
	
	public void getUV(float u, float v, Vec2F out) {
		out.x = MathUtil.lerp(uv1.x, uv2.x, u);
		out.y = MathUtil.lerp(uv1.y, uv2.y, v);
	}
}
