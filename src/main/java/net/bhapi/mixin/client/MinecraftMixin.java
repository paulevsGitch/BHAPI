package net.bhapi.mixin.client;

import net.bhapi.BHAPI;
import net.bhapi.client.ClientRegistries;
import net.bhapi.client.render.Textures;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "init()V", at = @At("TAIL"))
	private void bhapi_onMinecraftInit(CallbackInfo info) {
		BHAPI.log("Process client events");
		BHAPI.processEntryPoints("bhapi:client_events", ClientRegistries.EVENT_REGISTRY);
		Textures.init();
	}
}
