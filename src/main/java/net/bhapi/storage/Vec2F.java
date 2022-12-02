package net.bhapi.storage;

import net.minecraft.util.maths.MathHelper;

import java.util.Locale;
import java.util.Objects;

public class Vec2F {
	public float x;
	public float y;
	
	public Vec2F() {}
	
	public Vec2F(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public float lengthSqr() {
		return x * x + y * y;
	}
	
	public float length() {
		return MathHelper.sqrt(lengthSqr());
	}
	
	public Vec2F rotateCW(int index) {
		float nx = x;
		float ny = y;
		switch (index) {
			case 1 -> {
				nx = y;
				ny = -x;
			}
			case 2 -> {
				nx = -x;
				ny = -y;
			}
			case 3 -> {
				nx = -x;
				ny = y;
			}
		}
		return set(nx, ny);
	}
	
	public Vec2F add(float value) {
		return add(value, value);
	}
	
	public Vec2F add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}
	
	public Vec2F add(Vec2F vector) {
		return add(vector.x, vector.y);
	}
	
	public Vec2F set(float value) {
		return set(value, value);
	}
	
	public Vec2F set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Vec2F set(Vec2F vector) {
		return set(vector.x, vector.y);
	}
	
	public Vec2F subtract(float value) {
		return subtract(value, value);
	}
	
	public Vec2F subtract(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}
	
	public Vec2F subtract(Vec2F vector) {
		return subtract(vector.x, vector.y);
	}
	
	public Vec2F multiply(float value) {
		return multiply(value, value);
	}
	
	public Vec2F multiply(float x, float y) {
		this.x *= x;
		this.y *= y;
		return this;
	}
	
	public Vec2F multiply(Vec2F vector) {
		return multiply(vector.x, vector.y);
	}
	
	public Vec2F divide(float value) {
		return divide(value, value);
	}
	
	public Vec2F divide(float x, float y) {
		this.x /= x;
		this.y /= y;
		return this;
	}
	
	public Vec2F divide(Vec2F vector) {
		return divide(vector.x, vector.y);
	}
	
	@Override
	public Vec2F clone() {
		return new Vec2F(x, y);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Vec2F)) return false;
		Vec2F vec = (Vec2F) obj;
		return vec.x == x && vec.y == y;
	}
	
	@Override
	public String toString() {
		return String.format(Locale.ROOT, "[%f, %f]", x, y);
	}
}
