package net.bhapi.mixin.client;

import net.minecraft.client.render.AreaRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.BaseEntity;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AreaRenderer.class)
public abstract class AreaRendererMixin {
	@Shadow public boolean canUpdate;
	@Shadow public static int globalID;
	@Shadow public int startX;
	@Shadow public int startY;
	@Shadow public int startZ;
	@Shadow public int sideX;
	@Shadow public int sideY;
	@Shadow public int sideZ;
	@Shadow public boolean[] layerIsEmpty;
	@Shadow public List skipBlockEntities;
	@Shadow public Level level;
	@Shadow private int glListID;
	@Shadow private static Tessellator tesselator;
	@Shadow private List blockEntities;
	@Shadow public boolean hasSkyLight;
	@Shadow private boolean hasData;
	
	@Shadow protected abstract void offset();
	
	@Inject(method = "distance", at = @At("HEAD"), cancellable = true)
	private void bhapi_distance(BaseEntity entity, CallbackInfoReturnable<Float> info) {
		if (entity == null) info.setReturnValue(0F);
	}
	
	@SuppressWarnings("all")
	@Inject(method = "update", at = @At("HEAD"), cancellable = true)
	private void bhapi_update(CallbackInfo info) {
		info.cancel();
		this.canUpdate = false;
		this.hasData = false;
	}
}
