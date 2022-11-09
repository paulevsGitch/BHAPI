package net.bhapi.event;

import net.bhapi.BHAPI;
import net.bhapi.block.BHBaseBlock;
import net.bhapi.blockstate.BlockState;
import net.bhapi.util.Identifier;
import net.minecraft.block.material.Material;

// TODO remove this
public class TestEvent {
	@EventListener // Test Blocks
	public void registerBlocks(BlockRegistryEvent event) {
		Identifier id = Identifier.make("testblock");
		BHBaseBlock block = new BHBaseBlock(Material.STONE);
		event.getRegistry().register(id, block);
		BlockState.getDefaultState(block);
		BHAPI.log("Registered " + id);
		
		id = Identifier.make("testblock2");
		block = new BHBaseBlock(Material.STONE);
		event.getRegistry().register(id, block);
		BlockState.getDefaultState(block);
		BHAPI.log("Registered " + id);
	}
	
	@EventListener(priority = 1)
	public void registerBlocks1(BlockRegistryEvent event) {
		System.out.println("1 " + event.getRegistry());
	}
	
	@EventListener(priority = 30)
	public void registerBlocks0(BlockRegistryEvent event) {
		System.out.println("30");
	}
	
	@EventListener(priority = 2)
	public void registerBlocks2(BlockRegistryEvent event) {
		System.out.println("2");
	}
}
