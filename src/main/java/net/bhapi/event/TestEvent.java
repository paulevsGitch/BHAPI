package net.bhapi.event;

import net.bhapi.block.BHBaseBlock;
import net.bhapi.blockstate.BlockState;
import net.bhapi.client.TestClientEvent;
import net.bhapi.client.render.block.CustomBlockRender;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.item.BHBlockItem;
import net.bhapi.item.BHItem;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.Identifier;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.BlockSounds;
import net.minecraft.block.material.Material;
import net.minecraft.level.BlockView;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

// TODO remove this
public class TestEvent {
	public static final Map<Identifier, BaseBlock> BLOCKS = new HashMap<>();
	
	@EventListener // Test Blocks
	public void registerBlocks(BlockRegistryEvent event) {
		registerBlock("testblock", new TestBlock(Material.WOOD, BaseBlock.WOOD_SOUNDS), event::register);
		registerBlock("testblock2", new TestBlock(Material.DIRT, BaseBlock.GRASS_SOUNDS), event::register);
		registerBlock("testblock3", new TestBlock(Material.GLASS, BaseBlock.GLASS_SOUNDS), event::register);
	}
	
	private void registerBlock(String name, BaseBlock block, BiConsumer<Identifier, BaseBlock> register) {
		Identifier id = Identifier.make(name);
		register.accept(id, block);
		BLOCKS.put(id, block);
	}
	
	@EventListener // Test Items
	public void registerItems(ItemRegistryEvent event) {
		event.register(Identifier.make("testitem"), new BHItem());
		BLOCKS.forEach((id, block) -> event.register(id, new BHBlockItem(block, false)));
	}
	
	private class TestBlock extends BHBaseBlock implements CustomBlockRender {
		private static int textureID = 0;
		private final int texID = textureID++;
		
		public TestBlock(Material material, BlockSounds sounds) {
			super(material);
			setHardness(0.1F);
			setBlastResistance(0.1F);
			setSounds(sounds);
		}
		
		@Override
		public int getTextureForSide(int i) {
			return (CommonRegistries.BLOCK_REGISTRY.getID(this).hashCode() + i) & 3;
		}
		
		@Override
		public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int index) {
			return TestClientEvent.samples[texID];
		}
	}
}
