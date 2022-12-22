package net.bhapi.client.render.vbo;

import net.bhapi.storage.Vec3F;
import net.bhapi.util.BufferUtil;
import net.fabricmc.tinyremapper.extension.mixin.common.data.Pair;
import net.minecraft.util.maths.MathHelper;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class IndexedVBO extends VBO {
	private List<Pair<Vec3F, int[]>> quadIndexData;
	private IntBuffer indexBuffer;
	private boolean canSort;
	
	public void setData(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer, FloatBuffer uvBuffer) {
		canSort = false;
		super.setData(vertexBuffer, normalBuffer, colorBuffer, uvBuffer);
		
		int vertex = 0;
		int quadCount = size >> 2;
		indexBuffer = BufferUtil.createIntBuffer(quadCount << 2);
		List<Pair<Vec3F, int[]>> quadIndexData = new ArrayList<>();
		
		for (int i = 0; i < quadCount; i++) {
			Vec3F center = new Vec3F();
			for (byte j = 0; j < 4; j++) {
				float x = vertexBuffer.get(vertex++);
				float y = vertexBuffer.get(vertex++);
				float z = vertexBuffer.get(vertex++);
				center.add(x, y, z);
			}
			center.multiply(0.25F);
			int index = i << 2;
			int[] data = new int[] { index, index | 1, index | 2, index | 3 };
			quadIndexData.add(Pair.of(center, data));
			indexBuffer.put(index, data);
		}
		
		this.quadIndexData = quadIndexData;
		indexBuffer.position(0);
		canSort = true;
	}
	
	public void render() {
		bind();
		update();
		if (indexBuffer == null) return;
		GL11.glDrawElements(GL11.GL_QUADS, indexBuffer);
	}
	
	public void sort(Comparator<Pair<Vec3F, int[]>> comparator) {
		if (!canSort || quadIndexData == null || indexBuffer == null) return;
		quadIndexData.sort(comparator);
		int index = 0;
		for (Pair<Vec3F, int[]> pair: quadIndexData) {
			indexBuffer.put(index, pair.second());
			index += 4;
		}
		indexBuffer.position(0);
	}
	
	public void sort(Vec3F offset) {
		sort((p1, p2) -> {
			Vec3F center1 = p1.first();
			Vec3F center2 = p2.first();
			float x1 = MathHelper.abs(center1.x + offset.x - 8);
			float y1 = MathHelper.abs(center1.y + offset.y - 8);
			float z1 = MathHelper.abs(center1.z + offset.z - 8);
			float x2 = MathHelper.abs(center2.x + offset.x - 8);
			float y2 = MathHelper.abs(center2.y + offset.y - 8);
			float z2 = MathHelper.abs(center2.z + offset.z - 8);
			float d1 = x1 + y1 + z1;
			float d2 = x2 + y2 + z2;
			return Float.compare(d2, d1);
		});
	}
}
