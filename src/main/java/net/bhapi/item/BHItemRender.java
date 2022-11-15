package net.bhapi.item;

import net.bhapi.client.render.texture.TextureSample;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

public interface BHItemRender {
	@Environment(EnvType.CLIENT)
	TextureSample getTextureForIndex(ItemStack stack);
	
	static BHItemRender cast(Object obj) {
		return (BHItemRender) obj;
	}
}
