package net.bhapi.mixin.client;

import net.bhapi.client.render.level.ClientChunks;
import net.bhapi.storage.Vec3I;
import net.minecraft.client.render.AreaRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AreaRenderer.class)
public abstract class AreaRendererMixin {
	@Shadow public boolean canUpdate;
	@Shadow public Level level;
	@Shadow private boolean hasData;
	
	@Shadow protected abstract void offset();
	
	@Shadow public int startX;
	@Shadow public int startY;
	@Shadow public int startZ;
	
	@Inject(method = "distance", at = @At("HEAD"), cancellable = true)
	private void bhapi_distance(Entity entity, CallbackInfoReturnable<Float> info) {
		if (entity == null) info.setReturnValue(0F);
	}
	
	@Inject(method = "update", at = @At("HEAD"), cancellable = true)
	private void bhapi_update(CallbackInfo info) {
		info.cancel();
		this.canUpdate = false;
		this.hasData = false;
		ClientChunks.update(new Vec3I(this.startX >> 4, this.startY >> 4, this.startZ >> 4));
	}
}
