package net.bhapi.blockstate;

import net.minecraft.level.Level;

public interface BlockStateProvider {
	boolean setBlockState(int x, int y, int z, BlockState state);
	BlockState getBlockState(int x, int y, int z);
	
	static BlockStateProvider cast(Level level) {
		return (BlockStateProvider) level;
	}
}
