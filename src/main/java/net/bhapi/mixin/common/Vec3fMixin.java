package net.bhapi.mixin.common;

import net.bhapi.storage.vanilla.VanillaVectorCache;
import net.minecraft.util.maths.Vec3D;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Vec3D.class)
public class Vec3fMixin {
	@Unique
	private static final ThreadLocal<VanillaVectorCache> BHAPI_STORAGE = ThreadLocal.withInitial(VanillaVectorCache::new);
	
	@Inject(method = "getFromCacheAndSet", at = @At("HEAD"), cancellable = true)
	private static void bhapi_getFromCacheAndSet(double x, double y, double z, CallbackInfoReturnable<Vec3D> info) {
		info.setReturnValue(BHAPI_STORAGE.get().get(x, y, z));
	}
}
