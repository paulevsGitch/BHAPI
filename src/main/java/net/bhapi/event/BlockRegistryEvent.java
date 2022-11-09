package net.bhapi.event;

import net.bhapi.registry.Registry;
import net.minecraft.block.BaseBlock;

public class BlockRegistryEvent extends RegistryEvent<BaseBlock> {
	public BlockRegistryEvent(Registry<BaseBlock> registry) {
		super(registry);
	}
	
	@Override
	public int getPriority() {
		return 0;
	}
}
