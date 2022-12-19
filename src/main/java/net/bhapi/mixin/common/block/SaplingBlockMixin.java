package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockMixin extends PlantBlock implements BlockStateContainer, BHBlockRender {
	protected SaplingBlockMixin(int i, int j) {
		super(i, j);
	}
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16); // Saplings use | 8 for some states
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
		return Textures.getVanillaBlockSample(getTextureForSide(0, state.getMeta()));
	}
}

