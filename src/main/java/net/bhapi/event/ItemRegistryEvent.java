package net.bhapi.event;

import net.bhapi.registry.Registry;
import net.bhapi.util.Identifier;
import net.minecraft.item.Item;

public class ItemRegistryEvent extends RegistryEvent<Identifier, Item> {
	public ItemRegistryEvent(Registry<Item> registry) {
		super(registry::register);
	}
	
	@Override
	public int getPriority() {
		return EventPriorities.ITEM_REGISTRY;
	}
	
	@Override
	public void register(Identifier id, Item value) {
		super.register(id, value);
		value.setTranslationKey(id.getModID() + "." + id.getName());
	}
}
