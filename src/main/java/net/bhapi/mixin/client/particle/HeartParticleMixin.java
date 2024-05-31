package net.bhapi.mixin.client.particle;

import net.bhapi.client.render.texture.ParticleTextures;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.minecraft.client.render.particle.HeartParticle;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeartParticle.class)
public class HeartParticleMixin {
	@Inject(method = "<init>(Lnet/minecraft/level/Level;DDDDDDF)V", at = @At("TAIL"))
	private void bhapi_onParticleInit(Level d, double e, double f, double g, double h, double i, double j, float par8, CallbackInfo ci) {
		TextureSampleProvider.cast(this).bhapi_setTextureSample(ParticleTextures.HEART);
	}
}
