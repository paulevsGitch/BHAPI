package net.bhapi.item;

import net.minecraft.item.BaseItem;

public interface ItemProvider {
	BaseItem getItem();
	void setItem(BaseItem item);
	
	static ItemProvider cast(Object obj) {
		return (ItemProvider) obj;
	}
}
