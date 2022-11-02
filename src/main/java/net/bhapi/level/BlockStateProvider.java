package net.bhapi.level;

import net.bhapi.blockstate.BlockState;

public interface BlockStateProvider {
	boolean setBlockState(int x, int y, int z, BlockState state);
	BlockState getBlockState(int x, int y, int z);
	
	static BlockStateProvider cast(Object obj) {
		return (BlockStateProvider) obj;
	}
}
