package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.render.block.GrassColor;
import net.minecraft.level.BlockView;
import net.minecraft.level.gen.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
	
	@Environment(EnvType.CLIENT)
	@Inject(method = "getColorMultiplier", at = @At("HEAD"), cancellable = true)
	private void bhapi_getColorMultiplier(BlockView view, int x, int y, int z, CallbackInfoReturnable<Integer> info) {
		BiomeSource source = view.getBiomeSource();
		double temperature, wetness;
		synchronized (source) {
			source.getBiomes(x, z, 1, 1);
			temperature = source.temperatureNoises[0];
			wetness = source.rainfallNoises[0];
		}
		temperature = MathUtil.clamp(temperature, 0, 1);
		wetness = MathUtil.clamp(wetness, 0, 1);
		info.setReturnValue(GrassColor.getGrassColor(temperature, wetness));
	}
}
