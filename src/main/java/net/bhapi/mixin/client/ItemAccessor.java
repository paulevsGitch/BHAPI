package net.bhapi.mixin.client;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemAccessor {
	@Accessor("texturePosition")
	int bhapi_getTexturePosition();
	
	@Accessor("texturePosition")
	void bhapi_setTexturePosition(int texture);
}
