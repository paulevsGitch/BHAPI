package net.bhapi.client.render.model;

import net.bhapi.util.BlockDirection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum FaceGroup {
	NEG_Y(BlockDirection.NEG_Y),
	POS_Y(BlockDirection.POS_Y),
	NEG_Z(BlockDirection.NEG_Z),
	POS_Z(BlockDirection.POS_Z),
	NEG_X(BlockDirection.NEG_X),
	POS_X(BlockDirection.POS_X),
	NONE(null);
	
	public static final FaceGroup[] VALUES = values();
	private final BlockDirection direction;
	
	FaceGroup(BlockDirection direction) {
		this.direction = direction;
	}
	
	public BlockDirection getDirection() {
		return direction;
	}
	
	public static FaceGroup getFromFacing(BlockDirection dir) {
		return VALUES[dir.ordinal()];
	}
}
