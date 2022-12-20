package net.bhapi.client.render.level;

import net.bhapi.storage.Vec3I;

public record MeshData(
	int[] normalIndexes,
	int[] colorIndexes,
	float[] vertexData,
	float[] normalData,
	byte[] colorData,
	float[] uvData,
	Vec3I pos
) {
	public static MeshData makeEmpty(Vec3I pos) {
		return new MeshData(null, null, null, null, null, null, pos);
	}
}
