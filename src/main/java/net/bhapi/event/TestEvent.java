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
import net.bhapi.interfaces.ClientPostInit;
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
		registerBlock("testblock2", new TestBlock4(Material.DIRT, BaseBlock.GRASS_SOUNDS), event::register);
		registerBlock("testblock3", new TestBlock3(Material.GLASS, BaseBlock.GLASS_SOUNDS), event::register);
		registerBlock("farlands", new FarBlock(Material.WOOD, BaseBlock.STONE_SOUNDS), event::register);
		registerBlock("testblock4", new TestBlock(Material.WOOD, BaseBlock.WOOD_SOUNDS), event::register);
		registerBlock("testblock5", new TestBlock5(Material.WOOD, BaseBlock.METAL_SOUNDS), event::register);
		registerBlock("enchanted_iron", new EnchIron(), event::register);
	}
	
	private void registerBlock(String name, BaseBlock block, BiConsumer<Identifier, BaseBlock> register) {
		Identifier id = Identifier.make("bhapi", name);
		register.accept(id, block);
		BLOCKS.put(id, block);
	}
	
	@EventListener // Test Items
	public void registerItems(ItemRegistryEvent event) {
		BHAPI.log("Adding items");
		event.register(Identifier.make("bhapi", "testitem"), new BHSimpleItem(Identifier.make("block/stone")));
		BLOCKS.forEach((id, block) -> event.register(id, new BHBlockItem(block, false)));
	}
	
	@EventListener // Test Recipes
	public void registerRecipes(RecipeRegistryEvent event) {
		BHAPI.log("Adding shaped recipe");
		RecipeBuilder.start(ItemUtil.makeStack(Identifier.make("bhapi", "testblock")))
			.setShape("##", "##")
			.addIngredient('#', ItemUtil.makeStack(Identifier.make("dirt")))
			.build(event.getRegistry());
		
		BHAPI.log("Adding shapeless recipe");
		RecipeBuilder.start(ItemUtil.makeStack(Identifier.make("bhapi", "testblock3")))
			.addIngredient(ItemUtil.makeStack(Identifier.make("bhapi", "testblock")))
			.addIngredient(ItemUtil.makeStack(Identifier.make("bhapi", "testblock2")))
			.build(event.getRegistry());
	}
	
	private class FarBlock extends TestBlock {
		public FarBlock(Material material, BlockSounds sounds) {
			super(material, sounds);
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
			return TestClientEvent.samplesFar[MathUtil.clamp(textureIndex, 0, 2)];
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
		public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
			state = BlockStateProvider.cast(view).getBlockState(x, y - 1, z);
			if (state.isAir() || state.is(this)) return super.getTextureForIndex(view, x, y, z, state, textureIndex, overlayIndex);
			return state.getTextureForIndex(view, x, y - 1, z, 1, overlayIndex);
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
	
	private class TestBlock5 extends TestBlock3 {
		public TestBlock5(Material material, BlockSounds sounds) {
			super(material, sounds);
			setBoundingBox(0.3125F, 0, 0.3125F, 0.6875F, 0.4375F, 0.6875F);
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public CustomModel getModel(BlockView view, int x, int y, int z, BlockState state) {
			return TestClientEvent.testModel4;
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
			return TestClientEvent.samples[3];
		}
		
		@Override
		public int getEmittance(BlockState state) {
			return 15;
		}
	}
	
	private class TestBlock4 extends TestBlock3 implements ClientPostInit {
		@Environment(value=EnvType.CLIENT)
		private TextureSample fire;
		
		public TestBlock4(Material material, BlockSounds sounds) {
			super(material, sounds);
		}
		
		@Override
		@Environment(value=EnvType.CLIENT)
		public int getOverlayCount(BlockView view, int x, int y, int z, BlockState state) {
			BlockState below = BlockStateProvider.cast(view).getBlockState(x, y - 1, z);
			return below.is(BaseBlock.GRASS) || below.getMaterial() == Material.LAVA || below.getMaterial() == Material.WATER ? 2 : 1;
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
			TextureSample sample = null;
			switch (overlayIndex) {
				case 0 -> sample = super.getTextureForIndex(view, x, y, z, state, textureIndex, overlayIndex);
				case 1 -> {
					state = BlockStateProvider.cast(view).getBlockState(x, y - 1, z);
					if (state.is(BaseBlock.GRASS)) {
						sample = Textures.getVanillaBlockSample(BaseBlock.SUGAR_CANES.texture);
					}
					else if (state.getMaterial() == Material.LAVA) {
						sample = fire;
					}
					else {
						sample = Textures.getVanillaBlockSample(BaseBlock.STILL_WATER.texture);
					}
				}
			}
			return sample;
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public void afterClientInit() {
			if (fire != null) return;
			fire = Textures.getVanillaBlockSample(BaseBlock.FIRE.texture).clone();
			fire.setLight(1F);
		}
	}
	
	private class TestBlock3 extends TestBlock {
		public TestBlock3(Material material, BlockSounds sounds) {
			super(material, sounds);
		}
		
		@Override
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
		public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
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
		
		@Override
		public int getLightOpacity(BlockState state) {
			return 0;
		}
		
		@Override
		public boolean isFullCube() {
			return false;
		}
	}
	
	private class EnchIron extends BHBaseBlock implements BHBlockRender, ClientPostInit {
		private TextureSample[] samples;
		
		public EnchIron() {
			super(Material.METAL);
			setSounds(METAL_SOUNDS);
			setHardness(0.7F);
		}
		
		@Override
		public void afterClientInit() {
			if (samples != null) return;
			samples = new TextureSample[] {
				Textures.getVanillaBlockSample(BaseBlock.IRON_BLOCK.texture),
				Textures.getAtlas().getSample(Identifier.make("bhapi", "block/glint"))
			};
			samples[1].setLight(1);
		}
		
		@Override
		@Environment(value=EnvType.CLIENT)
		public int getOverlayCount(BlockView view, int x, int y, int z, BlockState state) {
			return 2;
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
			return samples[overlayIndex];
		}
	}
}
