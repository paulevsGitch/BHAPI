package net.bhapi.mixin.common;

import net.bhapi.interfaces.BiomeSourceDataProvider;
import net.bhapi.storage.MultiThreadStorage;
import net.bhapi.storage.vanilla.VanillaBiomeSourceData;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.Level;
import net.minecraft.level.biome.BaseBiome;
import net.minecraft.level.gen.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin implements BiomeSourceDataProvider {
	@Shadow public double[] temperatureNoises;
	@Shadow public double[] rainfallNoises;
	
	@Shadow public abstract BaseBiome[] getBiomes(BaseBiome[] out, int x, int z, int wx, int wz);
	
	@Unique private static long bhapi_seed;
	@Unique private static final MultiThreadStorage<VanillaBiomeSourceData> BHAPI_DATA = new MultiThreadStorage<>(
		() -> new VanillaBiomeSourceData(bhapi_seed)
	);
	
	@Inject(method = "<init>(Lnet/minecraft/level/Level;)V", at = @At("TAIL"))
	private void bhapi_onInit(Level level, CallbackInfo info) {
		if (bhapi_seed != level.getSeed()) {
			bhapi_seed = level.getSeed();
			BHAPI_DATA.clear();
		}
	}
	
	@Environment(value= EnvType.CLIENT)
	@Inject(method = "getTemperature", at = @At("HEAD"), cancellable = true)
	private void bhapi_getTemperature(int x, int z, CallbackInfoReturnable<Double> info) {
		VanillaBiomeSourceData data = BHAPI_DATA.get();
		data.temperatureNoises = data.temperatureNoise.sample(data.temperatureNoises, x, z, 1, 1, 0.025f, 0.025f, 0.5);
		info.setReturnValue(data.temperatureNoises[0]);
	}
	
	@Inject(
		method = "getBiomes(IIII)[Lnet/minecraft/level/biome/BaseBiome;",
		at = @At("HEAD"), cancellable = true
	)
	private void bhapi_getBiomes1(int x, int z, int wx, int wz, CallbackInfoReturnable<BaseBiome[]> info) {
		VanillaBiomeSourceData data = BHAPI_DATA.get();
		data.biomes = this.getBiomes(data.biomes, x, z, wx, wz);
		this.temperatureNoises = data.temperatureNoises;
		this.rainfallNoises = data.rainfallNoises;
		info.setReturnValue(data.biomes);
	}
	
	@Inject(method = "getTemperatures", at = @At("HEAD"), cancellable = true)
	private void bhapi_getTemperatures(double[] out, int x, int z, int wx, int wz, CallbackInfoReturnable<double[]> info) {
		if (out == null || out.length < wx * wz) {
			out = new double[wx * wz];
		}
		
		VanillaBiomeSourceData data = BHAPI_DATA.get();
		out = data.temperatureNoise.sample(out, x, z, wx, wz, 0.025f, 0.025f, 0.25);
		data.detailNoises = data.detailNoise.sample(data.detailNoises, x, z, wx, wz, 0.25, 0.25, 0.5882352941176471);
		
		int index = 0;
		double noise, value;
		for (int dx = 0; dx < wx; ++dx) {
			for (int dz = 0; dz < wz; ++dz) {
				noise = data.detailNoises[index] * 1.1 + 0.5;
				value = (out[index] * 0.15 + 0.7) * 0.99 + noise * 0.01;
				if ((value = 1.0 - (1.0 - value) * (1.0 - value)) < 0.0) {
					value = 0.0;
				}
				if (value > 1.0) {
					value = 1.0;
				}
				out[index++] = value;
			}
		}
		info.setReturnValue(out);
	}
	
	@Inject(
		method = "getBiomes([Lnet/minecraft/level/biome/BaseBiome;IIII)[Lnet/minecraft/level/biome/BaseBiome;",
		at = @At("HEAD"), cancellable = true
	)
	private void bhapi_getBiomes2(BaseBiome[] out, int x, int z, int wx, int wz, CallbackInfoReturnable<BaseBiome[]> info) {
		if (out == null || out.length < wx * wz) {
			out = new BaseBiome[wx * wz];
		}
		
		VanillaBiomeSourceData data = BHAPI_DATA.get();
		data.temperatureNoises = data.temperatureNoise.sample(data.temperatureNoises, x, z, wx, wx, 0.025f, 0.025f, 0.25);
		data.rainfallNoises = data.rainfallNoise.sample(data.rainfallNoises, x, z, wx, wx, 0.05f, 0.05f, 0.3333333333333333);
		data.detailNoises = data.detailNoise.sample(data.detailNoises, x, z, wx, wx, 0.25, 0.25, 0.5882352941176471);
		
		int index = 0;
		double temperature, wetness, noise;
		for (int i2 = 0; i2 < wx; ++i2) {
			for (int i3 = 0; i3 < wz; ++i3) {
				noise = data.detailNoises[index] * 1.1 + 0.5;
				temperature = (data.temperatureNoises[index] * 0.15 + 0.7) * 0.99 + noise * 0.01;
				wetness = (data.rainfallNoises[index] * 0.15 + 0.5) * 0.998 + noise * 0.002;
				temperature = 1.0 - (1.0 - temperature) * (1.0 - temperature);
				
				temperature = MathUtil.clamp(temperature, 0, 1);
				wetness = MathUtil.clamp(wetness, 0, 1);
				
				data.temperatureNoises[index] = temperature;
				data.rainfallNoises[index] = wetness;
				out[index++] = BaseBiome.getBiome(temperature, wetness);
			}
		}
		info.setReturnValue(out);
	}
	
	@Override
	public VanillaBiomeSourceData getBiomeSourceData() {
		return BHAPI_DATA.get();
	}
}
