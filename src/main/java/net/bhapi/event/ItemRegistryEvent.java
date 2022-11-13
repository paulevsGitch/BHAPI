package net.bhapi.event;

import net.bhapi.registry.Registry;
import net.bhapi.util.Identifier;
import net.minecraft.item.BaseItem;

public class ItemRegistryEvent extends RegistryEvent<Identifier, BaseItem> {
	public ItemRegistryEvent(Registry<BaseItem> registry) {
		super(registry::register);
	}
	
	@Override
	public int getPriority() {
		return EventPriorities.ITEM_REGISTRY;
	}
}
