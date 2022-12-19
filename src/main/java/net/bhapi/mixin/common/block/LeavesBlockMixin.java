package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.render.block.FoliageColor;
import net.minecraft.level.BlockView;
import net.minecraft.level.gen.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin implements BlockStateContainer {
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16); // Leaves use meta | 8 for updating states.
	}
	
	@Environment(value= EnvType.CLIENT)
	@Inject(method = "getColorMultiplier", at = @At("HEAD"), cancellable = true)
	private void bhapi_getColorMultiplier(BlockView view, int x, int y, int z, CallbackInfoReturnable<Integer> info) {
		int meta = view.getBlockMeta(x, y, z);
		
		if ((meta & 1) == 1) {
			info.setReturnValue(FoliageColor.getSpruceColor());
			return;
		}
		if ((meta & 2) == 2) {
			info.setReturnValue(FoliageColor.getBirchColor());
			return;
		}
		
		BiomeSource source = view.getBiomeSource();
		double temperature, wetness;
		synchronized (source) {
			source.getBiomes(x, z, 1, 1);
			temperature = source.temperatureNoises[0];
			wetness = source.rainfallNoises[0];
		}
		temperature = MathUtil.clamp(temperature, 0, 1);
		wetness = MathUtil.clamp(wetness, 0, 1);
		info.setReturnValue(FoliageColor.getFoliageColor(temperature, wetness));
	}
}
