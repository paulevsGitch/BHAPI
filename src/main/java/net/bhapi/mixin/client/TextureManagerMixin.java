package net.bhapi.mixin.client;

import net.bhapi.client.render.texture.TextureAtlas;
import net.bhapi.client.render.texture.Textures;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextureManager.class)
public class TextureManagerMixin {
	@Unique private static final String[] MASKS = new String[] {
		"/terrain.png",
		"/gui/items.png",
		"/particles.png",
	};
	
	@Inject(method = "getTextureId", at = @At("HEAD"), cancellable = true)
	private void bhapi_getTextureId(String name, CallbackInfoReturnable<Integer> info) {
		TextureAtlas atlas = Textures.getAtlas();
		if (atlas == null) return;
		for (String mask: MASKS) {
			if (mask.equals(name)) {
				info.setReturnValue(atlas.getGlTarget());
				return;
			}
		}
	}
}
