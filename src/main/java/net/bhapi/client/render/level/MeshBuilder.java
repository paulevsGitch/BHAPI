package net.bhapi.client.render.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Tessellator;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MeshBuilder {
	private final List<Integer> normalIndexes = new ArrayList<>(2048);
	private final List<Integer> colorIndexes = new ArrayList<>(2048);
	private final List<Float> vertexData = new ArrayList<>(32768);
	private final List<Float> normalData = new ArrayList<>(2048);
	private final List<Byte> colorData = new ArrayList<>(2048);
	private final List<Float> uvData = new ArrayList<>(32768);
	private double offsetX;
	private double offsetY;
	private double offsetZ;
	private int normalIndex;
	private int colorIndex;
	
	public void start() {
		normalIndexes.clear();
		colorIndexes.clear();
		vertexData.clear();
		normalData.clear();
		colorData.clear();
		uvData.clear();
		
		normalIndex = 0;
		colorIndex = 0;
		offsetX = 0;
		offsetY = 0;
		offsetZ = 0;
		
		normalData.add(0F);
		normalData.add(1F);
		normalData.add(0F);
		
		colorData.add((byte) 255);
		colorData.add((byte) 255);
		colorData.add((byte) 255);
		colorData.add((byte) 255);
	}
	
	public void setNormal(float x, float y, float z) {
		normalData.add(x);
		normalData.add(y);
		normalData.add(z);
		normalIndex++;
	}
	
	public void setColor(float r, float g, float b) {
		setColor((int) (r * 255), (int) (g * 255), (int) (b * 255), 255);
	}
	
	public void setColor(int r, int g, int b) {
		setColor(r, g, b, 255);
	}
	
	public void setColor(float r, float g, float b, float a) {
		setColor((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
	}
	
	public void setColor(int r, int g, int b, int a) {
		colorData.add((byte) r);
		colorData.add((byte) g);
		colorData.add((byte) b);
		colorData.add((byte) a);
		colorIndex++;
	}
	
	public void setOffset(int x, int y, int z) {
		offsetX = x;
		offsetY = y;
		offsetZ = z;
	}
	
	public void addOffset(double x, double y, double z) {
		offsetX += x;
		offsetY += y;
		offsetZ += z;
	}
	
	public void vertex(float x, float y, float z, float u, float v) {
		normalIndexes.add(normalIndex);
		colorIndexes.add(colorIndex);
		vertexData.add(x);
		vertexData.add(y);
		vertexData.add(z);
		uvData.add(u);
		uvData.add(v);
	}
	
	public void vertex(double x, double y, double z, float u, float v) {
		float dx = (float) (x + offsetX);
		float dy = (float) (y + offsetY);
		float dz = (float) (z + offsetZ);
		vertex(dx, dy, dz, u, v);
	}
	
	public boolean isEmpty() {
		return normalIndexes.isEmpty();
	}
	
	public void build(Tessellator tessellator) {
		final int count = normalIndexes.size();
		float x, y, z, nx, ny, nz, u, v;
		int r, g, b, a;
		
		normalIndex = -1;
		colorIndex = -1;
		
		for (int i = 0; i < count; i++) {
			int vertexIndex = i * 3;
			int uvIndex = i << 1;
			
			x = vertexData.get(vertexIndex++);
			y = vertexData.get(vertexIndex++);
			z = vertexData.get(vertexIndex);
			
			u = uvData.get(uvIndex++);
			v = uvData.get(uvIndex);
			
			int index = colorIndexes.get(i);
			if (index != colorIndex) {
				colorIndex = index << 2;
				r = colorData.get(colorIndex++) & 255;
				g = colorData.get(colorIndex++) & 255;
				b = colorData.get(colorIndex++) & 255;
				a = colorData.get(colorIndex) & 255;
				tessellator.color(r, g, b, a);
				colorIndex = index;
			}
			
			index = normalIndexes.get(i);
			if (index != normalIndex) {
				normalIndex = index * 3;
				nx = normalData.get(normalIndex++);
				ny = normalData.get(normalIndex++);
				nz = normalData.get(normalIndex);
				tessellator.setNormal(nx, ny, nz);
				normalIndex = index;
			}
			
			tessellator.vertex(x, y, z, u, v);
		}
	}
}
