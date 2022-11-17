package net.bhapi.mixin.client.particle;

import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.util.Identifier;
import net.minecraft.client.render.particle.BubbleParticle;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BubbleParticle.class)
public abstract class BubbleParticleMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_onParticleInit(Level arg, double d, double e, double f, double g, double h, double i, CallbackInfo info) {
		TextureSample sample = Textures.getAtlas().getSample(Identifier.make("particle_32"));
		TextureSampleProvider.cast(this).setTextureSample(sample);
	}
}
