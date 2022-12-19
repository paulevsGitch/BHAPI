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
import net.minecraft.block.BaseBlock;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.block.material.Material;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(TallGrassBlock.class)
public abstract class TallGrassBlockMixin extends BaseBlock implements BlockStateContainer, BHBlockRender {
	protected TallGrassBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_3); // Grass have 3 textures
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
		return Textures.getVanillaBlockSample(getTextureForSide(0, state.getMeta()));
	}
}
