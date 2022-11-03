package net.bhapi.mixin.client;

import net.minecraft.client.gui.screen.ScreenBase;
import net.minecraft.client.gui.screen.menu.MainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MainMenu.class)
public abstract class MainMenuMixin extends ScreenBase {
	@Inject(method = "init", at = @At("HEAD"), cancellable = true)
	private void bhapi_menuInit(CallbackInfo info) {
		this.minecraft.isApplet = false;
	}
}
