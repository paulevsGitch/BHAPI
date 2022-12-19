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
	private void bhapi_getColorMultiplier(BlockView arg, int i, int j, int k, CallbackInfoReturnable<Integer> info) {
		BiomeSource source = arg.getBiomeSource();
		double temperature, wetness;
		synchronized (source) {
			arg.getBiomeSource().getBiomes(i, k, 1, 1);
			temperature = arg.getBiomeSource().temperatureNoises[0];
			wetness = arg.getBiomeSource().rainfallNoises[0];
		}
		temperature = MathUtil.clamp(temperature, 0, 1);
		wetness = MathUtil.clamp(wetness, 0, 1);
		info.setReturnValue(GrassColor.getGrassColor(temperature, wetness));
	}
}
