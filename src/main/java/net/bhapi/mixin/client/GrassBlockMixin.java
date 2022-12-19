package net.bhapi.mixin.client;

import net.bhapi.util.MathUtil;
import net.minecraft.block.GrassBlock;
import net.minecraft.client.render.block.GrassColor;
import net.minecraft.level.BlockView;
import net.minecraft.level.gen.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrassBlock.class)
public class GrassBlockMixin {
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
