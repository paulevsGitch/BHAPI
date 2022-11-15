package net.bhapi.item;

import net.bhapi.util.ItemUtil;
import net.minecraft.item.BaseItem;

public abstract class BHItem extends BaseItem implements BHItemRender {
	public BHItem() {
		super(ItemUtil.MOD_ITEM_ID);
	}
}
