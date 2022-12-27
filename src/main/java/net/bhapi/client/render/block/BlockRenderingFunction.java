package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface BlockRenderingFunction {
	boolean render(BlockState blockState, int x, int y, int z);
}
