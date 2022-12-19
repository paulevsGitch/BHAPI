package net.bhapi.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Properties;

@Mixin(TranslationStorage.class)
public class TranslationStorageMixin {
	@Shadow private Properties translations;
	
	@Environment(value= EnvType.CLIENT)
	@Inject(method = "method_995", at = @At("HEAD"), cancellable = true)
	private void bhapi_translateItem(String string, CallbackInfoReturnable<String> info) {
		string = string + ".name";
		string = this.translations.getProperty(string, string);
		info.setReturnValue(string);
	}
}
