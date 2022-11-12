package net.bhapi.level;

import net.bhapi.blockstate.BlockState;

public interface PlaceChecker {
	boolean canPlaceState(BlockState state, int x, int y, int z, boolean flag, int facing);
	
	static PlaceChecker cast(Object obj) {
		return (PlaceChecker) obj;
	}
}
