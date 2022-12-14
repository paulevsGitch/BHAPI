package net.bhapi.storage;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class Matrix4x4 {
	private static final FloatBuffer IDENTITY = BufferUtils.createFloatBuffer(16);
	private static final Matrix4x4 INTERNAL_MATRIX_1 = new Matrix4x4();
	private static final Matrix4x4 INTERNAL_MATRIX_2 = new Matrix4x4();
	
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	
	public Matrix4x4 identity() {
		return set(IDENTITY);
	}
	
	public Matrix4x4 put(int index, float value) {
		buffer.put(index, value);
		return this;
	}
	
	public Matrix4x4 rotationX(float angle) {
		float sin = (float) Math.sin(angle);
		float cos = (float) Math.cos(angle);
		identity();
		buffer.put(5, cos);
		buffer.put(6, sin);
		buffer.put(9, -sin);
		buffer.put(10, cos);
		return this;
	}
	
	public Matrix4x4 rotationY(float angle) {
		float sin = (float) Math.sin(angle);
		float cos = (float) Math.cos(angle);
		identity();
		buffer.put(0, cos);
		buffer.put(2, -sin);
		buffer.put(8, sin);
		buffer.put(10, cos);
		return this;
	}
	
	public Matrix4x4 rotateX(float angle) {
		INTERNAL_MATRIX_1.rotationX(angle);
		return multiply(INTERNAL_MATRIX_1);
	}
	
	public Matrix4x4 rotateY(float angle) {
		INTERNAL_MATRIX_1.rotationY(angle);
		return multiply(INTERNAL_MATRIX_1);
	}
	
	public Matrix4x4 move(Vec3F offset) {
		return move(offset.x, offset.y, offset.z);
	}
	
	public Matrix4x4 move(float x, float y, float z) {
		buffer.put(12, buffer.get(12) + buffer.get(0) * x + buffer.get(4) * y + buffer.get(8) * z);
		buffer.put(13, buffer.get(13) + buffer.get(1) * x + buffer.get(5) * y + buffer.get(9) * z);
		buffer.put(14, buffer.get(14) + buffer.get(2) * x + buffer.get(6) * y + buffer.get(10) * z);
		buffer.put(15, buffer.get(15) + buffer.get(3) * x + buffer.get(7) * y + buffer.get(11) * z);
		return this;
	}
	
	public Matrix4x4 set(Matrix4x4 source) {
		for (int i = 0; i < 16; i++) {
			buffer.put(i, source.buffer.get(i));
		}
		return this;
	}
	
	public Matrix4x4 set(FloatBuffer buffer) {
		for (int i = 0; i < 16; i++) {
			this.buffer.put(i, buffer.get(i));
		}
		return this;
	}
	
	public Matrix4x4 multiply(Matrix4x4 matrix) {
		for (int i = 0; i < 16; i++) {
			int x = i & 3;
			int y = i >> 2;
			float value = 0;
			for (int j = 0; j < 4; j++) {
				value += matrix.buffer.get((y << 2) | j) * buffer.get((j << 2) | x);
			}
			INTERNAL_MATRIX_2.buffer.put(i, value);
		}
		return set(INTERNAL_MATRIX_2);
	}
	
	public FloatBuffer getBuffer() {
		return buffer;
	}
	
	public Vec3F multiply(Vec3F vector) {
		float x = vector.x * buffer.get(0) + vector.y * buffer.get(4) + vector.z * buffer.get(8) + buffer.get(12);
		float y = vector.x * buffer.get(1) + vector.y * buffer.get(5) + vector.z * buffer.get(9) + buffer.get(13);
		float z = vector.x * buffer.get(2) + vector.y * buffer.get(6) + vector.z * buffer.get(10) + buffer.get(14);
		return vector.set(x, y, z);
	}
	
	public Vec3F project(Vec3F vector) {
		float x = vector.x * buffer.get(0) + vector.y * buffer.get(4) + vector.z * buffer.get(8) + buffer.get(12);
		float y = vector.x * buffer.get(1) + vector.y * buffer.get(5) + vector.z * buffer.get(9) + buffer.get(13);
		float z = vector.x * buffer.get(2) + vector.y * buffer.get(6) + vector.z * buffer.get(10) + buffer.get(14);
		float w = vector.x * buffer.get(3) + vector.y * buffer.get(7) + vector.z * buffer.get(11) + buffer.get(15);
		return vector.set(x, y, z).divide(w);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[");
		builder.append(buffer.get(0));
		for (byte i = 0; i < 16; i++) {
			builder.append(", ");
			builder.append(buffer.get(i));
		}
		builder.append("]");
		return builder.toString();
	}
	
	static {
		IDENTITY.put(0, 1);
		IDENTITY.put(5, 1);
		IDENTITY.put(10, 1);
		IDENTITY.put(15, 1);
	}
}
