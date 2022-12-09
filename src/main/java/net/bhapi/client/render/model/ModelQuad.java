package net.bhapi.client.render.model;

import net.bhapi.client.render.block.BlockItemView;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.storage.CircleCache;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.Vec2F;
import net.bhapi.storage.Vec3F;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.client.render.Tessellator;
import net.minecraft.level.BlockView;

@Environment(EnvType.CLIENT)
public class ModelQuad {
	private static final EnumArray<BlockDirection, Float> NORMAL_BRIGHTNESS = new EnumArray<>(BlockDirection.class);
	private final Vec3F[] positions = new Vec3F[4];
	private final Vec2F[] uvs = new Vec2F[4];
	private final int index;
	private float brightness;
	private boolean useAO;
	private int tintIndex;
	private Vec3F normal;
	
	public ModelQuad(int index) {
		this.index = index;
		for (byte i = 0; i < 4; i++) {
			positions[i] = new Vec3F();
			uvs[i] = new Vec2F();
		}
	}
	
	public void setVertex(int index, float x, float y, float z, float u, float v) {
		positions[index].set(x, y, z);
		uvs[index].set(u, v);
	}
	
	public void setPosition(int index, float x, float y, float z) {
		positions[index].set(x, y, z);
	}
	
	public void setUV(int index, float u, float v) {
		uvs[index].set(u, v);
	}
	
	public void setAO(boolean useAO) {
		this.useAO = useAO;
	}
	
	public void setTintIndex(int tintIndex) {
		this.tintIndex = tintIndex;
	}
	
	public void setNormal(Vec3F normal) {
		this.normal = normal;
	}
	
	public void finalise() {
		if (normal == null) {
			Vec3F a = positions[1].clone().subtract(positions[0]).normalise();
			Vec3F b = positions[2].clone().subtract(positions[0]).normalise();
			normal = a.cross(b).normalise();
		}
		
		if (normal.x > 0.99 || normal.x < -0.99) brightness = 0.6F;
		else if (normal.z > 0.99 || normal.z < -0.99) brightness = 0.8F;
		else if (normal.y < -0.99) brightness = 0.5F;
		else if (normal.y > 0.99) brightness = 1.0F;
		else {
			float abs = Math.abs(normal.x);
			float l = abs + Math.abs(normal.z);
			brightness = MathUtil.lerp(0.8F, 0.6F, abs / l);
			
			abs = Math.abs(normal.y);
			float lightY = normal.y < 0 ? 0.5F : 1.0F;
			brightness = MathUtil.lerp(brightness, lightY, abs * abs);
		}
	}
	
	public int getIndex() {
		return index;
	}
	
	public void apply(ModelRenderingContext context, TextureSample sample, CircleCache<Vec2F> uvCache) {
		Tessellator tessellator = context.getTessellator();
		double x = context.getX();
		double y = context.getY();
		double z = context.getZ();
		tessellator.setNormal(normal.x, normal.y, normal.z);
		BlockView view = context.getBlockView();
		float light = 1.0F;
		if (!(view instanceof BlockItemView)) {
			BaseBlock block = context.getState().getBlock();
			light = block.getBrightness(view, (int) x, (int) y, (int) z);
		}
		tessellator.color(brightness * light, brightness * light, brightness * light);
		for (byte i = 0; i < 4; i++) {
			Vec3F pos = positions[i];
			Vec2F uv = uvs[i];
			uv = sample.getUV(uv.x, uv.y, uvCache.get());
			tessellator.vertex(x + pos.x, y + pos.y, z + pos.z, uv.x, uv.y);
		}
	}
	
	static {
		NORMAL_BRIGHTNESS.set(BlockDirection.NEG_Y, 0.5F);
		NORMAL_BRIGHTNESS.set(BlockDirection.POS_Y, 1.0F);
		NORMAL_BRIGHTNESS.set(BlockDirection.NEG_Z, 0.8F);
		NORMAL_BRIGHTNESS.set(BlockDirection.POS_Z, 0.8F);
		NORMAL_BRIGHTNESS.set(BlockDirection.NEG_X, 0.6F);
		NORMAL_BRIGHTNESS.set(BlockDirection.POS_X, 0.6F);
	}
}
