package net.bhapi.mixin.common;

import net.bhapi.interfaces.BiomeSourceDataProvider;
import net.bhapi.storage.vanilla.VanillaBiomeSourceData;
import net.minecraft.level.biome.BaseBiome;
import net.minecraft.level.gen.BiomeSource;
import net.minecraft.level.gen.FixedBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(FixedBiomeSource.class)
public abstract class FixedBiomeSourceMixin extends BiomeSource implements BiomeSourceDataProvider {
	@Shadow private BaseBiome biome;
	@Shadow private double rainfall;
	@Shadow private double temperature;
	
	@Shadow public abstract BaseBiome[] getBiomes(BaseBiome[] args, int i, int j, int k, int l);
	
	@Inject(method = "getBiomes(IIII)[Lnet/minecraft/level/biome/BaseBiome;", at = @At("HEAD"), cancellable = true)
	private void bhapi_getBiomes1(int x, int z, int wx, int wz, CallbackInfoReturnable<BaseBiome[]> info) {
		VanillaBiomeSourceData data = getBiomeSourceData();
		data.biomes = this.getBiomes(data.biomes, x, z, wx, wz);
		this.temperatureNoises = data.temperatureNoises;
		this.rainfallNoises = data.rainfallNoises;
		info.setReturnValue(data.biomes);
	}
	
	@Inject(
		method = "getBiomes([Lnet/minecraft/level/biome/BaseBiome;IIII)[Lnet/minecraft/level/biome/BaseBiome;",
		at = @At("HEAD"), cancellable = true
	)
	private void bhapi_getBiomes2(BaseBiome[] out, int x, int z, int dx, int dz, CallbackInfoReturnable<BaseBiome[]> info) {
		if (out == null || out.length < dx * dz) {
			out = new BaseBiome[dx * dz];
		}
		
		VanillaBiomeSourceData data = getBiomeSourceData();
		
		if (data.temperatureNoises == null || data.temperatureNoises.length < dx * dz) {
			data.temperatureNoises = new double[dx * dz];
			data.rainfallNoises = new double[dx * dz];
		}
		
		Arrays.fill(out, 0, dx * dz, this.biome);
		Arrays.fill(data.rainfallNoises, 0, dx * dz, this.rainfall);
		Arrays.fill(data.temperatureNoises, 0, dx * dz, this.temperature);
		
		info.setReturnValue(out);
	}
}
