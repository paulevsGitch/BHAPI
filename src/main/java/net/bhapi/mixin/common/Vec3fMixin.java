package net.bhapi.mixin.common;

import net.bhapi.storage.MultiThreadStorage;
import net.bhapi.storage.vanilla.VanillaVectorCache;
import net.minecraft.util.maths.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Vec3f.class)
public class Vec3fMixin {
	@Unique
	private static final MultiThreadStorage<VanillaVectorCache> BHAPI_STORAGE = new MultiThreadStorage<>(VanillaVectorCache::new);
	
	@Inject(method = "getFromCacheAndSet", at = @At("HEAD"), cancellable = true)
	private static void bhapi_getFromCacheAndSet(double x, double y, double z, CallbackInfoReturnable<Vec3f> info) {
		info.setReturnValue(BHAPI_STORAGE.get().get(x, y, z));
	}
}
