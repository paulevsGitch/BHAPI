package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;

@FunctionalInterface
public interface BlockRenderingFunction {
	boolean render(BlockState blockState, int x, int y, int z);
}
