package net.bhapi.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.menu.MainMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MainMenuScreen.class)
public abstract class MainMenuScreenMixin extends Screen {
	@Inject(method = "init", at = @At("HEAD"))
	private void bhapi_menuInit(CallbackInfo info) {
		this.minecraft.isApplet = false;
	}
}
