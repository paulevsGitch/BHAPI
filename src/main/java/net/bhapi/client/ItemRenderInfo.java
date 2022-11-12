package net.bhapi.client;

import net.minecraft.item.BaseItem;

public class ItemRenderInfo {
	private static BaseItem renderingItem;
	
	public static void setItem(BaseItem item) {
		renderingItem = item;
	}
	
	public static BaseItem getRenderingItem() {
		return renderingItem;
	}
}
