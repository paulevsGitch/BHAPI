package net.bhapi.client.render.model.builder;

import net.bhapi.client.render.model.FaceGroup;
import net.bhapi.client.render.model.ModelQuad;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.Vec2F;
import net.bhapi.storage.Vec3F;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.MathUtil;
import net.fabricmc.tinyremapper.extension.mixin.common.data.Pair;

public class ModelCuboidBuilder {
	private static final ModelCuboidBuilder INSTANCE = new ModelCuboidBuilder();
	private final EnumArray<BlockDirection, Integer> textureIndex = new EnumArray<>(BlockDirection.class);
	private final EnumArray<BlockDirection, FaceGroup> culling = new EnumArray<>(BlockDirection.class);
	private final EnumArray<BlockDirection, Boolean> faces = new EnumArray<>(BlockDirection.class);
	private final EnumArray<BlockDirection, Pair<Vec2F, Vec2F>> uvs = new EnumArray<>(BlockDirection.class);
	private Vec3F minPos = new Vec3F();
	private Vec3F maxPos = new Vec3F();
	private boolean rescale;
	
	private ModelCuboidBuilder() {}
	
	protected static ModelCuboidBuilder start() {
		for (BlockDirection facing: BlockDirection.VALUES) {
			INSTANCE.culling.set(facing, FaceGroup.NONE);
			INSTANCE.textureIndex.set(facing, 0);
			INSTANCE.faces.set(facing, false);
			INSTANCE.uvs.set(facing, null);
		}
		INSTANCE.minPos.set(0, 0, 0);
		INSTANCE.maxPos.set(16, 16, 16);
		INSTANCE.rescale = false;
		return INSTANCE;
	}
	
	/**
	 * Set rescale mode. If enabled will divide all positions and values by 16.
	 */
	public ModelCuboidBuilder rescale(boolean rescale) {
		this.rescale = rescale;
		return this;
	}
	
	/**
	 * Set minimum cuboid point.
	 */
	public ModelCuboidBuilder setMinPos(float x, float y, float z) {
		minPos.set(x, y, z);
		if (rescale) minPos.divide(16);
		return this;
	}
	
	/**
	 * Set maximum cuboid point.
	 */
	public ModelCuboidBuilder setMaxPos(float x, float y, float z) {
		maxPos.set(x, y, z);
		if (rescale) maxPos.divide(16);
		return this;
	}
	
	/**
	 * Set texture index for face.
	 */
	public ModelCuboidBuilder setTextureIndex(BlockDirection facing, int index) {
		textureIndex.set(facing, index);
		return this;
	}
	
	/**
	 * Mark all faces to be added
	 */
	public ModelCuboidBuilder allFaces() {
		for (BlockDirection facing: BlockDirection.VALUES) faces.set(facing, true);
		return this;
	}
	
	/**
	 * Mark face to be added
	 */
	public ModelCuboidBuilder addFace(BlockDirection facing) {
		faces.set(facing, true);
		return this;
	}
	
	/**
	 * Set face group (culling) for face.
	 */
	public ModelCuboidBuilder setFaceGroup(BlockDirection facing, FaceGroup group) {
		culling.set(facing, group);
		return this;
	}
	
	/**
	 * Set face UV coordinates
	 */
	public ModelCuboidBuilder setUV(BlockDirection facing, float u1, float v1, float u2, float v2) {
		if (rescale) {
			u1 /= 16.0F;
			v1 /= 16.0F;
			u2 /= 16.0F;
			v2 /= 16.0F;
		}
		uvs.set(facing, Pair.of(new Vec2F(u1, v1), new Vec2F(u2, v2)));
		return this;
	}
	
