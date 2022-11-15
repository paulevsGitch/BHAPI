package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.texture.TextureAtlas;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.client.render.texture.UVPair;
import net.bhapi.interfaces.SimpleBlockStateContainer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.particle.DiggingParticle;
import net.minecraft.entity.BaseParticle;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DiggingParticle.class)
public abstract class DiggingParticleMixin extends BaseParticle implements SimpleBlockStateContainer {
	@Shadow private int facing;
	@Unique private BlockState bhapi_state;
	@Unique private UVPair bhapi_uv;
	
	public DiggingParticleMixin(Level arg, double d, double e, double f, double g, double h, double i) {
		super(arg, d, e, f, g, h, i);
	}
	
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void bhapi_render(Tessellator tessellator, float delta, float x, float y, float z, float width, float height, CallbackInfo info) {
		info.cancel();
		if (bhapi_uv == null) return;
		
		float u = this.deltaU / 16F;
		float v = this.deltaU / 16F;
		float u1 = bhapi_uv.getU(u);
		float u2 = bhapi_uv.getU(u + 0.25F);
		float v1 = bhapi_uv.getV(v);
		float v2 = bhapi_uv.getV(v + 0.25F);
		float scale = 0.1f * this.size;
		
		float f7 = (float) (this.prevX + (this.x - this.prevX) * delta - posX);
		float f8 = (float) (this.prevY + (this.y - this.prevY) * delta - posY);
		float f9 = (float) (this.prevZ + (this.z - this.prevZ) * delta - posZ);
		
		float light = this.getBrightnessAtEyes(delta);
		
		tessellator.color(light * this.colorR, light * this.colorG, light * this.colorB);
		tessellator.vertex(f7 - x * scale - width * scale, f8 - y * scale, f9 - z * scale - height * scale, u1, v2);
		tessellator.vertex(f7 - x * scale + width * scale, f8 + y * scale, f9 - z * scale + height * scale, u1, v1);
		tessellator.vertex(f7 + x * scale + width * scale, f8 + y * scale, f9 + z * scale + height * scale, u2, v1);
		tessellator.vertex(f7 + x * scale - width * scale, f8 - y * scale, f9 + z * scale - height * scale, u2, v2);
	}
	
	@Override
	public BlockState getBlockState() {
		return bhapi_state;
	}
	
	@Override
	public void setBlockState(BlockState state) {
		bhapi_state = state;
		TextureSample texture = state.getTextureForIndex(this.level, (int) this.x, (int) this.y, (int) this.z, this.facing);
		bhapi_uv = texture.getUV();
	}
}
