package net.bhapi.storage;

import net.minecraft.util.maths.MathHelper;

import java.util.Locale;
import java.util.Objects;

public class Vec3F {
	public float x;
	public float y;
	public float z;
	
	public Vec3F() {}
	
	public Vec3F(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public float lengthSqr() {
		return x * x + y * y + z * z;
	}
	
	public float length() {
		return MathHelper.sqrt(lengthSqr());
	}
	
	public Vec3F add(float value) {
		return add(value, value, value);
	}
	
	public Vec3F add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vec3F add(Vec3F vector) {
		return add(vector.x, vector.y, vector.z);
	}
	
	public Vec3F set(float value) {
		return set(value, value, value);
	}
	
	public Vec3F set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vec3F set(Vec3F vector) {
		return set(vector.x, vector.y, vector.z);
	}
	
	public Vec3F subtract(float value) {
		return subtract(value, value, value);
	}
	
	public Vec3F subtract(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	public Vec3F subtract(Vec3F vector) {
		return subtract(vector.x, vector.y, vector.z);
	}
	
	public Vec3F multiply(float value) {
		return multiply(value, value, value);
	}
	
	public Vec3F multiply(float x, float y, float z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}
	
	public Vec3F multiply(Vec3F vector) {
		return multiply(vector.x, vector.y, vector.z);
	}
	
	public Vec3F divide(float value) {
		return divide(value, value, value);
	}
	
	public Vec3F divide(float x, float y, float z) {
		this.x /= x;
		this.y /= y;
		this.z /= z;
		return this;
	}
	
	public Vec3F divide(Vec3F vector) {
		return divide(vector.x, vector.y, vector.z);
	}
	
	public Vec3F normalise() {
		float l = lengthSqr();
		if (l > 0) divide((float) Math.sqrt(l));
		return this;
	}
	
	public Vec3F cross(Vec3F vector) {
		float nx = this.y * vector.z - this.z * vector.y;
		float ny = this.z * vector.x - this.x * vector.z;
		float nz = this.x * vector.y - this.y * vector.x;
		return set(nx, ny, nz);
	}
	
	public float dot(Vec3F vector) {
		return this.x * vector.x + this.y * vector.y + this.z * vector.z;
	}
	
	@Override
	public Vec3F clone() {
		return new Vec3F(x, y, z);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Vec3F)) return false;
		Vec3F vec = (Vec3F) obj;
		return vec.x == x && vec.y == y && vec.z == z;
	}
	
	@Override
	public String toString() {
		return String.format(Locale.ROOT, "[%f, %f, %f]", x, y, z);
	}
}
