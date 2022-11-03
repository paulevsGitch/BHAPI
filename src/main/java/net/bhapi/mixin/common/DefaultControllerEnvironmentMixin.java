package net.bhapi.mixin.common;

import net.java.games.input.Controller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.java.games.input.DefaultControllerEnvironment"}, remap = false)
public class DefaultControllerEnvironmentMixin {
	// Disable controllers, not working properly on modern OS
	@Inject(method = "getControllers", at = @At("HEAD"), cancellable = true)
	private void bhapi_disableControllers(CallbackInfoReturnable<Controller[]> info) {
		info.setReturnValue(new Controller[0]);
	}
}
