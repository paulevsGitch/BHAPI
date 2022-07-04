package net.bhapi.util;

import net.bhapi.blockstate.BlockState;

public class ChunkSection {
	private BlockState[] blockStates;
	
	public BlockState getState(int x, int y, int z) {
		BlockState state = blockStates[getIndex(x, y, z)];
		return state == null ? BlockState.AIR_STATE : state;
	}
	
	public void setState(int x, int y, int z, BlockState state) {
		blockStates[getIndex(x, y, z)] = state;
	}
	
	private int getIndex(int x, int y, int z) {
		return x << 8 | y << 4 | z;
	}
}
