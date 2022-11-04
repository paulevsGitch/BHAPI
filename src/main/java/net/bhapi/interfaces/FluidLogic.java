package net.bhapi.interfaces;

import net.bhapi.blockstate.BlockState;

public interface FluidLogic {
	boolean stateBlocksFluid(BlockState selfState, BlockState blockingState);
}
