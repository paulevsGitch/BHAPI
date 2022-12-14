package net.bhapi.client.render.model.builder;

import net.bhapi.client.render.model.FaceGroup;
import net.bhapi.client.render.model.ModelQuad;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.Matrix4x4;
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
	private final Matrix4x4 rotation = new Matrix4x4();
	private final Vec3F[] points = new Vec3F[4];
	private final Vec3F rotCenter = new Vec3F();
	private final Vec3F minPos = new Vec3F();
	private final Vec3F maxPos = new Vec3F();
	private boolean rescale;
	
	private ModelCuboidBuilder() {
		for (byte i = 0; i < 4; i++) points[i] = new Vec3F();
	}
	
	protected static ModelCuboidBuilder start() {
		for (BlockDirection facing: BlockDirection.VALUES) {
			INSTANCE.culling.set(facing, FaceGroup.NONE);
			INSTANCE.textureIndex.set(facing, 0);
			INSTANCE.faces.set(facing, false);
			INSTANCE.uvs.set(facing, null);
		}
		INSTANCE.minPos.set(0, 0, 0);
		INSTANCE.maxPos.set(16, 16, 16);
		INSTANCE.rotation.identity();
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
	
	public ModelCuboidBuilder addFaces(BlockDirection... facing) {
		for (BlockDirection face: facing) addFace(face);
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
	 * Set cuboid rotation around x or y axis
	 * @param x rotation center X
	 * @param y rotation center Y
	 * @param z rotation center Z
	 * @param axis rotation axis, 'x' or 'y'
	 * @param angle rotation angle, in radians
	 */
	public ModelCuboidBuilder setRotation(float x, float y, float z, char axis, float angle) {
		switch (axis) {
			case 'x' -> rotation.rotationX(angle);
			case 'y' -> rotation.rotationY(angle);
		}
		rotCenter.set(x, y, z);
		if (rescale) rotCenter.divide(16);
		return this;
	}
	
	private void applyRotation() {
		for (Vec3F pos: points) {
			rotation.multiply(pos.subtract(rotCenter)).add(rotCenter);
		}
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
						points[0].set(minPos.x, minPos.y, maxPos.z);
						points[1].set(minPos.x, minPos.y, minPos.z);
						points[2].set(maxPos.x, minPos.y, minPos.z);
						points[3].set(maxPos.x, minPos.y, maxPos.z);
						applyRotation();
						quad.setVertex(0, points[0].x, points[0].y, points[0].z, u1, v1);
						quad.setVertex(1, points[1].x, points[1].y, points[1].z, u1, v2);
						quad.setVertex(2, points[2].x, points[2].y, points[2].z, u2, v2);
						quad.setVertex(3, points[3].x, points[3].y, points[3].z, u2, v1);
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
						points[0].set(maxPos.x, maxPos.y, maxPos.z);
						points[1].set(maxPos.x, maxPos.y, minPos.z);
						points[2].set(minPos.x, maxPos.y, minPos.z);
						points[3].set(minPos.x, maxPos.y, maxPos.z);
						applyRotation();
						quad.setVertex(0, points[0].x, points[0].y, points[0].z, u1, v1);
						quad.setVertex(1, points[1].x, points[1].y, points[1].z, u1, v2);
						quad.setVertex(2, points[2].x, points[2].y, points[2].z, u2, v2);
						quad.setVertex(3, points[3].x, points[3].y, points[3].z, u2, v1);
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
						points[0].set(minPos.x, maxPos.y, minPos.z);
						points[1].set(maxPos.x, maxPos.y, minPos.z);
						points[2].set(maxPos.x, minPos.y, minPos.z);
						points[3].set(minPos.x, minPos.y, minPos.z);
						applyRotation();
						quad.setVertex(0, points[0].x, points[0].y, points[0].z, u2, v1);
						quad.setVertex(1, points[1].x, points[1].y, points[1].z, u1, v1);
						quad.setVertex(2, points[2].x, points[2].y, points[2].z, u1, v2);
						quad.setVertex(3, points[3].x, points[3].y, points[3].z, u2, v2);
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
						points[0].set(minPos.x, maxPos.y, maxPos.z);
						points[1].set(minPos.x, minPos.y, maxPos.z);
						points[2].set(maxPos.x, minPos.y, maxPos.z);
						points[3].set(maxPos.x, maxPos.y, maxPos.z);
						applyRotation();
						quad.setVertex(0, points[0].x, points[0].y, points[0].z, u1, v1);
						quad.setVertex(1, points[1].x, points[1].y, points[1].z, u1, v2);
						quad.setVertex(2, points[2].x, points[2].y, points[2].z, u2, v2);
						quad.setVertex(3, points[3].x, points[3].y, points[3].z, u2, v1);
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
						points[0].set(minPos.x, maxPos.y, maxPos.z);
						points[1].set(minPos.x, maxPos.y, minPos.z);
						points[2].set(minPos.x, minPos.y, minPos.z);
						points[3].set(minPos.x, minPos.y, maxPos.z);
						applyRotation();
						quad.setVertex(0, points[0].x, points[0].y, points[0].z, u1, v2);
						quad.setVertex(1, points[1].x, points[1].y, points[1].z, u2, v2);
						quad.setVertex(2, points[2].x, points[2].y, points[2].z, u2, v1);
						quad.setVertex(3, points[3].x, points[3].y, points[3].z, u1, v1);
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
						points[0].set(maxPos.x, minPos.y, maxPos.z);
						points[1].set(maxPos.x, minPos.y, minPos.z);
						points[2].set(maxPos.x, maxPos.y, minPos.z);
						points[3].set(maxPos.x, maxPos.y, maxPos.z);
						applyRotation();
						quad.setVertex(0, points[0].x, points[0].y, points[0].z, u1, v2);
						quad.setVertex(1, points[1].x, points[1].y, points[1].z, u2, v2);
						quad.setVertex(2, points[2].x, points[2].y, points[2].z, u2, v1);
						quad.setVertex(3, points[3].x, points[3].y, points[3].z, u1, v1);
					}
				}
				ModelBuilder.INSTANCE.addQuad(quad, culling.get(facing));
			}
		}
		return ModelBuilder.INSTANCE;
	}
}
