package net.bhapi.mixin.client.particle;

import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.bhapi.storage.Vec2F;
import net.bhapi.util.MathUtil;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.particle.DiggingParticle;
import net.minecraft.entity.technical.ParticleEntity;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DiggingParticle.class)
public abstract class DiggingParticleMixin extends ParticleEntity implements TextureSampleProvider {
	public DiggingParticleMixin(Level arg, double d, double e, double f, double g, double h, double i) {
		super(arg, d, e, f, g, h, i);
	}
	
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void bhapi_render(Tessellator tessellator, float delta, float x, float y, float z, float width, float height, CallbackInfo info) {
		info.cancel();
		
		TextureSample sample = TextureSampleProvider.cast(this).bhapi_getTextureSample();
		if (sample == null) return;
		
		float u = this.deltaU / 16F;
		float v = this.deltaU / 16F;
		Vec2F uv1 = sample.getUV(u, v);
		Vec2F uv2 = sample.getUV(u + 0.25F, v + 0.25F);
		float scale = 0.1f * this.size;
		
		float dx = (float) (MathUtil.lerp(this.prevX, this.x, delta) - posX);
		float dy = (float) (MathUtil.lerp(this.prevY, this.y, delta) - posY);
		float dz = (float) (MathUtil.lerp(this.prevZ, this.z, delta) - posZ);
		
		float light = this.getBrightnessAtEyes(delta);
		
		tessellator.color(light * this.colorR, light * this.colorG, light * this.colorB);
		tessellator.vertex(dx - x * scale - width * scale, dy - y * scale, dz - z * scale - height * scale, uv1.x, uv2.y);
		tessellator.vertex(dx - x * scale + width * scale, dy + y * scale, dz - z * scale + height * scale, uv1.x, uv1.y);
		tessellator.vertex(dx + x * scale + width * scale, dy + y * scale, dz + z * scale + height * scale, uv2.x, uv1.y);
		tessellator.vertex(dx + x * scale - width * scale, dy - y * scale, dz + z * scale - height * scale, uv2.x, uv2.y);
	}
}
