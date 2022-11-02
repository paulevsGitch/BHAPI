package net.bhapi.registry;

import net.bhapi.block.BHAirBlock;
import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.mixin.common.BaseBlockAccessor;
import net.bhapi.util.Identifier;
import net.minecraft.block.BaseBlock;
import net.minecraft.item.BaseItem;

public class DefaultRegistries {
	public static final Registry<BaseBlock> BLOCK_REGISTRY = new Registry<>();
	public static final Registry<BaseItem> ITEM_REGISTRY = new Registry<>();
	public static final BHAirBlock AIR_BLOCK = new BHAirBlock();
	
	public static final SerialisationMap<BlockState> BLOCKSTATES_MAP = new SerialisationMap<>(
		"blockstates",
		BlockState::toNBTString,
		BlockState::fromNBTString
	);
	
	public static void initBlocks() {
		// Experimental ID extension
		BaseBlock[] blocks = new BaseBlock[512];
		System.arraycopy(BaseBlock.BY_ID, 0, blocks, 0, BaseBlock.BY_ID.length);
		//blocks[2] = BaseBlock.BY_ID[1];
		//blocks[1] = BaseBlock.BY_ID[2];
		BaseBlockAccessor.bhapi_setBlocks(blocks);
		
		System.out.println("New Length " + BaseBlock.BY_ID.length);
		
		BLOCK_REGISTRY.register(Identifier.make("air"), AIR_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("stone"), BaseBlock.STONE);
		BLOCK_REGISTRY.register(Identifier.make("grass"), BaseBlock.GRASS);
		BLOCK_REGISTRY.register(Identifier.make("dirt"), BaseBlock.DIRT);
		BLOCK_REGISTRY.register(Identifier.make("cobblestone"), BaseBlock.COBBLESTONE);
		BLOCK_REGISTRY.register(Identifier.make("wood"), BaseBlock.WOOD);
		BLOCK_REGISTRY.register(Identifier.make("sapling"), BaseBlock.SAPLING);
		BLOCK_REGISTRY.register(Identifier.make("bedrock"), BaseBlock.BEDROCK);
		BLOCK_REGISTRY.register(Identifier.make("flowing_water"), BaseBlock.FLOWING_WATER);
		BLOCK_REGISTRY.register(Identifier.make("still_water"), BaseBlock.STILL_WATER);
		BLOCK_REGISTRY.register(Identifier.make("flowing_lava"), BaseBlock.FLOWING_LAVA);
		BLOCK_REGISTRY.register(Identifier.make("still_lava"), BaseBlock.STILL_LAVA);
		BLOCK_REGISTRY.register(Identifier.make("sand"), BaseBlock.SAND);
		BLOCK_REGISTRY.register(Identifier.make("gravel"), BaseBlock.GRAVEL);
		BLOCK_REGISTRY.register(Identifier.make("gold_ore"), BaseBlock.GOLD_ORE);
		BLOCK_REGISTRY.register(Identifier.make("iron_ore"), BaseBlock.IRON_ORE);
		BLOCK_REGISTRY.register(Identifier.make("coal_ore"), BaseBlock.COAL_ORE);
		BLOCK_REGISTRY.register(Identifier.make("log"), BaseBlock.LOG);
		BLOCK_REGISTRY.register(Identifier.make("leaves"), BaseBlock.LEAVES);
		BLOCK_REGISTRY.register(Identifier.make("sponge"), BaseBlock.SPONGE);
		BLOCK_REGISTRY.register(Identifier.make("glass"), BaseBlock.GLASS);
		BLOCK_REGISTRY.register(Identifier.make("lapis_lazuli_ore"), BaseBlock.LAPIS_LAZULI_ORE);
		BLOCK_REGISTRY.register(Identifier.make("lapis_lazuli_block"), BaseBlock.LAPIS_LAZULI_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("dispenser"), BaseBlock.DISPENSER);
		BLOCK_REGISTRY.register(Identifier.make("sandstone"), BaseBlock.SANDSTONE);
		BLOCK_REGISTRY.register(Identifier.make("noteblock"), BaseBlock.NOTEBLOCK);
		BLOCK_REGISTRY.register(Identifier.make("bed"), BaseBlock.BED);
		BLOCK_REGISTRY.register(Identifier.make("golden_rail"), BaseBlock.GOLDEN_RAIL);
		BLOCK_REGISTRY.register(Identifier.make("detector_rail"), BaseBlock.DETECTOR_RAIL);
		BLOCK_REGISTRY.register(Identifier.make("sticky_piston"), BaseBlock.STICKY_PISTON);
		BLOCK_REGISTRY.register(Identifier.make("cobweb"), BaseBlock.COBWEB);
		BLOCK_REGISTRY.register(Identifier.make("tallgrass"), BaseBlock.TALLGRASS);
		BLOCK_REGISTRY.register(Identifier.make("deadbush"), BaseBlock.DEADBUSH);
		BLOCK_REGISTRY.register(Identifier.make("piston"), BaseBlock.PISTON);
		BLOCK_REGISTRY.register(Identifier.make("piston_head"), BaseBlock.PISTON_HEAD);
		BLOCK_REGISTRY.register(Identifier.make("wool"), BaseBlock.WOOL);
		BLOCK_REGISTRY.register(Identifier.make("moving_piston"), BaseBlock.MOVING_PISTON);
		BLOCK_REGISTRY.register(Identifier.make("dandelion"), BaseBlock.DANDELION);
		BLOCK_REGISTRY.register(Identifier.make("rose"), BaseBlock.ROSE);
		BLOCK_REGISTRY.register(Identifier.make("brown_mushroom"), BaseBlock.BROWN_MUSHROOM);
		BLOCK_REGISTRY.register(Identifier.make("red_mushroom"), BaseBlock.RED_MUSHROOM);
		BLOCK_REGISTRY.register(Identifier.make("gold_block"), BaseBlock.GOLD_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("iron_block"), BaseBlock.IRON_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("double_stone_slab"), BaseBlock.DOUBLE_STONE_SLAB);
		BLOCK_REGISTRY.register(Identifier.make("stone_slab"), BaseBlock.STONE_SLAB);
		BLOCK_REGISTRY.register(Identifier.make("bricks"), BaseBlock.BRICKS);
		BLOCK_REGISTRY.register(Identifier.make("tnt"), BaseBlock.TNT);
		BLOCK_REGISTRY.register(Identifier.make("bookshelf"), BaseBlock.BOOKSHELF);
		BLOCK_REGISTRY.register(Identifier.make("mossy_cobblestone"), BaseBlock.MOSSY_COBBLESTONE);
		BLOCK_REGISTRY.register(Identifier.make("obsidian"), BaseBlock.OBSIDIAN);
		BLOCK_REGISTRY.register(Identifier.make("torch"), BaseBlock.TORCH);
		BLOCK_REGISTRY.register(Identifier.make("fire"), BaseBlock.FIRE);
		BLOCK_REGISTRY.register(Identifier.make("mob_spawner"), BaseBlock.MOB_SPAWNER);
		BLOCK_REGISTRY.register(Identifier.make("wood_stairs"), BaseBlock.WOOD_STAIRS);
		BLOCK_REGISTRY.register(Identifier.make("chest"), BaseBlock.CHEST);
		BLOCK_REGISTRY.register(Identifier.make("redstone_dust"), BaseBlock.REDSTONE_DUST);
		BLOCK_REGISTRY.register(Identifier.make("diamond_ore"), BaseBlock.DIAMOND_ORE);
		BLOCK_REGISTRY.register(Identifier.make("diamond_block"), BaseBlock.DIAMOND_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("workbench"), BaseBlock.WORKBENCH);
		BLOCK_REGISTRY.register(Identifier.make("crops"), BaseBlock.CROPS);
		BLOCK_REGISTRY.register(Identifier.make("farmland"), BaseBlock.FARMLAND);
		BLOCK_REGISTRY.register(Identifier.make("furnace"), BaseBlock.FURNACE);
		BLOCK_REGISTRY.register(Identifier.make("furnace_lit"), BaseBlock.FURNACE_LIT);
		BLOCK_REGISTRY.register(Identifier.make("standing_sign"), BaseBlock.STANDING_SIGN);
		BLOCK_REGISTRY.register(Identifier.make("wood_door"), BaseBlock.WOOD_DOOR);
		BLOCK_REGISTRY.register(Identifier.make("ladder"), BaseBlock.LADDER);
		BLOCK_REGISTRY.register(Identifier.make("rail"), BaseBlock.RAIL);
		BLOCK_REGISTRY.register(Identifier.make("cobblestone_stairs"), BaseBlock.COBBLESTONE_STAIRS);
		BLOCK_REGISTRY.register(Identifier.make("wall_sign"), BaseBlock.WALL_SIGN);
		BLOCK_REGISTRY.register(Identifier.make("lever"), BaseBlock.LEVER);
		BLOCK_REGISTRY.register(Identifier.make("wooden_pressure_plate"), BaseBlock.WOODEN_PRESSURE_PLATE);
		BLOCK_REGISTRY.register(Identifier.make("iron_door"), BaseBlock.IRON_DOOR);
		BLOCK_REGISTRY.register(Identifier.make("stone_pressure_plate"), BaseBlock.STONE_PRESSURE_PLATE);
		BLOCK_REGISTRY.register(Identifier.make("redstone_ore"), BaseBlock.REDSTONE_ORE);
		BLOCK_REGISTRY.register(Identifier.make("redstone_ore_lit"), BaseBlock.REDSTONE_ORE_LIT);
		BLOCK_REGISTRY.register(Identifier.make("redstone_torch"), BaseBlock.REDSTONE_TORCH);
		BLOCK_REGISTRY.register(Identifier.make("redstone_torch_lit"), BaseBlock.REDSTONE_TORCH_LIT);
		BLOCK_REGISTRY.register(Identifier.make("button"), BaseBlock.BUTTON);
		BLOCK_REGISTRY.register(Identifier.make("snow"), BaseBlock.SNOW);
		BLOCK_REGISTRY.register(Identifier.make("ice"), BaseBlock.ICE);
		BLOCK_REGISTRY.register(Identifier.make("snow_block"), BaseBlock.SNOW_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("cactus"), BaseBlock.CACTUS);
		BLOCK_REGISTRY.register(Identifier.make("clay"), BaseBlock.CLAY);
		BLOCK_REGISTRY.register(Identifier.make("sugar_canes"), BaseBlock.SUGAR_CANES);
		BLOCK_REGISTRY.register(Identifier.make("jukebox"), BaseBlock.JUKEBOX);
		BLOCK_REGISTRY.register(Identifier.make("fence"), BaseBlock.FENCE);
		BLOCK_REGISTRY.register(Identifier.make("pumpkin"), BaseBlock.PUMPKIN);
		BLOCK_REGISTRY.register(Identifier.make("netherrack"), BaseBlock.NETHERRACK);
		BLOCK_REGISTRY.register(Identifier.make("soul_sand"), BaseBlock.SOUL_SAND);
		BLOCK_REGISTRY.register(Identifier.make("glowstone"), BaseBlock.GLOWSTONE);
		BLOCK_REGISTRY.register(Identifier.make("portal"), BaseBlock.PORTAL);
		BLOCK_REGISTRY.register(Identifier.make("jack_o_lantern"), BaseBlock.JACK_O_LANTERN);
		BLOCK_REGISTRY.register(Identifier.make("cake"), BaseBlock.CAKE);
		BLOCK_REGISTRY.register(Identifier.make("redstone_repeater"), BaseBlock.REDSTONE_REPEATER);
		BLOCK_REGISTRY.register(Identifier.make("redstone_repeater_lit"), BaseBlock.REDSTONE_REPEATER_LIT);
		BLOCK_REGISTRY.register(Identifier.make("locked_chest"), BaseBlock.LOCKED_CHEST);
		BLOCK_REGISTRY.register(Identifier.make("trapdoor"), BaseBlock.TRAPDOOR);
		
		BLOCK_REGISTRY.forEach(block -> BlockStateContainer.cast(block).getDefaultState());
	}
}
