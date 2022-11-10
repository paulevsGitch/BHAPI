package net.bhapi.util;

import net.bhapi.storage.Vec3I;

public enum BlockDirection {
	NEG_Y(0, 0, -1, 0),
	POS_Y(1, 0, 1, 0),
	NEG_Z(2, 0, 0, -1),
	POS_Z(3, 0, 0, 1),
	NEG_X(4, -1, 0, 0),
	POS_X(5, 1, 0, 0);
	
	public static final BlockDirection[] HORIZONTAL = new BlockDirection[] { NEG_X, NEG_Z, POS_X, POS_Z };
	public static final BlockDirection[] VALUES = BlockDirection.values();
	
	private final int facing;
	private final int x;
	private final int y;
	private final int z;
	
	BlockDirection(int facing, int x, int y, int z) {
		this.facing = facing;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public static BlockDirection getFromFacing(int facing) {
		return VALUES[facing];
	}
	
	public Vec3I move(Vec3I vec) {
		return vec.add(x, y, z);
	}
}
