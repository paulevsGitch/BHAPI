package net.bhapi.event;

import net.bhapi.block.BHBaseBlock;
import net.bhapi.util.Identifier;
import net.minecraft.block.material.Material;

// TODO remove this
public class TestEvent {
	@EventListener // Test Blocks
	public void registerBlocks(BlockRegistryEvent event) {
		event.register(Identifier.make("testblock"), new BHBaseBlock(Material.STONE));
		event.register(Identifier.make("testblock2"), new BHBaseBlock(Material.DIRT));
	}
	
	@EventListener
	public void testUnexistingClass(EventRegistrationEvent event) {}
}
