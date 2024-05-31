package net.bhapi.mixin.client.particle;

import net.bhapi.client.render.texture.ParticleTextures;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.minecraft.client.render.particle.RedstoneParticle;
import net.minecraft.entity.technical.ParticleEntity;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedstoneParticle.class)
public abstract class RedstoneParticleMixin extends ParticleEntity {
	public RedstoneParticleMixin(Level arg, double d, double e, double f, double g, double h, double i) {
		super(arg, d, e, f, g, h, i);
	}
	
	@Inject(method = "tick", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/render/particle/RedstoneParticle;move(DDD)V",
		shift = Shift.BEFORE
	))
	public void bhapi_tick(CallbackInfo info) {
		if (this.textureIndex < 0 || this.textureIndex > 7) return;
		TextureSampleProvider.cast(this).bhapi_setTextureSample(ParticleTextures.GENERIC[this.textureIndex]);
	}
}
