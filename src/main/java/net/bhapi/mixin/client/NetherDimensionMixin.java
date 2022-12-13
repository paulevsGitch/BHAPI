package net.bhapi.mixin.client;

import net.minecraft.level.dimension.NetherDimension;
import net.minecraft.util.maths.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherDimension.class)
public class NetherDimensionMixin {
	@Unique private Vec3f bhapi_netherSkyColor = Vec3f.make(0.2f, 0.03f, 0.03f);
	
	@Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
	private void bhapi_getSkyColor(float f, float g, CallbackInfoReturnable<Vec3f> info) {
		info.setReturnValue(bhapi_netherSkyColor);
	}
}
