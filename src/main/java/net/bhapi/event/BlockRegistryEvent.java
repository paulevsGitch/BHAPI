package net.bhapi.event;

import net.bhapi.registry.Registry;
import net.bhapi.util.Identifier;
import net.minecraft.block.Block;

public class BlockRegistryEvent extends RegistryEvent<Identifier, Block> {
	public BlockRegistryEvent(Registry<Block> registry) {
		super(registry::register);
	}
	
	@Override
	public int getPriority() {
		return EventPriorities.BLOCK_REGISTRY;
	}
	
	@Override
	public void register(Identifier id, Block value) {
		super.register(id, value);
		value.setTranslationKey(id.getModID() + "." + id.getName());
	}
}
