package net.bhapi.mixin.client.particle;

import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.item.BHItemRender;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.storage.Vec2F;
import net.bhapi.util.Identifier;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.particle.PoofParticle;
import net.minecraft.entity.technical.ParticleEntity;
import net.minecraft.item.Item;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(PoofParticle.class)
public abstract class PoofParticleMixin extends ParticleEntity implements TextureSampleProvider {
	@Unique private static final Map<String, TextureSample> BHAPI_SAMPLES = new HashMap<>();
	
	public PoofParticleMixin(Level arg, double d, double e, double f, double g, double h, double i) {
		super(arg, d, e, f, g, h, i);
	}
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_onParticleInit(Level arg, double d, double e, double f, Item item, CallbackInfo info) {
		TextureSample sample;
		if (item instanceof BHItemRender) sample = BHItemRender.cast(item).getTexture(null);
		else sample = BHAPI_SAMPLES.computeIfAbsent(this.getClass().getName(), n -> {
			Identifier id = CommonRegistries.ITEM_REGISTRY.getID(item);
			id = Identifier.make(id.getModID(), "item/" + id.getName());
			return Textures.getAtlas().getSample(id);
		});
		TextureSampleProvider.cast(this).bhapi_setTextureSample(sample);
	}
	
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void bhapi_render(Tessellator tessellator, float delta, float dx, float dy, float dz, float width, float height, CallbackInfo info) {
		info.cancel();
		
		TextureSample sample = bhapi_getTextureSample();
		if (sample == null) return;
		
		float u1 = this.deltaU / 16F;
		float v1 = this.deltaV / 16F;
		Vec2F uv1 = sample.getUV(u1, v1);
		Vec2F uv2 = sample.getUV(u1 + 0.25F, v1 + 0.25F);
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
