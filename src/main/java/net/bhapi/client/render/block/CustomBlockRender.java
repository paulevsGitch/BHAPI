package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.level.BlockView;

public interface CustomBlockRender {
	@Environment(EnvType.CLIENT)
	default byte getRenderType(BlockView view, int x, int y, int z, BlockState state) {
		return (byte) state.getBlock().getRenderType();
	}
	
	@Environment(EnvType.CLIENT)
	default TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int index) {
		BaseBlock block = state.getBlock();
		int texture = block.getTextureForSide(view, x, y, z, index);
		return Textures.getVanillaBlockSample(texture);
	}
	
	static CustomBlockRender cast(Object obj) {
		return (CustomBlockRender) obj;
	}
}
