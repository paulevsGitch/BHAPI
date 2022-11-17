package net.bhapi.mixin.client.particle;

import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.client.render.texture.UVPair;
import net.bhapi.item.BHItemRender;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.Identifier;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.particle.PoofParticle;
import net.minecraft.entity.BaseParticle;
import net.minecraft.item.BaseItem;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(PoofParticle.class)
public abstract class PoofParticleMixin extends BaseParticle implements TextureSampleProvider {
	@Unique private static final Map<String, TextureSample> BHAPI_SAMPLES = new HashMap<>();
	
	public PoofParticleMixin(Level arg, double d, double e, double f, double g, double h, double i) {
		super(arg, d, e, f, g, h, i);
	}
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_onParticleInit(Level arg, double d, double e, double f, BaseItem item, CallbackInfo info) {
		TextureSample sample;
		if (item instanceof BHItemRender) sample = BHItemRender.cast(item).getTexture(null);
		else sample = BHAPI_SAMPLES.computeIfAbsent(this.getClass().getName(), n -> {
			Identifier id = CommonRegistries.ITEM_REGISTRY.getID(item);
			id = Identifier.make(id.getModID(), "item/" + id.getName());
			return Textures.getAtlas().getSample(id);
		});
		TextureSampleProvider.cast(this).setTextureSample(sample);
	}
	
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void bhapi_render(Tessellator tessellator, float delta, float dx, float dy, float dz, float width, float height, CallbackInfo info) {
		info.cancel();
		
		TextureSample sample = getTextureSample();
		if (sample == null) return;
		
		UVPair uv = sample.getUV();
		float u1 = uv.getU(0);
		float u2 = uv.getU(1);
		float v1 = uv.getV(0);
		float v2 = uv.getV(1);
		float scale = 0.1F * this.size;
		
		float x = (float) (this.prevX + (this.x - this.prevX) * delta - posX);
		float y = (float) (this.prevY + (this.y - this.prevY) * delta - posY);
		float z = (float) (this.prevZ + (this.z - this.prevZ) * delta - posZ);
		
		float light = this.getBrightnessAtEyes(delta);
		tessellator.color(this.colorR * light, this.colorG * light, this.colorB * light);
		
		tessellator.vertex(x - dx * scale - width * scale, y - dy * scale, z - dz * scale - height * scale, u2, v2);
		tessellator.vertex(x - dx * scale + width * scale, y + dy * scale, z - dz * scale + height * scale, u2, v1);
		tessellator.vertex(x + dx * scale + width * scale, y + dy * scale, z + dz * scale + height * scale, u1, v1);
		tessellator.vertex(x + dx * scale - width * scale, y - dy * scale, z + dz * scale - height * scale, u1, v2);
	}
}
