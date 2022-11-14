package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.BlockView;

public interface CustomBlockRender {
	@Environment(EnvType.CLIENT)
	default byte getRenderType(BlockView view, int x, int y, int z, BlockState state) {
		return (byte) state.getBlock().getRenderType();
	}
	
	static CustomBlockRender cast(Object obj) {
		return (CustomBlockRender) obj;
	}
}
