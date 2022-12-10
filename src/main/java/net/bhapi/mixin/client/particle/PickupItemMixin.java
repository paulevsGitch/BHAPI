package net.bhapi.mixin.client.particle;

import net.bhapi.util.MathUtil;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.particle.PickupItem;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.BaseParticle;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PickupItem.class)
public abstract class PickupItemMixin extends BaseParticle {
	@Shadow private int particleAge;
	@Shadow private int maxParticleAge;
	@Shadow private BaseEntity itemEntity;
	@Shadow private BaseEntity magnetTarget;
	@Shadow private float yOffset;
	
	public PickupItemMixin(Level arg, double d, double e, double f, double g, double h, double i) {
		super(arg, d, e, f, g, h, i);
	}
	
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void render(Tessellator arg, float delta, float g, float h, float i, float j, float k, CallbackInfo info) {
		info.cancel();
		
		float delta2 = (this.particleAge + delta) / this.maxParticleAge;
		delta2 *= delta2;
		
		double tx = MathUtil.lerp(this.magnetTarget.prevRenderX, this.magnetTarget.x, delta);
		double ty = MathUtil.lerp(this.magnetTarget.prevRenderY, this.magnetTarget.y, delta);
		double tz = MathUtil.lerp(this.magnetTarget.prevRenderZ, this.magnetTarget.z, delta);
		
		float dx = (float) MathUtil.lerp(this.itemEntity.x - tx, 0, delta2);
		float dy = (float) MathUtil.lerp(this.itemEntity.y - ty, yOffset, delta2);
		float dz = (float) MathUtil.lerp(this.itemEntity.z - tz, 0, delta2);
		
		EntityRenderDispatcher.INSTANCE.render(this.itemEntity, dx, dy, dz, this.itemEntity.yaw, delta);
	}
}
