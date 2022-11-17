package net.bhapi.mixin.client.particle;

import net.bhapi.client.render.texture.ParticleTextures;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.minecraft.client.render.particle.NoteParticle;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoteParticle.class)
public abstract class NoteParticleMixin {
	@Inject(method = "<init>(Lnet/minecraft/level/Level;DDDDDDF)V", at = @At("TAIL"))
	private void bhapi_onParticleInit(Level arg, double d, double e, double f, double g, double h, double i, float j, CallbackInfo info) {
		TextureSampleProvider.cast(this).setTextureSample(ParticleTextures.NOTE);
	}
}
