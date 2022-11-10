package net.bhapi.registry;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.event.BHEvent;
import net.bhapi.event.BlockRegistryEvent;
import net.bhapi.event.ItemRegistryEvent;
import net.bhapi.item.BHBlockItem;
import net.bhapi.util.BlockUtil;
import net.bhapi.util.Identifier;
import net.minecraft.block.BaseBlock;
import net.minecraft.item.BaseItem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CommonRegistries {
	public static final Registry<BaseBlock> BLOCK_REGISTRY = new Registry<>();
	public static final Registry<BaseItem> ITEM_REGISTRY = new Registry<>();
	public static final Map<Class<? extends BHEvent>, Supplier<? extends BHEvent>> EVENT_REGISTRY = new HashMap<>();
	
	public static final SerialisationMap<BlockState> BLOCKSTATES_MAP = new SerialisationMap<>(
		"blockstates",
		BlockState::saveToNBT,
		BlockState::loadFromNBT
	);
	
	public static void init() {
		initBlocks();
		initItems();
		initEvents();
	}
	
	private static void initBlocks() {
		BLOCK_REGISTRY.register(Identifier.make("air"), BlockUtil.AIR_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("stone"), BaseBlock.STONE);
		BLOCK_REGISTRY.register(Identifier.make("grass"), BaseBlock.GRASS);
		BLOCK_REGISTRY.register(Identifier.make("dirt"), BaseBlock.DIRT);
		BLOCK_REGISTRY.register(Identifier.make("cobblestone"), BaseBlock.COBBLESTONE);
		BLOCK_REGISTRY.register(Identifier.make("wood"), BaseBlock.WOOD);
		BLOCK_REGISTRY.register(Identifier.make("sapling"), BaseBlock.SAPLING);
		BLOCK_REGISTRY.register(Identifier.make("bedrock"), BaseBlock.BEDROCK);
		BLOCK_REGISTRY.register(Identifier.make("flowing_water"), BaseBlock.FLOWING_WATER);
		BLOCK_REGISTRY.register(Identifier.make("static_water"), BaseBlock.STILL_WATER);
		BLOCK_REGISTRY.register(Identifier.make("flowing_lava"), BaseBlock.FLOWING_LAVA);
		BLOCK_REGISTRY.register(Identifier.make("static_lava"), BaseBlock.STILL_LAVA);
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
		
		// Make sure that all vanilla blocks are generated on startup
		BLOCK_REGISTRY.forEach(block -> {
			BlockStateContainer.cast(block).getDefaultState().getPossibleStates();
			ITEM_REGISTRY.register(BLOCK_REGISTRY.getID(block), new BHBlockItem(block));
		});
	}
	
	private static void initItems() {
		ITEM_REGISTRY.register(Identifier.make("iron_shovel"), BaseItem.ironShovel);
		ITEM_REGISTRY.register(Identifier.make("iron_pickaxe"), BaseItem.ironPickaxe);
		ITEM_REGISTRY.register(Identifier.make("iron_axe"), BaseItem.ironAxe);
		ITEM_REGISTRY.register(Identifier.make("flint_and_steel"), BaseItem.flintAndSteel);
		ITEM_REGISTRY.register(Identifier.make("apple"), BaseItem.apple);
		ITEM_REGISTRY.register(Identifier.make("bow"), BaseItem.bow);
		ITEM_REGISTRY.register(Identifier.make("arrow"), BaseItem.arrow);
		ITEM_REGISTRY.register(Identifier.make("coal"), BaseItem.coal);
		ITEM_REGISTRY.register(Identifier.make("diamond"), BaseItem.diamond);
		ITEM_REGISTRY.register(Identifier.make("iron_ingot"), BaseItem.ironIngot);
		ITEM_REGISTRY.register(Identifier.make("gold_ingot"), BaseItem.goldIngot);
		ITEM_REGISTRY.register(Identifier.make("iron_sword"), BaseItem.ironSword);
		ITEM_REGISTRY.register(Identifier.make("wood_sword"), BaseItem.woodSword);
		ITEM_REGISTRY.register(Identifier.make("wood_shovel"), BaseItem.woodShovel);
		ITEM_REGISTRY.register(Identifier.make("wood_pickaxe"), BaseItem.woodPickaxe);
		ITEM_REGISTRY.register(Identifier.make("wood_axe"), BaseItem.woodAxe);
		ITEM_REGISTRY.register(Identifier.make("stone_sword"), BaseItem.stoneSword);
		ITEM_REGISTRY.register(Identifier.make("stone_shovel"), BaseItem.stoneShovel);
		ITEM_REGISTRY.register(Identifier.make("stone_pickaxe"), BaseItem.stonePickaxe);
		ITEM_REGISTRY.register(Identifier.make("stone_axe"), BaseItem.stoneAxe);
		ITEM_REGISTRY.register(Identifier.make("diamond_sword"), BaseItem.diamondSword);
		ITEM_REGISTRY.register(Identifier.make("diamond_shovel"), BaseItem.diamondShovel);
		ITEM_REGISTRY.register(Identifier.make("diamond_pickaxe"), BaseItem.diamondPickaxe);
		ITEM_REGISTRY.register(Identifier.make("diamond_axe"), BaseItem.diamondAxe);
		ITEM_REGISTRY.register(Identifier.make("stick"), BaseItem.stick);
		ITEM_REGISTRY.register(Identifier.make("bowl"), BaseItem.bowl);
		ITEM_REGISTRY.register(Identifier.make("mushroom_stew"), BaseItem.mushroomStew);
		ITEM_REGISTRY.register(Identifier.make("gold_sword"), BaseItem.goldSword);
		ITEM_REGISTRY.register(Identifier.make("gold_shovel"), BaseItem.goldShovel);
		ITEM_REGISTRY.register(Identifier.make("gold_pickaxe"), BaseItem.goldPickaxe);
		ITEM_REGISTRY.register(Identifier.make("gold_axe"), BaseItem.goldAxe);
		ITEM_REGISTRY.register(Identifier.make("string"), BaseItem.string);
		ITEM_REGISTRY.register(Identifier.make("feather"), BaseItem.feather);
		ITEM_REGISTRY.register(Identifier.make("gunpowder"), BaseItem.gunpowder);
		ITEM_REGISTRY.register(Identifier.make("wood_hoe"), BaseItem.woodHoe);
		ITEM_REGISTRY.register(Identifier.make("stone_hoe"), BaseItem.stoneHoe);
		ITEM_REGISTRY.register(Identifier.make("iron_hoe"), BaseItem.ironHoe);
		ITEM_REGISTRY.register(Identifier.make("diamond_hoe"), BaseItem.diamondHoe);
		ITEM_REGISTRY.register(Identifier.make("gold_hoe"), BaseItem.goldHoe);
		ITEM_REGISTRY.register(Identifier.make("seeds"), BaseItem.seeds);
		ITEM_REGISTRY.register(Identifier.make("wheat"), BaseItem.wheat);
		ITEM_REGISTRY.register(Identifier.make("bread"), BaseItem.bread);
		ITEM_REGISTRY.register(Identifier.make("leather_helmet"), BaseItem.leatherHelmet);
		ITEM_REGISTRY.register(Identifier.make("leather_chestplate"), BaseItem.leatherChestplate);
		ITEM_REGISTRY.register(Identifier.make("leather_leggings"), BaseItem.leatherLeggings);
		ITEM_REGISTRY.register(Identifier.make("leather_boots"), BaseItem.leatherBoots);
		ITEM_REGISTRY.register(Identifier.make("chain_helmet"), BaseItem.chainHelmet);
		ITEM_REGISTRY.register(Identifier.make("chain_chestplate"), BaseItem.chainChestplate);
		ITEM_REGISTRY.register(Identifier.make("chain_leggings"), BaseItem.chainLeggings);
		ITEM_REGISTRY.register(Identifier.make("chain_boots"), BaseItem.chainBoots);
		ITEM_REGISTRY.register(Identifier.make("iron_helmet"), BaseItem.ironHelmet);
		ITEM_REGISTRY.register(Identifier.make("iron_chestplate"), BaseItem.ironChestplate);
		ITEM_REGISTRY.register(Identifier.make("iron_leggings"), BaseItem.ironLeggings);
		ITEM_REGISTRY.register(Identifier.make("iron_boots"), BaseItem.ironBoots);
		ITEM_REGISTRY.register(Identifier.make("diamond_helmet"), BaseItem.diamondHelmet);
		ITEM_REGISTRY.register(Identifier.make("diamond_chestplate"), BaseItem.diamondChestplate);
		ITEM_REGISTRY.register(Identifier.make("diamond_leggings"), BaseItem.diamondLeggings);
		ITEM_REGISTRY.register(Identifier.make("diamond_boots"), BaseItem.diamondBoots);
		ITEM_REGISTRY.register(Identifier.make("gold_helmet"), BaseItem.goldHelmet);
		ITEM_REGISTRY.register(Identifier.make("gold_chestplate"), BaseItem.goldChestplate);
		ITEM_REGISTRY.register(Identifier.make("gold_leggings"), BaseItem.goldLeggings);
		ITEM_REGISTRY.register(Identifier.make("gold_boots"), BaseItem.goldBoots);
		ITEM_REGISTRY.register(Identifier.make("flint"), BaseItem.flint);
		ITEM_REGISTRY.register(Identifier.make("raw_porkchop"), BaseItem.rawPorkchop);
		ITEM_REGISTRY.register(Identifier.make("cooked_porkchop"), BaseItem.cookedPorkchop);
		ITEM_REGISTRY.register(Identifier.make("painting"), BaseItem.painting);
		ITEM_REGISTRY.register(Identifier.make("golden_apple"), BaseItem.goldenApple);
		ITEM_REGISTRY.register(Identifier.make("sign"), BaseItem.sign);
		ITEM_REGISTRY.register(Identifier.make("wood_door"), BaseItem.woodDoor);
		ITEM_REGISTRY.register(Identifier.make("bucket"), BaseItem.bucket);
		ITEM_REGISTRY.register(Identifier.make("water_bucket"), BaseItem.waterBucket);
		ITEM_REGISTRY.register(Identifier.make("lava_bucket"), BaseItem.lavaBucket);
		ITEM_REGISTRY.register(Identifier.make("minecart"), BaseItem.minecart);
		ITEM_REGISTRY.register(Identifier.make("saddle"), BaseItem.saddle);
		ITEM_REGISTRY.register(Identifier.make("iron_door"), BaseItem.ironDoor);
		ITEM_REGISTRY.register(Identifier.make("redstone_dust"), BaseItem.redstoneDust);
		ITEM_REGISTRY.register(Identifier.make("snowball"), BaseItem.snowball);
		ITEM_REGISTRY.register(Identifier.make("boat"), BaseItem.boat);
		ITEM_REGISTRY.register(Identifier.make("leather"), BaseItem.leather);
		ITEM_REGISTRY.register(Identifier.make("milk"), BaseItem.milk);
		ITEM_REGISTRY.register(Identifier.make("brick"), BaseItem.brick);
		ITEM_REGISTRY.register(Identifier.make("clay"), BaseItem.clay);
		ITEM_REGISTRY.register(Identifier.make("sugar_canes"), BaseItem.sugarCanes);
		ITEM_REGISTRY.register(Identifier.make("paper"), BaseItem.paper);
		ITEM_REGISTRY.register(Identifier.make("book"), BaseItem.book);
		ITEM_REGISTRY.register(Identifier.make("slimeball"), BaseItem.slimeball);
		ITEM_REGISTRY.register(Identifier.make("minecart_chest"), BaseItem.minecartChest);
		ITEM_REGISTRY.register(Identifier.make("minecart_furnace"), BaseItem.minecartFurnace);
		ITEM_REGISTRY.register(Identifier.make("egg"), BaseItem.egg);
		ITEM_REGISTRY.register(Identifier.make("compass"), BaseItem.compass);
		ITEM_REGISTRY.register(Identifier.make("fishing_rod"), BaseItem.fishingRod);
		ITEM_REGISTRY.register(Identifier.make("clock"), BaseItem.clock);
		ITEM_REGISTRY.register(Identifier.make("glowstone_dust"), BaseItem.glowstoneDust);
		ITEM_REGISTRY.register(Identifier.make("raw_fish"), BaseItem.rawFish);
		ITEM_REGISTRY.register(Identifier.make("cooked_fish"), BaseItem.cookedFish);
		ITEM_REGISTRY.register(Identifier.make("dye_powder"), BaseItem.dyePowder);
		ITEM_REGISTRY.register(Identifier.make("bone"), BaseItem.bone);
		ITEM_REGISTRY.register(Identifier.make("sugar"), BaseItem.sugar);
		ITEM_REGISTRY.register(Identifier.make("cake"), BaseItem.cake);
		ITEM_REGISTRY.register(Identifier.make("bed"), BaseItem.bed);
		ITEM_REGISTRY.register(Identifier.make("redstone_repeater"), BaseItem.redstoneRepeater);
		ITEM_REGISTRY.register(Identifier.make("cookie"), BaseItem.cookie);
		ITEM_REGISTRY.register(Identifier.make("map"), BaseItem.map);
		ITEM_REGISTRY.register(Identifier.make("shears"), BaseItem.shears);
		ITEM_REGISTRY.register(Identifier.make("record_13"), BaseItem.record13);
		ITEM_REGISTRY.register(Identifier.make("record_cat"), BaseItem.recordCat);
	}
	
	private static void initEvents() {
		EVENT_REGISTRY.put(BlockRegistryEvent.class, () -> new BlockRegistryEvent(BLOCK_REGISTRY));
		EVENT_REGISTRY.put(ItemRegistryEvent.class, () -> new ItemRegistryEvent(ITEM_REGISTRY));
	}
}
