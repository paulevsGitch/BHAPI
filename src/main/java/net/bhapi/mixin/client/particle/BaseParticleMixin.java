package net.bhapi.mixin.client.particle;

import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.bhapi.storage.Vec2F;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.BaseParticle;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseParticle.class)
public abstract class BaseParticleMixin extends BaseEntity implements TextureSampleProvider {
	@Shadow public static double posX;
	@Shadow public static double posY;
	@Shadow public static double posZ;
	
	@Shadow protected float colorR;
	@Shadow protected float colorG;
	@Shadow protected float colorB;
	@Shadow protected float size;
	
	@Unique private TextureSample bhapi_sample;
	
	public BaseParticleMixin(Level arg) {
		super(arg);
	}
	
	@Unique
	@Override
	public TextureSample getTextureSample() {
		return bhapi_sample;
	}
	
	@Unique
	@Override
	public void setTextureSample(TextureSample sample) {
		bhapi_sample = sample;
	}
	
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void bhapi_render(Tessellator tessellator, float delta, float dx, float dy, float dz, float width, float height, CallbackInfo info) {
		info.cancel();
		
		TextureSample sample = getTextureSample();
		if (sample == null) return;
		
		Vec2F uv1 = sample.getUV(0, 0);
		Vec2F uv2 = sample.getUV(1, 1);
		float scale = 0.1F * this.size;
		
		float x = (float) (this.prevX + (this.x - this.prevX) * delta - posX);
		float y = (float) (this.prevY + (this.y - this.prevY) * delta - posY);
		float z = (float) (this.prevZ + (this.z - this.prevZ) * delta - posZ);
		
		float light = this.getBrightnessAtEyes(delta);
		tessellator.color(this.colorR * light, this.colorG * light, this.colorB * light);
		
		tessellator.vertex(x - dx * scale - width * scale, y - dy * scale, z - dz * scale - height * scale, uv2.x, uv2.y);
		tessellator.vertex(x - dx * scale + width * scale, y + dy * scale, z - dz * scale + height * scale, uv2.x, uv1.y);
		tessellator.vertex(x + dx * scale + width * scale, y + dy * scale, z + dz * scale + height * scale, uv1.x, uv1.y);
		tessellator.vertex(x + dx * scale - width * scale, y - dy * scale, z + dz * scale - height * scale, uv1.x, uv2.y);
	}
}
