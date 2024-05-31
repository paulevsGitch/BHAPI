package net.bhapi.item;

import net.minecraft.item.Item;

public interface ItemProvider {
	Item bhapi_getItem();
	void bhapi_setItem(Item item);
	
	static ItemProvider cast(Object obj) {
		return (ItemProvider) obj;
	}
}
