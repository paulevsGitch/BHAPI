package net.bhapi.mixin.client;

import net.bhapi.BHAPI;
import net.bhapi.client.ClientRegistries;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.level.ClientChunks;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.interfaces.ClientPostInit;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.ImageUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow public String fpsDebugString;
	
	@Inject(method = "init()V", at = @At("TAIL"))
	private void bhapi_onMinecraftInit(CallbackInfo info) {
		BHAPI.log("Process client events (pre)");
		Textures.preInit();
		BHAPI.processEntryPoints("bhapi:client_events", ClientRegistries.EVENT_REGISTRY_PRE);
		Textures.init();
		ImageUtil.processAnimations();
	}
	
	@Inject(method = "init()V", at = @At("RETURN"))
	private void bhapi_afterMinecraftInit(CallbackInfo info) {
		BHAPI.log("Process client events (post)");
		BHAPI.processEntryPoints("bhapi:client_events", ClientRegistries.EVENT_REGISTRY_POST);
		
		CommonRegistries.BLOCK_REGISTRY
			.values()
			.stream()
			.filter(ClientPostInit.class::isInstance)
			.map(ClientPostInit::cast)
			.forEach(ClientPostInit::afterClientInit);
		
		CommonRegistries.ITEM_REGISTRY
			.values()
			.stream()
			.filter(ClientPostInit.class::isInstance)
			.map(ClientPostInit::cast)
			.forEach(ClientPostInit::afterClientInit);
	}
	
	@Inject(method = "run", at = @At(
		value = "FIELD",
		target = "Lnet/minecraft/client/Minecraft;fpsDebugString:Ljava/lang/String;",
		shift = Shift.AFTER)
	)
	private void bhapi_fixChunkUpdates(CallbackInfo info) {
		this.fpsDebugString = this.fpsDebugString.substring(0, this.fpsDebugString.length() - 15) +
			ClientChunks.getChunkUpdates() + " chunk updates";
	}
	
	@Inject(method = "Lnet/minecraft/client/Minecraft;stop()V", at = @At("HEAD"))
	private void bhapi_onExit(CallbackInfo info) {
		BHBlockRenderer.clearItemCache();
		ClientChunks.onExit();
	}
}
