package net.bhapi.storage;

import java.util.Locale;

public class Vec2I {
	public int x;
	public int y;
	
	public Vec2I() {}
	
	public Vec2I(int x, int y) {
		this.x = x;
		this.y = y;
	}
    
    public Vec2I add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }
    
    public Vec2I add(Vec2I vector) {
        return add(vector.x, vector.y);
    }
    
    public Vec2I set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    public Vec2I set(Vec2I vector) {
        return set(vector.x, vector.y);
    }
    
    public Vec2I subtract(int value) {
        return subtract(value, value);
    }
    
    public Vec2I subtract(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }
    
    public Vec2I subtract(Vec2I vector) {
        return subtract(vector.x, vector.y);
    }
	
    @Override
    public Vec2I clone() {
	    return new Vec2I(x, y);
    }
    
    @Override
    public int hashCode() {
		return x * 31 + y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Vec2I)) {
			return false;
		}
		Vec2I vec = (Vec2I) obj;
		return vec.x == x && vec.y == y;
	}
	
	@Override
	public String toString() {
		return String.format(Locale.ROOT, "[%d, %d, %d]", x, y);
	}
}
