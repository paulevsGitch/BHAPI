package net.bhapi.client.render.vbo;

import net.bhapi.storage.Vec3F;
import net.bhapi.util.BufferUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.bhapi.storage.Pair;
import net.minecraft.util.maths.MCMath;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class IndexedVBO extends VBO {
	private IndexedVBOData indexedData;
	
	public void setData(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer, FloatBuffer uvBuffer) {
		int vertex = 0;
		int quadCount = vertexBuffer.capacity() / 12;
		IntBuffer indexBuffer = BufferUtil.createIntBuffer(quadCount << 2);
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
			quadIndexData.add(new Pair<>(center, data));
			indexBuffer.put(index, data);
		}
		
		indexBuffer.position(0);
		
		indexedData = new IndexedVBOData(
			vertexBuffer,
			normalBuffer,
			colorBuffer,
			uvBuffer,
			quadIndexData,
			indexBuffer
		);
		
		update = true;
	}
	
	public void render() {
		init();
		bind();
		update();
		IndexedVBOData data = this.indexedData;
		if (data == null) return;
		GL11.glDrawElements(GL11.GL_QUADS, data.indexBuffer);
	}
	
	public void sort(Comparator<Pair<Vec3F, int[]>> comparator) {
		IndexedVBOData data = this.indexedData;
		if (data == null) return;
		data.quadIndexData.sort(comparator);
		int index = 0;
		for (Pair<Vec3F, int[]> pair: data.quadIndexData) {
			data.indexBuffer.put(index, pair.second());
			index += 4;
		}
		data.indexBuffer.position(0);
	}
	
	public void sort(Vec3F offset) {
		sort((p1, p2) -> {
			Vec3F center1 = p1.first();
			Vec3F center2 = p2.first();
			float x1 = MCMath.abs(center1.x + offset.x - 8);
			float y1 = MCMath.abs(center1.y + offset.y - 8);
			float z1 = MCMath.abs(center1.z + offset.z - 8);
			float x2 = MCMath.abs(center2.x + offset.x - 8);
			float y2 = MCMath.abs(center2.y + offset.y - 8);
			float z2 = MCMath.abs(center2.z + offset.z - 8);
			float d1 = x1 + y1 + z1;
			float d2 = x2 + y2 + z2;
			return Float.compare(d2, d1);
		});
	}
	
	@Override
	protected void update() {
		if (!update) return;
		IndexedVBOData data = this.indexedData;
		update = false;
		
		if (data == null) return;
		
		attachBuffer(vertexTarget, data.vertexBuffer);
		attachBuffer(normalTarget, data.normalBuffer);
		attachBuffer(colorTarget, data.colorBuffer);
		attachBuffer(uvTarget, data.uvBuffer);
		this.size = data.vertexBuffer.capacity() / 3;
		
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexTarget);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		
		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalTarget);
		GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
		
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorTarget);
		GL11.glColorPointer(4, GL11.GL_FLOAT, 0, 0);
		
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvTarget);
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
	}
	
	private record IndexedVBOData(
		FloatBuffer vertexBuffer,
		FloatBuffer normalBuffer,
		FloatBuffer colorBuffer,
		FloatBuffer uvBuffer,
		List<Pair<Vec3F, int[]>> quadIndexData,
		IntBuffer indexBuffer
	) {}
}
