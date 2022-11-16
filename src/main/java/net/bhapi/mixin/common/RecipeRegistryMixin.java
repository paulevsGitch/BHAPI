package net.bhapi.mixin.common;

import net.minecraft.recipe.RecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeRegistry.class)
public class RecipeRegistryMixin {
	@Shadow private List recipes;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_onRegistryInit(CallbackInfo info) {
		this.recipes.clear();
	}
}
