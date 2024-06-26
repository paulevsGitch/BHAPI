package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.GrassBlock;
import net.minecraft.client.render.block.GrassColor;
import net.minecraft.level.BlockView;
import net.minecraft.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrassBlock.class)
public class GrassBlockMixin implements BHBlockRender {
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
	
	@Override
	public int bhapi_getOverlayCount(BlockView view, int x, int y, int z, BlockState state) {
		return BlockStateProvider.cast(view).bhapi_getBlockState(x, y + 1, z).is(Block.SNOW) ? 1 : 2;
	}
	
	@Override
	public TextureSample bhapi_getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
		TextureSample sample;
		switch (textureIndex) {
			case 0 -> sample = overlayIndex == 0 ? Textures.getVanillaBlockSample(2) : null;
			case 1 -> sample = overlayIndex == 0 ? Textures.getVanillaBlockSample(0) : null;
			default -> {
				int index = overlayIndex == 1 ? 3 : 38;
				if (overlayIndex == 0) {
					if (BlockStateProvider.cast(view).bhapi_getBlockState(x, y + 1, z).is(Block.SNOW)) {
						index = 68;
					}
				}
				sample = Textures.getVanillaBlockSample(index);
			}
		}
		return sample;
	}
}
