package net.bhapi.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.BaseItem;

@Environment(EnvType.CLIENT)
public class ItemRenderInfo {
	private static BaseItem renderingItem;
	public static void setItem(BaseItem item) {
		renderingItem = item;
	}
	public static BaseItem getRenderingItem() {
		return renderingItem;
	}
}
