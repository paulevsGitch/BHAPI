package net.bhapi.event;

import net.bhapi.block.BHBaseBlock;
import net.bhapi.item.BHBlockItem;
import net.bhapi.item.BHItem;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.Identifier;
import net.minecraft.block.material.Material;

// TODO remove this
public class TestEvent {
	@EventListener // Test Blocks
	public void registerBlocks(BlockRegistryEvent event) {
		event.register(Identifier.make("testblock"), new BHBaseBlock(Material.STONE));
		event.register(Identifier.make("testblock2"), new BHBaseBlock(Material.DIRT));
	}
	
	@EventListener // Test Items
	public void registerItems(ItemRegistryEvent event) {
		event.register(Identifier.make("testitem"), new BHItem());
		event.register(
			Identifier.make("testitem2"),
			new BHBlockItem(CommonRegistries.BLOCK_REGISTRY.get(Identifier.make("testblock")), false)
		);
	}
}
