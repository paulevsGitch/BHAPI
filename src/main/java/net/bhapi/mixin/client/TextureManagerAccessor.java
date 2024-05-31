package net.bhapi.mixin.client;

import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {
	@Accessor("textureBinders")
	List<?> bhapi_getTextureBinders();
}
