package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.level.BlockView;

public class CustomBlockRenderer {
	private static BlockRenderer renderer;
	private static BlockView view;
	
	public static void setRenderer(BlockView view, BlockRenderer renderer) {
		CustomBlockRenderer.renderer = renderer;
		CustomBlockRenderer.view = view;
	}
	
	public static boolean render(BlockState state, int x, int y, int z) {
		byte type = CustomBlockRender.cast(state.getBlock()).getRenderType(view, x, y, z, state);
		if (type == BlockRenderTypes.EMPTY) return true;
		if (type == BlockRenderTypes.CUSTOM) return true; // TODO make custom rendering
		else if (BlockRenderTypes.isVanilla(type)) {
			return renderer.render(state.getBlock(), x, y, z);
		}
		return false;
	}
}
