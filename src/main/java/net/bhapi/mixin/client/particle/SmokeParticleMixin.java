package net.bhapi.mixin.client.particle;

import net.bhapi.client.render.texture.ParticleTextures;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.bhapi.util.MathUtil;
import net.minecraft.client.render.particle.SmokeParticle;
import net.minecraft.entity.technical.ParticleEntity;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmokeParticle.class)
public abstract class SmokeParticleMixin extends ParticleEntity {
	public SmokeParticleMixin(Level arg, double d, double e, double f, double g, double h, double i) {
		super(arg, d, e, f, g, h, i);
	}
	
	@Inject(method = "tick", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/render/particle/SmokeParticle;move(DDD)V",
		shift = Shift.BEFORE
	))
	public void bhapi_tick(CallbackInfo info) {
		int tex = MathUtil.clamp(this.textureIndex, 0, 7);
		TextureSampleProvider.cast(this).bhapi_setTextureSample(ParticleTextures.GENERIC[tex]);
	}
}
