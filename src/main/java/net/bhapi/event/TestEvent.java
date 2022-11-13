package net.bhapi.event;

import net.bhapi.block.BHBaseBlock;
import net.bhapi.item.BHBlockItem;
import net.bhapi.item.BHItem;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.Identifier;
import net.minecraft.block.material.Material;
import net.minecraft.item.BaseItem;

// TODO remove this
public class TestEvent {
	@EventListener // Test Blocks
	public void registerBlocks(BlockRegistryEvent event) {
		event.register(Identifier.make("testblock"), new TestBlock(Material.STONE));
		event.register(Identifier.make("testblock2"), new TestBlock(Material.DIRT));
	}
	
	@EventListener // Test Items
	public void registerItems(ItemRegistryEvent event) {
		event.register(Identifier.make("testitem"), new BHItem());
		event.register(
			Identifier.make("testblock"),
			new BHBlockItem(CommonRegistries.BLOCK_REGISTRY.get(Identifier.make("testblock")), false)
		);
		event.register(
			Identifier.make("testblock2"),
			new BHBlockItem(CommonRegistries.BLOCK_REGISTRY.get(Identifier.make("testblock2")), false)
		);
	}
	
	private class TestBlock extends BHBaseBlock {
		public TestBlock(Material material) {
			super(material);
			setHardness(0.1F);
			setBlastResistance(0.1F);
		}
		
		@Override
		public int getTextureForSide(int i) {
			return (CommonRegistries.BLOCK_REGISTRY.getID(this).hashCode() + i) & 3;
		}
	}
}
