package net.bhapi.mixin.client.particle;

import net.bhapi.client.render.texture.ParticleTextures;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.minecraft.client.render.particle.LavaParticle;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaParticle.class)
public abstract class LavaParticleMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_onParticleInit(Level arg, double d, double e, double f, CallbackInfo info) {
		TextureSampleProvider.cast(this).setTextureSample(ParticleTextures.LAVA);
	}
}
