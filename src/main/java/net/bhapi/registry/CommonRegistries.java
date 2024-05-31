package net.bhapi.registry;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.command.BHCommand;
import net.bhapi.event.AfterBlockAndItemsEvent;
import net.bhapi.event.BHEvent;
import net.bhapi.event.BlockRegistryEvent;
import net.bhapi.event.CommandRegistryEvent;
import net.bhapi.event.EventRegistrationEvent;
import net.bhapi.event.ItemRegistryEvent;
import net.bhapi.event.RecipeRegistryEvent;
import net.bhapi.event.StartupEvent;
import net.bhapi.item.BHBlockItem;
import net.bhapi.mixin.common.recipe.RecipeRegistryAccessor;
import net.bhapi.util.BlockUtil;
import net.bhapi.util.DyeUtil;
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.entity.living.player.ServerPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ArmorRecipes;
import net.minecraft.recipe.DyeRecipes;
import net.minecraft.recipe.MaterialBlockRecipes;
import net.minecraft.recipe.RecipeRegistry;
import net.minecraft.recipe.StewAndCookieRecipes;
import net.minecraft.recipe.ToolRecipes;
import net.minecraft.recipe.UtilitiesAndSandstoneRecipes;
import net.minecraft.recipe.WeaponRecipes;
import net.minecraft.server.ServerPlayerConnectionManager;
import net.minecraft.server.command.CommandSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CommonRegistries {
	public static final Registry<Block> BLOCK_REGISTRY = new Registry<>();
	public static final Registry<Item> ITEM_REGISTRY = new Registry<>();
	public static final Map<Class<? extends BHEvent>, Supplier<? extends BHEvent>> EVENT_REGISTRY = new HashMap<>();
	public static final Map<String, BHCommand> COMMAND_REGISTRY = new HashMap<>();
	
	public static final SerialisationMap<BlockState> BLOCKSTATES_MAP = new SerialisationMap<>(
		"blockstates",
		BlockState::saveToNBT,
		BlockState::loadFromNBT
	);
	
	public static void init() {
		initBlocks();
		initItems();
		initEvents();
		if (BHAPI.isServer()) initCommands();
	}
	
	private static void initBlocks() {
		BLOCK_REGISTRY.register(Identifier.make("air"), BlockUtil.AIR_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("stone"), Block.STONE);
		BLOCK_REGISTRY.register(Identifier.make("grass_block"), Block.GRASS);
		BLOCK_REGISTRY.register(Identifier.make("dirt"), Block.DIRT);
		BLOCK_REGISTRY.register(Identifier.make("cobblestone"), Block.COBBLESTONE);
		BLOCK_REGISTRY.register(Identifier.make("planks"), Block.PLANKS);
		BLOCK_REGISTRY.register(Identifier.make("sapling"), Block.SAPLING);
		BLOCK_REGISTRY.register(Identifier.make("bedrock"), Block.BEDROCK);
		BLOCK_REGISTRY.register(Identifier.make("flowing_water"), Block.FLOWING_WATER);
		BLOCK_REGISTRY.register(Identifier.make("static_water"), Block.STILL_WATER);
		BLOCK_REGISTRY.register(Identifier.make("flowing_lava"), Block.FLOWING_LAVA);
		BLOCK_REGISTRY.register(Identifier.make("static_lava"), Block.STILL_LAVA);
		BLOCK_REGISTRY.register(Identifier.make("sand"), Block.SAND);
		BLOCK_REGISTRY.register(Identifier.make("gravel"), Block.GRAVEL);
		BLOCK_REGISTRY.register(Identifier.make("gold_ore"), Block.GOLD_ORE);
		BLOCK_REGISTRY.register(Identifier.make("iron_ore"), Block.IRON_ORE);
		BLOCK_REGISTRY.register(Identifier.make("coal_ore"), Block.COAL_ORE);
		BLOCK_REGISTRY.register(Identifier.make("log"), Block.LOG);
		BLOCK_REGISTRY.register(Identifier.make("leaves"), Block.LEAVES);
		BLOCK_REGISTRY.register(Identifier.make("sponge"), Block.SPONGE);
		BLOCK_REGISTRY.register(Identifier.make("glass"), Block.GLASS);
		BLOCK_REGISTRY.register(Identifier.make("lapis_ore"), Block.LAPIS_LAZULI_ORE);
		BLOCK_REGISTRY.register(Identifier.make("lapis_block"), Block.LAPIS_LAZULI_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("dispenser"), Block.DISPENSER);
		BLOCK_REGISTRY.register(Identifier.make("sandstone"), Block.SANDSTONE);
		BLOCK_REGISTRY.register(Identifier.make("note_block"), Block.NOTEBLOCK);
		BLOCK_REGISTRY.register(Identifier.make("bed"), Block.BED);
		BLOCK_REGISTRY.register(Identifier.make("powered_rail"), Block.GOLDEN_RAIL);
		BLOCK_REGISTRY.register(Identifier.make("detector_rail"), Block.DETECTOR_RAIL);
		BLOCK_REGISTRY.register(Identifier.make("sticky_piston"), Block.STICKY_PISTON);
		BLOCK_REGISTRY.register(Identifier.make("cobweb"), Block.COBWEB);
		BLOCK_REGISTRY.register(Identifier.make("tall_grass"), Block.TALL_GRASS);
		BLOCK_REGISTRY.register(Identifier.make("deadbush"), Block.DEAD_BUSH);
		BLOCK_REGISTRY.register(Identifier.make("piston"), Block.PISTON);
		BLOCK_REGISTRY.register(Identifier.make("piston_head"), Block.PISTON_HEAD);
		BLOCK_REGISTRY.register(Identifier.make("wool"), Block.WOOL);
		BLOCK_REGISTRY.register(Identifier.make("moving_piston"), Block.MOVING_PISTON);
		BLOCK_REGISTRY.register(Identifier.make("dandelion"), Block.DANDELION);
		BLOCK_REGISTRY.register(Identifier.make("rose"), Block.ROSE);
		BLOCK_REGISTRY.register(Identifier.make("brown_mushroom"), Block.BROWN_MUSHROOM);
		BLOCK_REGISTRY.register(Identifier.make("red_mushroom"), Block.RED_MUSHROOM);
		BLOCK_REGISTRY.register(Identifier.make("gold_block"), Block.GOLD_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("iron_block"), Block.IRON_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("double_slab"), Block.DOUBLE_STONE_SLAB);
		BLOCK_REGISTRY.register(Identifier.make("slab"), Block.STONE_SLAB);
		BLOCK_REGISTRY.register(Identifier.make("bricks"), Block.BRICKS);
		BLOCK_REGISTRY.register(Identifier.make("tnt"), Block.TNT);
		BLOCK_REGISTRY.register(Identifier.make("bookshelf"), Block.BOOKSHELF);
		BLOCK_REGISTRY.register(Identifier.make("mossy_cobblestone"), Block.MOSSY_COBBLESTONE);
		BLOCK_REGISTRY.register(Identifier.make("obsidian"), Block.OBSIDIAN);
		BLOCK_REGISTRY.register(Identifier.make("torch"), Block.TORCH);
		BLOCK_REGISTRY.register(Identifier.make("fire"), Block.FIRE);
		BLOCK_REGISTRY.register(Identifier.make("spawner"), Block.MOB_SPAWNER);
		BLOCK_REGISTRY.register(Identifier.make("wooden_stairs"), Block.WOOD_STAIRS);
		BLOCK_REGISTRY.register(Identifier.make("chest"), Block.CHEST);
		BLOCK_REGISTRY.register(Identifier.make("redstone"), Block.REDSTONE_DUST);
		BLOCK_REGISTRY.register(Identifier.make("diamond_ore"), Block.DIAMOND_ORE);
		BLOCK_REGISTRY.register(Identifier.make("diamond_block"), Block.DIAMOND_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("crafting_table"), Block.WORKBENCH);
		BLOCK_REGISTRY.register(Identifier.make("crops"), Block.CROPS);
		BLOCK_REGISTRY.register(Identifier.make("farmland"), Block.FARMLAND);
		BLOCK_REGISTRY.register(Identifier.make("furnace"), Block.FURNACE);
		BLOCK_REGISTRY.register(Identifier.make("furnace_lit"), Block.FURNACE_LIT);
		BLOCK_REGISTRY.register(Identifier.make("standing_sign"), Block.STANDING_SIGN);
		BLOCK_REGISTRY.register(Identifier.make("wood_door"), Block.WOOD_DOOR);
		BLOCK_REGISTRY.register(Identifier.make("ladder"), Block.LADDER);
		BLOCK_REGISTRY.register(Identifier.make("rail"), Block.RAIL);
		BLOCK_REGISTRY.register(Identifier.make("cobblestone_stairs"), Block.COBBLESTONE_STAIRS);
		BLOCK_REGISTRY.register(Identifier.make("wall_sign"), Block.WALL_SIGN);
		BLOCK_REGISTRY.register(Identifier.make("lever"), Block.LEVER);
		BLOCK_REGISTRY.register(Identifier.make("wooden_pressure_plate"), Block.WOODEN_PRESSURE_PLATE);
		BLOCK_REGISTRY.register(Identifier.make("iron_door"), Block.IRON_DOOR);
		BLOCK_REGISTRY.register(Identifier.make("stone_pressure_plate"), Block.STONE_PRESSURE_PLATE);
		BLOCK_REGISTRY.register(Identifier.make("redstone_ore"), Block.REDSTONE_ORE);
		BLOCK_REGISTRY.register(Identifier.make("redstone_ore_lit"), Block.REDSTONE_ORE_LIT);
		BLOCK_REGISTRY.register(Identifier.make("redstone_torch"), Block.REDSTONE_TORCH);
		BLOCK_REGISTRY.register(Identifier.make("redstone_torch_lit"), Block.REDSTONE_TORCH_LIT);
		BLOCK_REGISTRY.register(Identifier.make("button"), Block.BUTTON);
		BLOCK_REGISTRY.register(Identifier.make("snow"), Block.SNOW);
		BLOCK_REGISTRY.register(Identifier.make("ice"), Block.ICE);
		BLOCK_REGISTRY.register(Identifier.make("snow_block"), Block.SNOW_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("cactus"), Block.CACTUS);
		BLOCK_REGISTRY.register(Identifier.make("clay"), Block.CLAY);
		BLOCK_REGISTRY.register(Identifier.make("sugar_cane"), Block.SUGAR_CANES);
		BLOCK_REGISTRY.register(Identifier.make("jukebox"), Block.JUKEBOX);
		BLOCK_REGISTRY.register(Identifier.make("fence"), Block.FENCE);
		BLOCK_REGISTRY.register(Identifier.make("pumpkin"), Block.PUMPKIN);
		BLOCK_REGISTRY.register(Identifier.make("netherrack"), Block.NETHERRACK);
		BLOCK_REGISTRY.register(Identifier.make("soul_sand"), Block.SOUL_SAND);
		BLOCK_REGISTRY.register(Identifier.make("glowstone"), Block.GLOWSTONE);
		BLOCK_REGISTRY.register(Identifier.make("portal"), Block.PORTAL);
		BLOCK_REGISTRY.register(Identifier.make("jack_o_lantern"), Block.JACK_O_LANTERN);
		BLOCK_REGISTRY.register(Identifier.make("cake"), Block.CAKE);
		BLOCK_REGISTRY.register(Identifier.make("repeater"), Block.REDSTONE_REPEATER);
		BLOCK_REGISTRY.register(Identifier.make("repeater_lit"), Block.REDSTONE_REPEATER_LIT);
		BLOCK_REGISTRY.register(Identifier.make("locked_chest"), Block.LOCKED_CHEST);
		BLOCK_REGISTRY.register(Identifier.make("trapdoor"), Block.TRAPDOOR);
		
		Set<Identifier> customNames = new HashSet<>();
		customNames.add(Identifier.make("sapling"));
		customNames.add(Identifier.make("log"));
		customNames.add(Identifier.make("leaves"));
		customNames.add(Identifier.make("wool"));
		customNames.add(Identifier.make("slab"));
		customNames.add(Identifier.make("redstone_torch"));
		customNames.add(Identifier.make("redstone_torch_lit"));
		customNames.add(Identifier.make("tall_grass"));
		customNames.add(Identifier.make("air"));
		customNames.add(Identifier.make("flowing_water"));
		customNames.add(Identifier.make("static_water"));
		customNames.add(Identifier.make("flowing_lava"));
		customNames.add(Identifier.make("static_lava"));
		customNames.add(Identifier.make("deadbush"));
		customNames.add(Identifier.make("piston_head"));
		customNames.add(Identifier.make("moving_piston"));
		customNames.add(Identifier.make("double_slab"));
		customNames.add(Identifier.make("fire"));
		customNames.add(Identifier.make("furnace_lit"));
		customNames.add(Identifier.make("wall_sign"));
		customNames.add(Identifier.make("standing_sign"));
		customNames.add(Identifier.make("repeater_lit"));
		
		BLOCK_REGISTRY.forEach(block -> {
			BlockState state = BlockStateContainer.cast(block).bhapi_getDefaultState();
			state.getPossibleStates(); // Make sure that all vanilla blocks are generated on startup
			Identifier id = BLOCK_REGISTRY.getID(block);
			if (customNames.contains(id)) return;
			BHBlockItem item = new BHBlockItem(state, itemIsFlat(block));
			ITEM_REGISTRY.register(id, item);
		});
		
		String[] names = new String[] {"oak", "spruce", "birch"};
		for (byte i = 0; i < 3; i++) {
			addVariant(names[i] + "_sapling", Block.SAPLING, i);
			addVariant(names[i] + "_log", Block.LOG, i);
			addVariant(names[i] + "_leaves", Block.LEAVES, i);
		}
		
		for (byte i = 0; i < 16; i++) {
			String name = DyeUtil.BLOCK_NAMES[i] + "_wool";
			addVariant(name, Block.WOOL, i);
		}
		
		addVariant("redstone_torch", Block.STONE_SLAB, 0);
		BlockState state = BlockState.getDefaultState(Block.REDSTONE_TORCH_LIT);
		ITEM_REGISTRY.register(Identifier.make("redstone_torch"), new BHBlockItem(state, true));
		
		addVariant("stone_slab", Block.STONE_SLAB, 0);
		addVariant("sandstone_slab", Block.STONE_SLAB, 1);
		addVariant("wooden_slab", Block.STONE_SLAB, 2);
		addVariant("cobblestone_slab", Block.STONE_SLAB, 3);
		
		addVariant("dead_bush", Block.TALL_GRASS, 0);
		addVariant("tall_grass", Block.TALL_GRASS, 1);
		addVariant("fern", Block.TALL_GRASS, 2);
	}
	
	private static void addVariant(String name, Block block, int meta) {
		BlockState state = BlockState.getDefaultState(block).withMeta(meta);
		Identifier id = Identifier.make(name);
		boolean isFlat = itemIsFlat(block);
		ITEM_REGISTRY.register(id, new BHBlockItem(state, isFlat));
	}
	
	private static boolean itemIsFlat(Block block) {
		if (!BHAPI.isClient()) return false;
		return !BlockRenderer.isSpecificRenderType(block.getRenderType());
	}
	
	private static void initItems() {
		ITEM_REGISTRY.register(Identifier.make("iron_shovel"), Item.ironShovel);
		ITEM_REGISTRY.register(Identifier.make("iron_pickaxe"), Item.ironPickaxe);
		ITEM_REGISTRY.register(Identifier.make("iron_axe"), Item.ironAxe);
		ITEM_REGISTRY.register(Identifier.make("flint_and_steel"), Item.flintAndSteel);
		ITEM_REGISTRY.register(Identifier.make("apple"), Item.apple);
		ITEM_REGISTRY.register(Identifier.make("bow"), Item.bow);
		ITEM_REGISTRY.register(Identifier.make("arrow"), Item.arrow);
		ITEM_REGISTRY.register(Identifier.make("coal"), Item.coal);
		ITEM_REGISTRY.register(Identifier.make("diamond"), Item.diamond);
		ITEM_REGISTRY.register(Identifier.make("iron_ingot"), Item.ironIngot);
		ITEM_REGISTRY.register(Identifier.make("gold_ingot"), Item.goldIngot);
		ITEM_REGISTRY.register(Identifier.make("iron_sword"), Item.ironSword);
		ITEM_REGISTRY.register(Identifier.make("wooden_sword"), Item.woodSword);
		ITEM_REGISTRY.register(Identifier.make("wooden_shovel"), Item.woodShovel);
		ITEM_REGISTRY.register(Identifier.make("wooden_pickaxe"), Item.woodPickaxe);
		ITEM_REGISTRY.register(Identifier.make("wooden_axe"), Item.woodAxe);
		ITEM_REGISTRY.register(Identifier.make("stone_sword"), Item.stoneSword);
		ITEM_REGISTRY.register(Identifier.make("stone_shovel"), Item.stoneShovel);
		ITEM_REGISTRY.register(Identifier.make("stone_pickaxe"), Item.stonePickaxe);
		ITEM_REGISTRY.register(Identifier.make("stone_axe"), Item.stoneAxe);
		ITEM_REGISTRY.register(Identifier.make("diamond_sword"), Item.diamondSword);
		ITEM_REGISTRY.register(Identifier.make("diamond_shovel"), Item.diamondShovel);
		ITEM_REGISTRY.register(Identifier.make("diamond_pickaxe"), Item.diamondPickaxe);
		ITEM_REGISTRY.register(Identifier.make("diamond_axe"), Item.diamondAxe);
		ITEM_REGISTRY.register(Identifier.make("stick"), Item.stick);
		ITEM_REGISTRY.register(Identifier.make("bowl"), Item.bowl);
		ITEM_REGISTRY.register(Identifier.make("mushroom_stew"), Item.mushroomStew);
		ITEM_REGISTRY.register(Identifier.make("golden_sword"), Item.goldSword);
		ITEM_REGISTRY.register(Identifier.make("golden_shovel"), Item.goldShovel);
		ITEM_REGISTRY.register(Identifier.make("golden_pickaxe"), Item.goldPickaxe);
		ITEM_REGISTRY.register(Identifier.make("golden_axe"), Item.goldAxe);
		ITEM_REGISTRY.register(Identifier.make("string"), Item.string);
		ITEM_REGISTRY.register(Identifier.make("feather"), Item.feather);
		ITEM_REGISTRY.register(Identifier.make("gunpowder"), Item.gunpowder);
		ITEM_REGISTRY.register(Identifier.make("wooden_hoe"), Item.woodHoe);
		ITEM_REGISTRY.register(Identifier.make("stone_hoe"), Item.stoneHoe);
		ITEM_REGISTRY.register(Identifier.make("iron_hoe"), Item.ironHoe);
		ITEM_REGISTRY.register(Identifier.make("diamond_hoe"), Item.diamondHoe);
		ITEM_REGISTRY.register(Identifier.make("golden_hoe"), Item.goldHoe);
		ITEM_REGISTRY.register(Identifier.make("wheat_seeds"), Item.seeds);
		ITEM_REGISTRY.register(Identifier.make("wheat"), Item.wheat);
		ITEM_REGISTRY.register(Identifier.make("bread"), Item.bread);
		ITEM_REGISTRY.register(Identifier.make("leather_helmet"), Item.leatherHelmet);
		ITEM_REGISTRY.register(Identifier.make("leather_chestplate"), Item.leatherChestplate);
		ITEM_REGISTRY.register(Identifier.make("leather_leggings"), Item.leatherLeggings);
		ITEM_REGISTRY.register(Identifier.make("leather_boots"), Item.leatherBoots);
		ITEM_REGISTRY.register(Identifier.make("chainmail_helmet"), Item.chainHelmet);
		ITEM_REGISTRY.register(Identifier.make("chainmail_chestplate"), Item.chainChestplate);
		ITEM_REGISTRY.register(Identifier.make("chainmail_leggings"), Item.chainLeggings);
		ITEM_REGISTRY.register(Identifier.make("chainmail_boots"), Item.chainBoots);
		ITEM_REGISTRY.register(Identifier.make("iron_helmet"), Item.ironHelmet);
		ITEM_REGISTRY.register(Identifier.make("iron_chestplate"), Item.ironChestplate);
		ITEM_REGISTRY.register(Identifier.make("iron_leggings"), Item.ironLeggings);
		ITEM_REGISTRY.register(Identifier.make("iron_boots"), Item.ironBoots);
		ITEM_REGISTRY.register(Identifier.make("diamond_helmet"), Item.diamondHelmet);
		ITEM_REGISTRY.register(Identifier.make("diamond_chestplate"), Item.diamondChestplate);
		ITEM_REGISTRY.register(Identifier.make("diamond_leggings"), Item.diamondLeggings);
		ITEM_REGISTRY.register(Identifier.make("diamond_boots"), Item.diamondBoots);
		ITEM_REGISTRY.register(Identifier.make("golden_helmet"), Item.goldHelmet);
		ITEM_REGISTRY.register(Identifier.make("golden_chestplate"), Item.goldChestplate);
		ITEM_REGISTRY.register(Identifier.make("golden_leggings"), Item.goldLeggings);
		ITEM_REGISTRY.register(Identifier.make("golden_boots"), Item.goldBoots);
		ITEM_REGISTRY.register(Identifier.make("flint"), Item.flint);
		ITEM_REGISTRY.register(Identifier.make("porkchop"), Item.rawPorkchop);
		ITEM_REGISTRY.register(Identifier.make("cooked_porkchop"), Item.cookedPorkchop);
		ITEM_REGISTRY.register(Identifier.make("painting"), Item.painting);
		ITEM_REGISTRY.register(Identifier.make("golden_apple"), Item.goldenApple);
		ITEM_REGISTRY.register(Identifier.make("sign"), Item.sign);
		ITEM_REGISTRY.register(Identifier.make("wooden_door"), Item.woodDoor);
		ITEM_REGISTRY.register(Identifier.make("bucket"), Item.bucket);
		ITEM_REGISTRY.register(Identifier.make("water_bucket"), Item.waterBucket);
		ITEM_REGISTRY.register(Identifier.make("lava_bucket"), Item.lavaBucket);
		ITEM_REGISTRY.register(Identifier.make("minecart"), Item.minecart);
		ITEM_REGISTRY.register(Identifier.make("saddle"), Item.saddle);
		ITEM_REGISTRY.register(Identifier.make("iron_door"), Item.ironDoor);
		ITEM_REGISTRY.register(Identifier.make("redstone"), Item.redstoneDust);
		ITEM_REGISTRY.register(Identifier.make("snowball"), Item.snowball);
		ITEM_REGISTRY.register(Identifier.make("boat"), Item.boat);
		ITEM_REGISTRY.register(Identifier.make("leather"), Item.leather);
		ITEM_REGISTRY.register(Identifier.make("milk_bucket"), Item.milk);
		ITEM_REGISTRY.register(Identifier.make("brick"), Item.brick);
		ITEM_REGISTRY.register(Identifier.make("clay_ball"), Item.clay);
		ITEM_REGISTRY.register(Identifier.make("sugar_cane"), Item.sugarCanes);
		ITEM_REGISTRY.register(Identifier.make("paper"), Item.paper);
		ITEM_REGISTRY.register(Identifier.make("book"), Item.book);
		ITEM_REGISTRY.register(Identifier.make("slime_ball"), Item.slimeball);
		ITEM_REGISTRY.register(Identifier.make("chest_minecart"), Item.minecartChest);
		ITEM_REGISTRY.register(Identifier.make("furnace_minecart"), Item.minecartFurnace);
		ITEM_REGISTRY.register(Identifier.make("egg"), Item.egg);
		ITEM_REGISTRY.register(Identifier.make("compass"), Item.compass);
		ITEM_REGISTRY.register(Identifier.make("fishing_rod"), Item.fishingRod);
		ITEM_REGISTRY.register(Identifier.make("clock"), Item.clock);
		ITEM_REGISTRY.register(Identifier.make("glowstone_dust"), Item.glowstoneDust);
		ITEM_REGISTRY.register(Identifier.make("fish"), Item.rawFish);
		ITEM_REGISTRY.register(Identifier.make("cooked_fish"), Item.cookedFish);
		ITEM_REGISTRY.register(Identifier.make("dye"), Item.dyePowder);
		ITEM_REGISTRY.register(Identifier.make("bone"), Item.bone);
		ITEM_REGISTRY.register(Identifier.make("sugar"), Item.sugar);
		ITEM_REGISTRY.register(Identifier.make("cake"), Item.cake);
		ITEM_REGISTRY.register(Identifier.make("bed"), Item.bed);
		ITEM_REGISTRY.register(Identifier.make("repeater"), Item.redstoneRepeater);
		ITEM_REGISTRY.register(Identifier.make("cookie"), Item.cookie);
		ITEM_REGISTRY.register(Identifier.make("map"), Item.map);
		ITEM_REGISTRY.register(Identifier.make("shears"), Item.shears);
		ITEM_REGISTRY.register(Identifier.make("record_13"), Item.record13);
		ITEM_REGISTRY.register(Identifier.make("record_cat"), Item.recordCat);
	}
	
	@Environment(EnvType.SERVER)
	private static void initCommands() {
		COMMAND_REGISTRY.put("give", (command, server, args) -> {
			ServerPlayerConnectionManager serverPlayerConnectionManager = server.serverPlayerConnectionManager;
			CommandSource commandSource = command.source;
			String name = commandSource.getName();
			
			if (args.length < 2 || args.length > 3) {
				commandSource.sendFeedback("Wrong args, usage: /give [player] item");
				return;
			}
			
			Identifier id = Identifier.make(args[args.length - 1]);
			Item item = CommonRegistries.ITEM_REGISTRY.get(id);
			if (item == null) {
				commandSource.sendFeedback("No such item: " + id);
				return;
			}
			
			ItemStack stack = new ItemStack(item);
			ServerPlayer player;
			
			if (args.length == 2) {
				player = serverPlayerConnectionManager.getServerPlayer(name);
				if (player == null) return;
				if (!player.inventory.addStack(stack)) {
					player.dropItem(stack);
				}
			}
			else {
				player = serverPlayerConnectionManager.getServerPlayer(args[1]);
				if (player == null) {
					commandSource.sendFeedback("Can't find user " + args[1]);
					return;
				}
				if (!player.inventory.addStack(stack)) {
					player.dropItem(stack);
				}
			}
			
			String log = "Giving " + player.name + " some " + id;
			commandSource.sendFeedback(log);
			BHAPI.log(log);
		});
	}
	
	public static void initRecipes() {
		RecipeRegistry registry = RecipeRegistry.getInstance();
		RecipeRegistryAccessor accessor = (RecipeRegistryAccessor) registry;
		new ToolRecipes().register(registry);
		new WeaponRecipes().register(registry);
		new MaterialBlockRecipes().register(registry);
		new StewAndCookieRecipes().register(registry);
		new UtilitiesAndSandstoneRecipes().register(registry);
		new ArmorRecipes().register(registry);
		new DyeRecipes().register(registry);
		accessor.callAddShapedRecipe(new ItemStack(Item.paper, 3), "###", '#', Item.sugarCanes);
		accessor.callAddShapedRecipe(new ItemStack(Item.book, 1), "#", "#", "#", '#', Item.paper);
		accessor.callAddShapedRecipe(new ItemStack(Block.FENCE, 2), "###", "###", '#', Item.stick);
		accessor.callAddShapedRecipe(new ItemStack(Block.JUKEBOX, 1), "###", "#X#", "###", '#', Block.PLANKS, 'X', Item.diamond);
		accessor.callAddShapedRecipe(new ItemStack(Block.NOTEBLOCK, 1), "###", "#X#", "###", '#', Block.PLANKS, 'X', Item.redstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(Block.BOOKSHELF, 1), "###", "XXX", "###", '#', Block.PLANKS, 'X', Item.book);
		accessor.callAddShapedRecipe(new ItemStack(Block.SNOW_BLOCK, 1), "##", "##", '#', Item.snowball);
		accessor.callAddShapedRecipe(new ItemStack(Block.CLAY, 1), "##", "##", '#', Item.clay);
		accessor.callAddShapedRecipe(new ItemStack(Block.BRICKS, 1), "##", "##", '#', Item.brick);
		accessor.callAddShapedRecipe(new ItemStack(Block.GLOWSTONE, 1), "##", "##", '#', Item.glowstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(Block.WOOL, 1), "##", "##", '#', Item.string);
		accessor.callAddShapedRecipe(new ItemStack(Block.TNT, 1), "X#X", "#X#", "X#X", 'X', Item.gunpowder, '#', Block.SAND);
		accessor.callAddShapedRecipe(new ItemStack(Block.STONE_SLAB, 3, 3), "###", '#', Block.COBBLESTONE);
		accessor.callAddShapedRecipe(new ItemStack(Block.STONE_SLAB, 3, 0), "###", '#', Block.STONE);
		accessor.callAddShapedRecipe(new ItemStack(Block.STONE_SLAB, 3, 1), "###", '#', Block.SANDSTONE);
		accessor.callAddShapedRecipe(new ItemStack(Block.STONE_SLAB, 3, 2), "###", '#', Block.PLANKS);
		accessor.callAddShapedRecipe(new ItemStack(Block.LADDER, 2), "# #", "###", "# #", '#', Item.stick);
		accessor.callAddShapedRecipe(new ItemStack(Item.woodDoor, 1), "##", "##", "##", '#', Block.PLANKS);
		accessor.callAddShapedRecipe(new ItemStack(Block.TRAPDOOR, 2), "###", "###", '#', Block.PLANKS);
		accessor.callAddShapedRecipe(new ItemStack(Item.ironDoor, 1), "##", "##", "##", '#', Item.ironIngot);
		accessor.callAddShapedRecipe(new ItemStack(Item.sign, 1), "###", "###", " X ", '#', Block.PLANKS, 'X', Item.stick);
		accessor.callAddShapedRecipe(new ItemStack(Item.cake, 1), "AAA", "BEB", "CCC", 'A', Item.milk, 'B', Item.sugar, 'C', Item.wheat, 'E', Item.egg);
		accessor.callAddShapedRecipe(new ItemStack(Item.sugar, 1), "#", '#', Item.sugarCanes);
		accessor.callAddShapedRecipe(new ItemStack(Block.PLANKS, 4), "#", '#', Block.LOG);
		accessor.callAddShapedRecipe(new ItemStack(Item.stick, 4), "#", "#", '#', Block.PLANKS);
		accessor.callAddShapedRecipe(new ItemStack(Block.TORCH, 4), "X", "#", 'X', Item.coal, '#', Item.stick);
		accessor.callAddShapedRecipe(new ItemStack(Block.TORCH, 4), "X", "#", 'X', new ItemStack(Item.coal, 1, 1), '#', Item.stick);
		accessor.callAddShapedRecipe(new ItemStack(Item.bowl, 4), "# #", " # ", '#', Block.PLANKS);
		accessor.callAddShapedRecipe(new ItemStack(Block.RAIL, 16), "X X", "X#X", "X X", 'X', Item.ironIngot, '#', Item.stick);
		accessor.callAddShapedRecipe(new ItemStack(Block.GOLDEN_RAIL, 6), "X X", "X#X", "XRX", 'X', Item.goldIngot, 'R', Item.redstoneDust, '#', Item.stick);
		accessor.callAddShapedRecipe(new ItemStack(Block.DETECTOR_RAIL, 6), "X X", "X#X", "XRX", 'X', Item.ironIngot, 'R', Item.redstoneDust, '#', Block.WOODEN_PRESSURE_PLATE);
		accessor.callAddShapedRecipe(new ItemStack(Item.minecart, 1), "# #", "###", '#', Item.ironIngot);
		accessor.callAddShapedRecipe(new ItemStack(Block.JACK_O_LANTERN, 1), "A", "B", 'A', Block.PUMPKIN, 'B', Block.TORCH);
		accessor.callAddShapedRecipe(new ItemStack(Item.minecartChest, 1), "A", "B", 'A', Block.CHEST, 'B', Item.minecart);
		accessor.callAddShapedRecipe(new ItemStack(Item.minecartFurnace, 1), "A", "B", 'A', Block.FURNACE, 'B', Item.minecart);
		accessor.callAddShapedRecipe(new ItemStack(Item.boat, 1), "# #", "###", '#', Block.PLANKS);
		accessor.callAddShapedRecipe(new ItemStack(Item.bucket, 1), "# #", " # ", '#', Item.ironIngot);
		accessor.callAddShapedRecipe(new ItemStack(Item.flintAndSteel, 1), "A ", " B", 'A', Item.ironIngot, 'B', Item.flint);
		accessor.callAddShapedRecipe(new ItemStack(Item.bread, 1), "###", '#', Item.wheat);
		accessor.callAddShapedRecipe(new ItemStack(Block.WOOD_STAIRS, 4), "#  ", "## ", "###", '#', Block.PLANKS);
		accessor.callAddShapedRecipe(new ItemStack(Item.fishingRod, 1), "  #", " #X", "# X", '#', Item.stick, 'X', Item.string);
		accessor.callAddShapedRecipe(new ItemStack(Block.COBBLESTONE_STAIRS, 4), "#  ", "## ", "###", '#', Block.COBBLESTONE);
		accessor.callAddShapedRecipe(new ItemStack(Item.painting, 1), "###", "#X#", "###", '#', Item.stick, 'X', Block.WOOL);
		accessor.callAddShapedRecipe(new ItemStack(Item.goldenApple, 1), "###", "#X#", "###", '#', Block.GOLD_BLOCK, 'X', Item.apple);
		accessor.callAddShapedRecipe(new ItemStack(Block.LEVER, 1), "X", "#", '#', Block.COBBLESTONE, 'X', Item.stick);
		accessor.callAddShapedRecipe(new ItemStack(Block.REDSTONE_TORCH_LIT, 1), "X", "#", '#', Item.stick, 'X', Item.redstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(Item.redstoneRepeater, 1), "#X#", "III", '#', Block.REDSTONE_TORCH_LIT, 'X', Item.redstoneDust, 'I', Block.STONE);
		accessor.callAddShapedRecipe(new ItemStack(Item.clock, 1), " # ", "#X#", " # ", '#', Item.goldIngot, 'X', Item.redstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(Item.compass, 1), " # ", "#X#", " # ", '#', Item.ironIngot, 'X', Item.redstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(Item.map, 1), "###", "#X#", "###", '#', Item.paper, 'X', Item.compass);
		accessor.callAddShapedRecipe(new ItemStack(Block.BUTTON, 1), "#", "#", '#', Block.STONE);
		accessor.callAddShapedRecipe(new ItemStack(Block.WOODEN_PRESSURE_PLATE, 1), "##", '#', Block.STONE);
		accessor.callAddShapedRecipe(new ItemStack(Block.STONE_PRESSURE_PLATE, 1), "##", '#', Block.PLANKS);
		accessor.callAddShapedRecipe(new ItemStack(Block.DISPENSER, 1), "###", "#X#", "#R#", '#', Block.COBBLESTONE, 'X', Item.bow, 'R', Item.redstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(Block.PISTON, 1), "TTT", "#X#", "#R#", '#', Block.COBBLESTONE, 'X', Item.ironIngot, 'R', Item.redstoneDust, 'T', Block.PLANKS);
		accessor.callAddShapedRecipe(new ItemStack(Block.STICKY_PISTON, 1), "S", "P", 'S', Item.slimeball, 'P', Block.PISTON);
		accessor.callAddShapedRecipe(new ItemStack(Item.bed, 1), "###", "XXX", '#', Block.WOOL, 'X', Block.PLANKS);
	}
	
	private static void initEvents() {
		EVENT_REGISTRY.put(StartupEvent.class, StartupEvent::new);
		EVENT_REGISTRY.put(BlockRegistryEvent.class, () -> new BlockRegistryEvent(BLOCK_REGISTRY));
		EVENT_REGISTRY.put(ItemRegistryEvent.class, () -> new ItemRegistryEvent(ITEM_REGISTRY));
		EVENT_REGISTRY.put(AfterBlockAndItemsEvent.class, AfterBlockAndItemsEvent::new);
		EVENT_REGISTRY.put(RecipeRegistryEvent.class, () -> new RecipeRegistryEvent(RecipeRegistry.getInstance()));
		EVENT_REGISTRY.put(EventRegistrationEvent.class, () -> new EventRegistrationEvent(EVENT_REGISTRY));
		if (BHAPI.isServer()) {
			EVENT_REGISTRY.put(CommandRegistryEvent.class, () -> new CommandRegistryEvent(COMMAND_REGISTRY));
		}
	}
}
