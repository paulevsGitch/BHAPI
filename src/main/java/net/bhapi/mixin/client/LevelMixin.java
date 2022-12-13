package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.util.MathUtil;
import net.minecraft.entity.BaseEntity;
import net.minecraft.level.Level;
import net.minecraft.level.dimension.BaseDimension;
import net.minecraft.level.dimension.NetherDimension;
import net.minecraft.level.gen.BiomeSource;
import net.minecraft.util.maths.MathHelper;
import net.minecraft.util.maths.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Shadow public Random random;
	@Shadow public int brightnessTicks;
	
	@Shadow public abstract float getSunAngle(float f);
	@Shadow public abstract BiomeSource getBiomeSource();
	@Shadow public abstract float getRainGradient(float f);
	@Shadow public abstract float getThunderGradient(float f);
	
	@Shadow private long skyColor;
	@Shadow @Final public BaseDimension dimension;
	@Unique private Random bhapi_clientRandom = new Random();
	@Unique private Vec3f bhapi_skyColor = Vec3f.make(0, 0, 0);
	@Unique private Vec3f bhapi_sunColor = Vec3f.make(0, 0, 0);
	
	@Inject(method = "shuffleSpawnPoint", at = @At("HEAD"), cancellable = true)
	private void bhapi_fixShuffleSpawnPoint(CallbackInfo info) {
		info.cancel();
	}
	
	@Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
	public void bhapi_randomDisplayTick(int x, int y, int z, CallbackInfo info) {
		info.cancel();
		for (int count = 0; count < 1000; ++count) {
			int px = x + this.bhapi_clientRandom.nextInt(32) - 16;
			int py = y + this.bhapi_clientRandom.nextInt(32) - 16;
			int pz = z + this.bhapi_clientRandom.nextInt(32) - 16;
			BlockState state = BlockStateProvider.cast(this).getBlockState(px, py, pz);
			if (!state.hasRandomTicks()) continue;
			state.getBlock().randomDisplayTick(Level.class.cast(this), px, py, pz, bhapi_clientRandom);
		}
	}
	
	@Inject(
		method = "getSkyColor(Lnet/minecraft/entity/BaseEntity;F)Lnet/minecraft/util/maths/Vec3f;",
		at = @At("HEAD"), cancellable = true
	)
	private void bhapi_getSkyColor(BaseEntity entity, float delta, CallbackInfoReturnable<Vec3f> info) {
		float light1, light2;
		float angle = this.getSunAngle(delta);
		float intensity = MathHelper.cos(angle * (float) Math.PI * 2.0f) * 2.0f + 0.5f;
		intensity = MathUtil.clamp(intensity, 0, 1);
		
		int ix = MathHelper.floor(entity.x);
		int iz = MathHelper.floor(entity.z);
		float f6 = (float) this.getBiomeSource().getTemperature(ix, iz);
		int skyColor = this.getBiomeSource().getBiome(ix, iz).getSkyColor(f6);
		
		float cr = (skyColor >> 16 & 0xFF) / 255.0f;
		float cg = (skyColor >> 8 & 0xFF) / 255.0f;
		float cb = (skyColor & 0xFF) / 255.0f;
		
		cr *= intensity;
		cg *= intensity;
		cb *= intensity;
		
		float f10 = this.getRainGradient(delta);
		
		if (f10 > 0.0f) {
			light2 = (cr * 0.3f + cg * 0.59f + cb * 0.11f) * 0.6f;
			light1 = 1.0f - f10 * 0.75f;
			cr = cr * light1 + light2 * (1.0f - light1);
			cg = cg * light1 + light2 * (1.0f - light1);
			cb = cb * light1 + light2 * (1.0f - light1);
		}
		
		if ((light2 = this.getThunderGradient(delta)) > 0.0f) {
			light1 = (cr * 0.3f + cg * 0.59f + cb * 0.11f) * 0.2f;
			float f11 = 1.0f - light2 * 0.75f;
			cr = cr * f11 + light1 * (1.0f - f11);
			cg = cg * f11 + light1 * (1.0f - f11);
			cb = cb * f11 + light1 * (1.0f - f11);
		}
		
		if (this.brightnessTicks > 0) {
			light1 = (float) this.brightnessTicks - delta;
			if (light1 > 1.0f) light1 = 1.0f;
			light1 *= 0.45f;
			cr = cr * (1.0f - light1) + 0.8f * light1;
			cg = cg * (1.0f - light1) + 0.8f * light1;
			cb = cb * (1.0f - light1) + light1;
		}
		
		bhapi_skyColor.x = cr;
		bhapi_skyColor.y = cg;
		bhapi_skyColor.z = cb;
		
		info.setReturnValue(bhapi_skyColor);
	}
	
	@Inject(method = "getSunPosition", at = @At("HEAD"), cancellable = true)
	private void bhapi_getSunPosition(float delta, CallbackInfoReturnable<Vec3f> info) {
		float light1, light2;
		float angle = this.getSunAngle(delta);
		float intensity = MathHelper.cos(angle * (float)Math.PI * 2.0f) * 2.0f + 0.5f;
		intensity = MathUtil.clamp(intensity, 0, 1);
		float cr = (this.skyColor >> 16 & 0xFFL) / 255.0f;
		float cg = (this.skyColor >> 8 & 0xFFL) / 255.0f;
		float cb = (this.skyColor & 0xFFL) / 255.0f;
		float rain = this.getRainGradient(delta);
		
		if (rain > 0.0f) {
			light2 = (cr * 0.3f + cg * 0.59f + cb * 0.11f) * 0.6f;
			light1 = 1.0f - rain * 0.95f;
			cr = cr * light1 + light2 * (1.0f - light1);
			cg = cg * light1 + light2 * (1.0f - light1);
			cb = cb * light1 + light2 * (1.0f - light1);
		}
		
		cr *= intensity * 0.9f + 0.1f;
		cg *= intensity * 0.9f + 0.1f;
		cb *= intensity * 0.85f + 0.15f;
		
		light2 = this.getThunderGradient(delta);
		if (light2 > 0.0f) {
			light1 = (cr * 0.3f + cg * 0.59f + cb * 0.11f) * 0.2f;
			float f10 = 1.0f - light2 * 0.95f;
			cr = cr * f10 + light1 * (1.0f - f10);
			cg = cg * f10 + light1 * (1.0f - f10);
			cb = cb * f10 + light1 * (1.0f - f10);
		}
		
		bhapi_sunColor.x = cr;
		bhapi_sunColor.y = cg;
		bhapi_sunColor.z = cb;
		info.setReturnValue(bhapi_sunColor);
	}
	
	@Inject(method = "getThunderGradient", at = @At("HEAD"), cancellable = true)
	private void bhapi_getThunderGradient(float delta, CallbackInfoReturnable<Float> info) {
		if (this.dimension instanceof NetherDimension) info.setReturnValue(0F);
	}
	
	@Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
	private void bhapi_getRainGradient(float delta, CallbackInfoReturnable<Float> info) {
		if (this.dimension instanceof NetherDimension) info.setReturnValue(0F);
	}
}
