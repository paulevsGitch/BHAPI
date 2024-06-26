package net.bhapi.client.render.level;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.bhapi.client.render.vbo.VBO;
import net.bhapi.util.BufferUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Tessellator;

import java.nio.FloatBuffer;

@Environment(EnvType.CLIENT)
public class MeshBuilder {
	private final IntList normalIndexes = new IntArrayList(2048);
	private final IntList colorIndexes = new IntArrayList(2048);
	private final FloatList vertexData = new FloatArrayList(32768);
	private final FloatList normalData = new FloatArrayList(2048);
	private final ByteList colorData = new ByteArrayList(2048);
	private final FloatList uvData = new FloatArrayList(32768);
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
			
			x = vertexData.getFloat(vertexIndex++);
			y = vertexData.getFloat(vertexIndex++);
			z = vertexData.getFloat(vertexIndex);
			
			u = uvData.getFloat(uvIndex++);
			v = uvData.getFloat(uvIndex);
			
			int index = colorIndexes.getInt(i);
			if (index != colorIndex) {
				colorIndex = index << 2;
				r = colorData.getByte(colorIndex++) & 255;
				g = colorData.getByte(colorIndex++) & 255;
				b = colorData.getByte(colorIndex++) & 255;
				a = colorData.getByte(colorIndex) & 255;
				tessellator.color(r, g, b, a);
				colorIndex = index;
			}
			
			index = normalIndexes.getInt(i);
			if (index != normalIndex) {
				normalIndex = index * 3;
				nx = normalData.getFloat(normalIndex++);
				ny = normalData.getFloat(normalIndex++);
				nz = normalData.getFloat(normalIndex);
				tessellator.setNormal(nx, ny, nz);
				normalIndex = index;
			}
			
			tessellator.vertex(x, y, z, u, v);
		}
	}
	
	public void build(VBO vbo) {
		final int count = normalIndexes.size();
		
		if (count == 0) {
			vbo.setEmpty();
			return;
		}
		
		float x, y, z, nx, ny, nz, u, v, r, g, b, a;
		nx = 0; ny = 1; nz = 0;
		r = 1; g = 1; b = 1; a = 1;
		
		normalIndex = -1;
		colorIndex = -1;
		
		int bufferSize = vertexData.size();
		FloatBuffer vertexBuffer = BufferUtil.createFloatBuffer(bufferSize);
		FloatBuffer normalBuffer = BufferUtil.createFloatBuffer(bufferSize);
		FloatBuffer colorBuffer = BufferUtil.createFloatBuffer(bufferSize / 3 * 4);
		FloatBuffer uvBuffer = BufferUtil.createFloatBuffer(bufferSize / 3 * 2);
		
		for (int i = 0; i < count; i++) {
			int vertexIndex = i * 3;
			int uvIndex = i << 1;
			
			x = vertexData.getFloat(vertexIndex++);
			y = vertexData.getFloat(vertexIndex++);
			z = vertexData.getFloat(vertexIndex);
			
			u = uvData.getFloat(uvIndex++);
			v = uvData.getFloat(uvIndex);
			
			int index = colorIndexes.getInt(i);
			if (index != colorIndex) {
				colorIndex = index << 2;
				r = (colorData.getByte(colorIndex++) & 255) / 255F;
				g = (colorData.getByte(colorIndex++) & 255) / 255F;
				b = (colorData.getByte(colorIndex++) & 255) / 255F;
				a = (colorData.getByte(colorIndex) & 255) / 255F;
				colorIndex = index;
			}
			
			index = normalIndexes.getInt(i);
			if (index != normalIndex) {
				normalIndex = index * 3;
				nx = normalData.getFloat(normalIndex++);
				ny = normalData.getFloat(normalIndex++);
				nz = normalData.getFloat(normalIndex);
				normalIndex = index;
			}
			
			vertexBuffer.put(x);
			vertexBuffer.put(y);
			vertexBuffer.put(z);
			
			normalBuffer.put(nx);
			normalBuffer.put(ny);
			normalBuffer.put(nz);
			
			colorBuffer.put(r);
			colorBuffer.put(g);
			colorBuffer.put(b);
			colorBuffer.put(a);
			
			uvBuffer.put(u);
			uvBuffer.put(v);
		}
		
		vertexBuffer.position(0);
		normalBuffer.position(0);
		colorBuffer.position(0);
		uvBuffer.position(0);
		
		vbo.setData(vertexBuffer, normalBuffer, colorBuffer, uvBuffer);
	}
}
