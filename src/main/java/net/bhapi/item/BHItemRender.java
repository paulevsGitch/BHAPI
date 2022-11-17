package net.bhapi.item;

import net.bhapi.client.render.texture.TextureSample;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface BHItemRender {
	@Environment(EnvType.CLIENT)
	TextureSample getTexture(@Nullable ItemStack stack);
	
	static BHItemRender cast(Object obj) {
		return (BHItemRender) obj;
	}
}
