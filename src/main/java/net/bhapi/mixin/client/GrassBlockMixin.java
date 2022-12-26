package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.util.MathUtil;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.GrassBlock;
import net.minecraft.client.render.block.GrassColor;
import net.minecraft.level.BlockView;
import net.minecraft.level.gen.BiomeSource;
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
	public int getOverlayCount(BlockView view, int x, int y, int z, BlockState state) {
		return 2;//BlockStateProvider.cast(view).getBlockState(x, y + 1, z).is(BaseBlock.SNOW) ? 1 : 2;
	}
	
	@Override
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
		TextureSample sample;
		switch (textureIndex) {
			case 0 -> sample = overlayIndex == 1 ? Textures.getVanillaBlockSample(2) : null;
			case 1 -> sample = overlayIndex == 1 ? Textures.getVanillaBlockSample(0) : null;
			default -> {
				int index;
				if (overlayIndex == 1) {
					if (BlockStateProvider.cast(view).getBlockState(x, y + 1, z).is(BaseBlock.SNOW)) index = 68;
					else index = 3;
				}
				else index = 38;
				sample = Textures.getVanillaBlockSample(index);
			}
		}
		return sample;
	}
}
