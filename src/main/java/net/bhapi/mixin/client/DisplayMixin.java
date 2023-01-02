package net.bhapi.mixin.client;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = Display.class, remap = false)
public class DisplayMixin {
	@ModifyVariable(method = "create(Lorg/lwjgl/opengl/PixelFormat;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private static PixelFormat bhapi_changeDepth(PixelFormat format) {
		return format.withDepthBits(24);
	}
}
