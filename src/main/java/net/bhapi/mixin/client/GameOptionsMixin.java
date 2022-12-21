package net.bhapi.mixin.client;

import net.bhapi.client.render.level.ClientChunks;
import net.bhapi.level.light.ClientLightLevel;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
	@Inject(method = "saveOptions", at = @At("HEAD"))
	private void bhapi_saveOptions(CallbackInfo info) {
		ClientChunks.init();
		ClientLightLevel.init();
	}
}
