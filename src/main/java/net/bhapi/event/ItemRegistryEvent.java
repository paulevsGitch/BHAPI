package net.bhapi.event;

import net.bhapi.BHAPI;
import net.bhapi.registry.Registry;
import net.minecraft.item.BaseItem;

public class ItemRegistryEvent extends RegistryEvent<BaseItem> {
	public ItemRegistryEvent(Registry<BaseItem> registry) {
		super(registry);
		BHAPI.log("Register Items");
	}
	
	@Override
	public int getPriority() {
		return 1;
	}
}
