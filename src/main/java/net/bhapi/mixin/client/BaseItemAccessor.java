package net.bhapi.mixin.client;

import net.minecraft.item.BaseItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseItem.class)
public interface BaseItemAccessor {
	@Accessor("texturePosition")
	int bhapi_getTexturePosition();
	
	@Accessor("texturePosition")
	void bhapi_setTexturePosition(int texture);
}
