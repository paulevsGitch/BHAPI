package net.bhapi.interfaces;

import net.bhapi.blockstate.BlockState;

public interface SimpleBlockStateContainer {
	BlockState getBlockState();
	void setBlockState(BlockState state);
}
