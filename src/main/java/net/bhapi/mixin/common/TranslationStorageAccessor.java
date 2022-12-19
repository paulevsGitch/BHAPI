package net.bhapi.mixin.common;

import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Properties;

@Mixin(TranslationStorage.class)
public interface TranslationStorageAccessor {
	@Accessor("translations")
	Properties getProperties();
}
