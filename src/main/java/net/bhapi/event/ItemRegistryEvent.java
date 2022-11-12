package net.bhapi.event;

import net.bhapi.registry.Registry;
import net.minecraft.item.BaseItem;

public class ItemRegistryEvent extends RegistryEvent<BaseItem> {
	public ItemRegistryEvent(Registry<BaseItem> registry) {
		super(registry);
	}
	
	@Override
	public int getPriority() {
		return 1;
	}
}