	/**
	 * Finish building process and return to model builder
	 */
	public ModelBuilder build() {
		float u1, v1, u2, v2;
		for (BlockDirection facing: BlockDirection.VALUES) {
			if (faces.get(facing)) {
				ModelQuad quad = new ModelQuad(textureIndex.get(facing));
				Pair<Vec2F, Vec2F> uv = uvs.get(facing);
				switch (facing) {
					case NEG_Y -> {
						if (uv == null) {
							u1 = MathUtil.clamp(1F - minPos.x, 0, 1);
							u2 = MathUtil.clamp(1F - maxPos.x, 0, 1);
							v1 = MathUtil.clamp(maxPos.z, 0, 1);
							v2 = MathUtil.clamp(minPos.z, 0, 1);
						}
						else {
							Vec2F uv1 = uv.first();
							Vec2F uv2 = uv.second();
							u1 = MathUtil.clamp(uv1.x, 0, 1);
							u2 = MathUtil.clamp(uv2.x, 0, 1);
							v1 = MathUtil.clamp(uv1.y, 0, 1);
							v2 = MathUtil.clamp(uv2.y, 0, 1);
						}
						quad.setVertex(0, minPos.x, minPos.y, maxPos.z, u1, v1);
						quad.setVertex(1, minPos.x, minPos.y, minPos.z, u1, v2);
						quad.setVertex(2, maxPos.x, minPos.y, minPos.z, u2, v2);
						quad.setVertex(3, maxPos.x, minPos.y, maxPos.z, u2, v1);
					}
					case POS_Y -> {
						if (uv == null) {
							u1 = MathUtil.clamp(1F - maxPos.x, 0, 1);
							u2 = MathUtil.clamp(1F - minPos.x, 0, 1);
							v1 = MathUtil.clamp(1F - maxPos.z, 0, 1);
							v2 = MathUtil.clamp(1F - minPos.z, 0, 1);
						}
						else {
							Vec2F uv1 = uv.first();
							Vec2F uv2 = uv.second();
							u1 = MathUtil.clamp(uv1.x, 0, 1);
							u2 = MathUtil.clamp(uv2.x, 0, 1);
							v1 = MathUtil.clamp(uv1.y, 0, 1);
							v2 = MathUtil.clamp(uv2.y, 0, 1);
						}
						quad.setVertex(0, maxPos.x, maxPos.y, maxPos.z, u1, v1);
						quad.setVertex(1, maxPos.x, maxPos.y, minPos.z, u1, v2);
						quad.setVertex(2, minPos.x, maxPos.y, minPos.z, u2, v2);
						quad.setVertex(3, minPos.x, maxPos.y, maxPos.z, u2, v1);
					}
					case NEG_Z -> {
						if (uv == null) {
							u1 = MathUtil.clamp(1F - maxPos.x, 0, 1);
							u2 = MathUtil.clamp(1F - minPos.x, 0, 1);
							v1 = MathUtil.clamp(1F - maxPos.y, 0, 1);
							v2 = MathUtil.clamp(1F - minPos.y, 0, 1);
						}
						else {
							Vec2F uv1 = uv.first();
							Vec2F uv2 = uv.second();
							u1 = MathUtil.clamp(uv1.x, 0, 1);
							u2 = MathUtil.clamp(uv2.x, 0, 1);
							v1 = MathUtil.clamp(uv1.y, 0, 1);
							v2 = MathUtil.clamp(uv2.y, 0, 1);
						}
						quad.setVertex(0, minPos.x, maxPos.y, minPos.z, u2, v1);
						quad.setVertex(1, maxPos.x, maxPos.y, minPos.z, u1, v1);
						quad.setVertex(2, maxPos.x, minPos.y, minPos.z, u1, v2);
						quad.setVertex(3, minPos.x, minPos.y, minPos.z, u2, v2);
					}
					case POS_Z -> {
						if (uv == null) {
							u1 = MathUtil.clamp(minPos.x, 0, 1);
							u2 = MathUtil.clamp(maxPos.x, 0, 1);
							v1 = MathUtil.clamp(1F - maxPos.y, 0, 1);
							v2 = MathUtil.clamp(1F - minPos.y, 0, 1);
						}
						else {
							Vec2F uv1 = uv.first();
							Vec2F uv2 = uv.second();
							u1 = MathUtil.clamp(uv1.x, 0, 1);
							u2 = MathUtil.clamp(uv2.x, 0, 1);
							v1 = MathUtil.clamp(uv1.y, 0, 1);
							v2 = MathUtil.clamp(uv2.y, 0, 1);
						}
						quad.setVertex(0, minPos.x, maxPos.y, maxPos.z, u1, v1);
						quad.setVertex(1, minPos.x, minPos.y, maxPos.z, u1, v2);
						quad.setVertex(2, maxPos.x, minPos.y, maxPos.z, u2, v2);
						quad.setVertex(3, maxPos.x, maxPos.y, maxPos.z, u2, v1);
					}
					case NEG_X -> {
						if (uv == null) {
							u1 = MathUtil.clamp(maxPos.z, 0, 1);
							u2 = MathUtil.clamp(minPos.z, 0, 1);
							v1 = MathUtil.clamp(1F - minPos.y, 0, 1);
							v2 = MathUtil.clamp(1F - maxPos.y, 0, 1);
						}
						else {
							Vec2F uv1 = uv.first();
							Vec2F uv2 = uv.second();
							u1 = MathUtil.clamp(uv1.x, 0, 1);
							u2 = MathUtil.clamp(uv2.x, 0, 1);
							v1 = MathUtil.clamp(uv1.y, 0, 1);
							v2 = MathUtil.clamp(uv2.y, 0, 1);
						}
						quad.setVertex(0, minPos.x, maxPos.y, maxPos.z, u1, v2);
						quad.setVertex(1, minPos.x, maxPos.y, minPos.z, u2, v2);
						quad.setVertex(2, minPos.x, minPos.y, minPos.z, u2, v1);
						quad.setVertex(3, minPos.x, minPos.y, maxPos.z, u1, v1);
					}
					case POS_X -> {
						if (uv == null) {
							u1 = MathUtil.clamp(1F - maxPos.z, 0, 1);
							u2 = MathUtil.clamp(1F - minPos.z, 0, 1);
							v1 = MathUtil.clamp(1F - maxPos.y, 0, 1);
							v2 = MathUtil.clamp(1F - minPos.y, 0, 1);
						}
						else {
							Vec2F uv1 = uv.first();
							Vec2F uv2 = uv.second();
							u1 = MathUtil.clamp(uv1.x, 0, 1);
							u2 = MathUtil.clamp(uv2.x, 0, 1);
							v1 = MathUtil.clamp(uv1.y, 0, 1);
							v2 = MathUtil.clamp(uv2.y, 0, 1);
						}
						quad.setVertex(0, maxPos.x, minPos.y, maxPos.z, u1, v2);
						quad.setVertex(1, maxPos.x, minPos.y, minPos.z, u2, v2);
						quad.setVertex(2, maxPos.x, maxPos.y, minPos.z, u2, v1);
						quad.setVertex(3, maxPos.x, maxPos.y, maxPos.z, u1, v1);
					}
				}
				ModelBuilder.INSTANCE.addQuad(quad, culling.get(facing));
			}
		}
		return ModelBuilder.INSTANCE;
	}
}
