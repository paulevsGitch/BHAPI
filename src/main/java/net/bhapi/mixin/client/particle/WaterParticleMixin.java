package net.bhapi.mixin.client.particle;

import net.bhapi.client.render.texture.ParticleTextures;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.bhapi.util.MathUtil;
import net.minecraft.client.render.particle.WaterParticle;
import net.minecraft.entity.BaseParticle;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterParticle.class)
public abstract class WaterParticleMixin extends BaseParticle {
	public WaterParticleMixin(Level arg, double d, double e, double f, double g, double h, double i) {
		super(arg, d, e, f, g, h, i);
	}
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_onParticleInit(Level arg, double d, double e, double f, double g, double h, double i, CallbackInfo info) {
		int tex = MathUtil.clamp(this.textureIndex - 19, 0, 4);
		TextureSampleProvider.cast(this).setTextureSample(ParticleTextures.SPLASH[tex]);
	}
}
