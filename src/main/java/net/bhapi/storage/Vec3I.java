package net.bhapi.storage;

import net.bhapi.util.BlockDirection;
import net.bhapi.util.MathUtil;
import net.minecraft.util.maths.MCMath;
import java.util.Locale;

public class Vec3I {
	public int x;
	public int y;
	public int z;
	
	public Vec3I() {}
	
	public Vec3I(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3I add(int value) {
		return add(value, value, value);
	}
	
	public Vec3I add(int x, int y, int z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vec3I add(Vec3I vector) {
		return add(vector.x, vector.y, vector.z);
	}
	
	public Vec3I set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vec3I set(Vec3I vector) {
		return set(vector.x, vector.y, vector.z);
	}
	
	public Vec3I subtract(int value) {
		return subtract(value, value, value);
	}
	
	public Vec3I subtract(int x, int y, int z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	public Vec3I subtract(Vec3I vector) {
		return subtract(vector.x, vector.y, vector.z);
	}
	
	public Vec3I move(BlockDirection direction) {
		return direction.move(this);
	}
	
	public int lengthSqr() {
		return x * x + y * y + z * z;
	}
	
	public float length() {
		return MCMath.sqrt(lengthSqr());
	}
	
	public int distanceSqr(Vec3I vector) {
		int dx = this.x - vector.x;
		int dy = this.y - vector.y;
		int dz = this.z - vector.z;
		return dx * dx + dy * dy + dz * dz;
	}
	
	public int distanceManhattan(Vec3I vector) {
		int dx = MathUtil.abs(this.x - vector.x);
		int dy = MathUtil.abs(this.y - vector.y);
		int dz = MathUtil.abs(this.z - vector.z);
		return dx + dy + dz;
	}
	
	public float distance(Vec3I vector) {
		return MCMath.sqrt(distanceSqr(vector));
	}
	
	@Override
	public Vec3I clone() {
		return new Vec3I(x, y, z);
	}
	
	@Override
	public int hashCode() {
		return (x & 1023) << 20 | (y & 1023) << 10 | (z & 1023);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Vec3I vec)) return false;
		return vec.x == x && vec.y == y && vec.z == z;
	}
	
	@Override
	public String toString() {
		return String.format(Locale.ROOT, "[%d, %d, %d]", x, y, z);
	}
}
