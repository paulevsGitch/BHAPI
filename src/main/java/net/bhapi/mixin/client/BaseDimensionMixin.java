package net.bhapi.mixin.client;

import net.bhapi.util.MathUtil;
import net.minecraft.level.dimension.BaseDimension;
import net.minecraft.util.maths.MathHelper;
import net.minecraft.util.maths.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseDimension.class)
public class BaseDimensionMixin {
	@Unique private Vec3f bhapi_skyColor = Vec3f.make(0, 0, 0);
	
	@Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
	private void bhapi_getSkyColor(float f, float g, CallbackInfoReturnable<Vec3f> info) {
		float light = MathHelper.cos(f * (float)Math.PI * 2.0f) * 2.0f + 0.5f;
		light = MathUtil.clamp(light, 0, 1);
		float cr = 0.7529412f * (light * 0.94f + 0.06f);
		float cg = 0.84705883f * (light * 0.94f + 0.06f);
		float cb = light * 0.91f + 0.09f;
		bhapi_skyColor.x = cr;
		bhapi_skyColor.y = cg;
		bhapi_skyColor.z = cb;
		info.setReturnValue(bhapi_skyColor);
	}
}
