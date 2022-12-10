package net.bhapi.util;

import net.bhapi.storage.Vec3F;
import net.bhapi.storage.Vec3I;

public enum BlockDirection {
	NEG_Y(0, "NEG_Y", 0, -1, 0),
	POS_Y(1, "POS_Y", 0, 1, 0),
	NEG_Z(2, "NEG_Z", 0, 0, -1),
	POS_Z(3, "POS_Z", 0, 0, 1),
	NEG_X(4, "NEG_X", -1, 0, 0),
	POS_X(5, "POS_X", 1, 0, 0);
	
	public static final BlockDirection[] HORIZONTAL = new BlockDirection[] { NEG_X, NEG_Z, POS_X, POS_Z };
	public static final BlockDirection[] VALUES = BlockDirection.values();
	
	private final String name;
	private final int facing;
	private final int x;
	private final int y;
	private final int z;
	
	BlockDirection(int facing, String name, int x, int y, int z) {
		this.facing = facing;
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public static BlockDirection getFromFacing(int facing) {
		return VALUES[facing];
	}
	
	public static BlockDirection getFromVector(Vec3F dir) {
		float ax = Math.abs(dir.x);
		float ay = Math.abs(dir.y);
		float az = Math.abs(dir.z);
		float max = Math.max(ax, Math.max(ay, az));
		if (max == ax) return dir.x < 0 ? NEG_X : POS_X;
		if (max == ay) return dir.y < 0 ? NEG_Y : POS_Y;
		else return dir.z < 0 ? NEG_Z : POS_Z;
	}
	
	public BlockDirection invert() {
		return (facing & 1) == 0 ? VALUES[facing + 1] : VALUES[facing - 1];
	}
	
	public Vec3I move(Vec3I vec) {
		return vec.add(x, y, z);
	}
	
	public int getFacing() {
		return facing;
	}
	
	public Vec3F makeVec3F() {
		return new Vec3F(x, y, z);
	}
	
	public Vec3I makeVec3I() {
		return new Vec3I(x, y, z);
	}
	
	public String getName() {
		return name;
	}
	
	public static BlockDirection getByName(String name) {
		for (BlockDirection dir: VALUES) {
			if (dir.name.equals(name)) return dir;
		}
		return null;
	}
}
