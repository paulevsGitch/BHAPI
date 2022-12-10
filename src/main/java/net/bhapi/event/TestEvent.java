package net.bhapi.event;

import net.bhapi.BHAPI;
import net.bhapi.block.BHBaseBlock;
import net.bhapi.blockstate.BlockState;
import net.bhapi.client.TestClientEvent;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.block.BlockRenderTypes;
import net.bhapi.client.render.model.CustomModel;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.item.BHBlockItem;
import net.bhapi.item.BHSimpleItem;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.recipe.RecipeBuilder;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.Identifier;
import net.bhapi.util.ItemUtil;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
		registerBlock("testblock", new TestBlock2(Material.WOOD, BaseBlock.WOOD_SOUNDS), event::register);
		registerBlock("testblock2", new TestBlock3(Material.DIRT, BaseBlock.GRASS_SOUNDS), event::register);
		registerBlock("testblock3", new TestBlock3(Material.GLASS, BaseBlock.GLASS_SOUNDS), event::register);
		registerBlock("farlands", new FarBlock(Material.WOOD, BaseBlock.STONE_SOUNDS), event::register);
		registerBlock("testblock4", new TestBlock(Material.WOOD, BaseBlock.WOOD_SOUNDS), event::register);
		
		System.out.println(CommonRegistries.BLOCK_REGISTRY.get(Identifier.make("farlands")).isFullOpaque());
	}
	
	private void registerBlock(String name, BaseBlock block, BiConsumer<Identifier, BaseBlock> register) {
		Identifier id = Identifier.make(name);
		register.accept(id, block);
		BLOCKS.put(id, block);
	}
	
	@EventListener // Test Items
	public void registerItems(ItemRegistryEvent event) {
		BHAPI.log("Adding items");
		event.register(Identifier.make("testitem"), new BHSimpleItem(Identifier.make("block/stone")));
		BLOCKS.forEach((id, block) -> event.register(id, new BHBlockItem(block, false)));
	}
	
	@EventListener // Test Recipes
	public void registerRecipes(RecipeRegistryEvent event) {
		BHAPI.log("Adding shaped recipe");
		RecipeBuilder.start(ItemUtil.makeStack(Identifier.make("testblock")))
			.setShape("##", "##")
			.addIngredient('#', ItemUtil.makeStack(Identifier.make("dirt")))
			.build(event.getRegistry());
		
		BHAPI.log("Adding shapeless recipe");
		RecipeBuilder.start(ItemUtil.makeStack(Identifier.make("testblock3")))
			.addIngredient(ItemUtil.makeStack(Identifier.make("testblock")))
			.addIngredient(ItemUtil.makeStack(Identifier.make("testblock2")))
			.build(event.getRegistry());
	}
	
	private class FarBlock extends TestBlock {
		public FarBlock(Material material, BlockSounds sounds) {
			super(material, sounds);
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int index) {
			return TestClientEvent.samplesFar[MathUtil.clamp(index, 0, 2)];
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public CustomModel getModel(BlockView view, int x, int y, int z, BlockState state) {
			return TestClientEvent.testModel2;
		}
	}
	
	private class TestBlock2 extends TestBlock {
		public TestBlock2(Material material, BlockSounds sounds) {
			super(material, sounds);
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int index) {
			state = BlockStateProvider.cast(view).getBlockState(x, y - 1, z);
			if (state.isAir() || state.is(this)) return super.getTextureForIndex(view, x, y, z, state, index);
			return state.getTextureForIndex(view, x, y - 1, z, index);
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public CustomModel getModel(BlockView view, int x, int y, int z, BlockState state) {
			return TestClientEvent.testModel;
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public boolean isSideRendered(BlockView blockView, int x, int y, int z, BlockDirection facing, BlockState state, BlockState target) {
			return true;
		}
	}
	
	private class TestBlock3 extends TestBlock {
		public TestBlock3(Material material, BlockSounds sounds) {
			super(material, sounds);
		}
		
		@Environment(value=EnvType.CLIENT)
		public boolean isSideRendered(BlockView arg, int i, int j, int k, int l) {
			return true;
		}
		
		@Override
		public boolean isFullOpaque() {
			return false;
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public CustomModel getModel(BlockView view, int x, int y, int z, BlockState state) {
			return TestClientEvent.testModel;
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public boolean isSideRendered(BlockView blockView, int x, int y, int z, BlockDirection facing, BlockState state, BlockState target) {
			return true;
		}
	}
	
	private class TestBlock extends BHBaseBlock implements BHBlockRender {
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
		@Environment(EnvType.CLIENT)
		public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int index) {
			return texID < 3 ? TestClientEvent.samples[texID] : Textures.getVanillaBlockSample(4);
		}
		
		@Override
		public boolean hasRandomTicks(BlockState state) {
			return true;
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public byte getRenderType(BlockView view, int x, int y, int z, BlockState state) {
			return BlockRenderTypes.CUSTOM;
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public CustomModel getModel(BlockView view, int x, int y, int z, BlockState state) {
			return TestClientEvent.testModel3;
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public boolean isSideRendered(BlockView blockView, int x, int y, int z, BlockDirection facing, BlockState state, BlockState target) {
			return false;
		}
	}
}
