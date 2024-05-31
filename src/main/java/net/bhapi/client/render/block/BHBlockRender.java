package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.model.CustomModel;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.level.BlockView;

public interface BHBlockRender {
	/**
	 * Get render type for this block, full cube by default.
	 * @see BlockRenderTypes
	 * @param view {@link BlockView}
	 * @param x X block coordinate
	 * @param y Y block coordinate
	 * @param z Z block coordinate
	 * @param state current {@link BlockState}
	 */
	@Environment(EnvType.CLIENT)
	default byte bhapi_getRenderType(BlockView view, int x, int y, int z, BlockState state) {
		return (byte) state.getBlock().getRenderType();
	}
	
	/**
	 * Get texture for current model uvID. Vanilla blocks have indexes equal to quad face directions, custom models
	 * can have any indexes.
	 * @see net.bhapi.util.BlockDirection
	 * @param view {@link BlockView}
	 * @param x X block coordinate
	 * @param y Y block coordinate
	 * @param z Z block coordinate
	 * @param state current {@link BlockState}
	 * @param textureIndex current texture uvID
	 * @param overlayIndex current overlay uvID
	 * @return {@link TextureSample} or null
	 */
	@Environment(EnvType.CLIENT)
	default TextureSample bhapi_getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
		Block block = state.getBlock();
		int texture = block.getTexture(view, x, y, z, textureIndex);
		return Textures.getVanillaBlockSample(texture);
	}
	
	/**
	 * Get custom block model. Will be used only with BlockRenderTypes.CUSTOM.
	 * @see BlockRenderTypes
	 * @param view {@link BlockView}
	 * @param x X block coordinate
	 * @param y Y block coordinate
	 * @param z Z block coordinate
	 * @param state current {@link BlockState}
	 * @return {@link CustomModel}
	 */
	@Environment(EnvType.CLIENT)
	default CustomModel bhapi_getModel(BlockView view, int x, int y, int z, BlockState state) {
		return null;
	}
	
	/**
	 * Get count of overlay textures for this block.
	 * Each overlay will be rendered as same model, but with different textures.
	 */
	@Environment(EnvType.CLIENT)
	default int bhapi_getOverlayCount(BlockView view, int x, int y, int z, BlockState state) {
		return 1;
	}
	
	static BHBlockRender cast(Object obj) {
		return (BHBlockRender) obj;
	}
}
