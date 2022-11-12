package net.bhapi.event;

import net.bhapi.registry.Registry;
import net.bhapi.util.Identifier;
import net.minecraft.block.BaseBlock;

public class BlockRegistryEvent extends RegistryEvent<BaseBlock> {
	public BlockRegistryEvent(Registry<BaseBlock> registry) {
		super(registry);
	}
	
	@Override
	public int getPriority() {
		return 0;
	}
	
	@Override
	public void register(Identifier id, BaseBlock value) {
		super.register(id, value);
		value.setTranslationKey(id.getModID() + "." + id.getName());
	}
}
