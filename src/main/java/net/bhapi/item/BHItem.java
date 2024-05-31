package net.bhapi.item;

import net.bhapi.util.ItemUtil;
import net.minecraft.item.Item;

public abstract class BHItem extends Item implements BHItemRender {
	public BHItem() {
		super(ItemUtil.MOD_ITEM_ID);
	}
}
