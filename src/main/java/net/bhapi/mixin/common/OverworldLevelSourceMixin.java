package net.bhapi.mixin.common;

import net.bhapi.interfaces.BiomeSourceDataProvider;
import net.bhapi.storage.Vec2I;
import net.bhapi.storage.vanilla.VanillaBiomeSourceData;
import net.minecraft.level.gen.BiomeSource;
import net.minecraft.level.source.OverworldLevelSource;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OverworldLevelSource.class)
public class OverworldLevelSourceMixin {
	private final Vec2I start = new Vec2I();
	
	@Inject(method = "calculateNoise", at = @At("HEAD"))
	private void bhapi_calculateNoise(double[] ds, int x, int y, int z, int dx, int dy, int dz, CallbackInfoReturnable<double[]> info) {
		start.set(x, z);
	}
	
	@Redirect(method = "calculateNoise", at = @At(
		value = "FIELD",
		target = "Lnet/minecraft/level/gen/BiomeSource;temperatureNoises:[D",
		opcode = Opcodes.GETFIELD)
	)
	private double[] bhapi_changeTemperature(BiomeSource source) {
		BiomeSourceDataProvider provider = BiomeSourceDataProvider.cast(source);
		VanillaBiomeSourceData data = provider.getBiomeSourceData();
		return data.temperatureNoises;
	}
	
	@Redirect(method = "calculateNoise", at = @At(
		value = "FIELD",
		target = "Lnet/minecraft/level/gen/BiomeSource;rainfallNoises:[D",
		opcode = Opcodes.GETFIELD)
	)
	private double[] bhapi_changeRainfallNoises(BiomeSource source) {
		BiomeSourceDataProvider provider = BiomeSourceDataProvider.cast(source);
		VanillaBiomeSourceData data = provider.getBiomeSourceData();
		return data.rainfallNoises;
	}
	
	@Redirect(method = "getChunk", at = @At(
		value = "FIELD",
		target = "Lnet/minecraft/level/gen/BiomeSource;temperatureNoises:[D",
		opcode = Opcodes.GETFIELD)
	)
	private double[] bhapi_changeGetChunk(BiomeSource source) {
		BiomeSourceDataProvider provider = BiomeSourceDataProvider.cast(source);
		VanillaBiomeSourceData data = provider.getBiomeSourceData();
		return data.temperatureNoises;
	}
}
