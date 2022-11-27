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
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ArmourRecipes;
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
import java.util.Map;
import java.util.function.Supplier;

public class CommonRegistries {
	public static final Registry<BaseBlock> BLOCK_REGISTRY = new Registry<>();
	public static final Registry<BaseItem> ITEM_REGISTRY = new Registry<>();
	public static final Map<Class<? extends BHEvent>, Supplier<? extends BHEvent>> EVENT_REGISTRY = new HashMap<>();
	
	@Environment(EnvType.SERVER)
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
		BLOCK_REGISTRY.register(Identifier.make("stone"), BaseBlock.STONE);
		BLOCK_REGISTRY.register(Identifier.make("grass_block"), BaseBlock.GRASS);
		BLOCK_REGISTRY.register(Identifier.make("dirt"), BaseBlock.DIRT);
		BLOCK_REGISTRY.register(Identifier.make("cobblestone"), BaseBlock.COBBLESTONE);
		BLOCK_REGISTRY.register(Identifier.make("planks"), BaseBlock.WOOD);
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
		BLOCK_REGISTRY.register(Identifier.make("lapis_ore"), BaseBlock.LAPIS_LAZULI_ORE);
		BLOCK_REGISTRY.register(Identifier.make("lapis_block"), BaseBlock.LAPIS_LAZULI_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("dispenser"), BaseBlock.DISPENSER);
		BLOCK_REGISTRY.register(Identifier.make("sandstone"), BaseBlock.SANDSTONE);
		BLOCK_REGISTRY.register(Identifier.make("note_block"), BaseBlock.NOTEBLOCK);
		BLOCK_REGISTRY.register(Identifier.make("bed"), BaseBlock.BED);
		BLOCK_REGISTRY.register(Identifier.make("powered_rail"), BaseBlock.GOLDEN_RAIL);
		BLOCK_REGISTRY.register(Identifier.make("detector_rail"), BaseBlock.DETECTOR_RAIL);
		BLOCK_REGISTRY.register(Identifier.make("sticky_piston"), BaseBlock.STICKY_PISTON);
		BLOCK_REGISTRY.register(Identifier.make("cobweb"), BaseBlock.COBWEB);
		BLOCK_REGISTRY.register(Identifier.make("tall_grass"), BaseBlock.TALLGRASS);
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
		BLOCK_REGISTRY.register(Identifier.make("double_slab"), BaseBlock.DOUBLE_STONE_SLAB);
		BLOCK_REGISTRY.register(Identifier.make("slab"), BaseBlock.STONE_SLAB);
		BLOCK_REGISTRY.register(Identifier.make("bricks"), BaseBlock.BRICKS);
		BLOCK_REGISTRY.register(Identifier.make("tnt"), BaseBlock.TNT);
		BLOCK_REGISTRY.register(Identifier.make("bookshelf"), BaseBlock.BOOKSHELF);
		BLOCK_REGISTRY.register(Identifier.make("mossy_cobblestone"), BaseBlock.MOSSY_COBBLESTONE);
		BLOCK_REGISTRY.register(Identifier.make("obsidian"), BaseBlock.OBSIDIAN);
		BLOCK_REGISTRY.register(Identifier.make("torch"), BaseBlock.TORCH);
		BLOCK_REGISTRY.register(Identifier.make("fire"), BaseBlock.FIRE);
		BLOCK_REGISTRY.register(Identifier.make("spawner"), BaseBlock.MOB_SPAWNER);
		BLOCK_REGISTRY.register(Identifier.make("wooden_stairs"), BaseBlock.WOOD_STAIRS);
		BLOCK_REGISTRY.register(Identifier.make("chest"), BaseBlock.CHEST);
		BLOCK_REGISTRY.register(Identifier.make("redstone"), BaseBlock.REDSTONE_DUST);
		BLOCK_REGISTRY.register(Identifier.make("diamond_ore"), BaseBlock.DIAMOND_ORE);
		BLOCK_REGISTRY.register(Identifier.make("diamond_block"), BaseBlock.DIAMOND_BLOCK);
		BLOCK_REGISTRY.register(Identifier.make("crafting_table"), BaseBlock.WORKBENCH);
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
		BLOCK_REGISTRY.register(Identifier.make("sugar_cane"), BaseBlock.SUGAR_CANES);
		BLOCK_REGISTRY.register(Identifier.make("jukebox"), BaseBlock.JUKEBOX);
		BLOCK_REGISTRY.register(Identifier.make("fence"), BaseBlock.FENCE);
		BLOCK_REGISTRY.register(Identifier.make("pumpkin"), BaseBlock.PUMPKIN);
		BLOCK_REGISTRY.register(Identifier.make("netherrack"), BaseBlock.NETHERRACK);
		BLOCK_REGISTRY.register(Identifier.make("soul_sand"), BaseBlock.SOUL_SAND);
		BLOCK_REGISTRY.register(Identifier.make("glowstone"), BaseBlock.GLOWSTONE);
		BLOCK_REGISTRY.register(Identifier.make("portal"), BaseBlock.PORTAL);
		BLOCK_REGISTRY.register(Identifier.make("jack_o_lantern"), BaseBlock.JACK_O_LANTERN);
		BLOCK_REGISTRY.register(Identifier.make("cake"), BaseBlock.CAKE);
		BLOCK_REGISTRY.register(Identifier.make("repeater"), BaseBlock.REDSTONE_REPEATER);
		BLOCK_REGISTRY.register(Identifier.make("repeater_lit"), BaseBlock.REDSTONE_REPEATER_LIT);
		BLOCK_REGISTRY.register(Identifier.make("locked_chest"), BaseBlock.LOCKED_CHEST);
		BLOCK_REGISTRY.register(Identifier.make("trapdoor"), BaseBlock.TRAPDOOR);
		
		BLOCK_REGISTRY.forEach(block -> {
			BlockState state = BlockStateContainer.cast(block).getDefaultState();
			state.getPossibleStates(); // Make sure that all vanilla blocks are generated on startup
			Identifier id = BLOCK_REGISTRY.getID(block);
			BHBlockItem item = new BHBlockItem(state, itemIsFlat(block));
			ITEM_REGISTRY.register(id, item);
		});
		
		addVariants("sapling", BaseBlock.SAPLING, 3);
		addVariants("log", BaseBlock.LOG, 3);
		addVariants("leaves", BaseBlock.LEAVES, 3);
		addVariants("slab", BaseBlock.STONE_SLAB, 4);
		addVariants("wool", BaseBlock.WOOL, 16);
		
		BlockState state = BlockState.getDefaultState(BaseBlock.TALLGRASS);
		ITEM_REGISTRY.register(
			Identifier.make("tall_grass"),
			new BHBlockItem(state.withMeta(1), true)
		);
		ITEM_REGISTRY.register(
			Identifier.make("fern"),
			new BHBlockItem(state.withMeta(2), true)
		);
	}
	
	private static void addVariants(String id, BaseBlock block, int count) {
		BlockState state = BlockState.getDefaultState(block);
		for (int meta = 1; meta < count; meta++) {
			ITEM_REGISTRY.register(Identifier.make(id + "_" + meta), new BHBlockItem(state.withMeta(meta), false));
		}
	}
	
	private static boolean itemIsFlat(BaseBlock block) {
		if (!BHAPI.isClient()) return false;
		return !BlockRenderer.isSpecificRenderType(block.getRenderType());
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
		ITEM_REGISTRY.register(Identifier.make("wooden_sword"), BaseItem.woodSword);
		ITEM_REGISTRY.register(Identifier.make("wooden_shovel"), BaseItem.woodShovel);
		ITEM_REGISTRY.register(Identifier.make("wooden_pickaxe"), BaseItem.woodPickaxe);
		ITEM_REGISTRY.register(Identifier.make("wooden_axe"), BaseItem.woodAxe);
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
		ITEM_REGISTRY.register(Identifier.make("golden_sword"), BaseItem.goldSword);
		ITEM_REGISTRY.register(Identifier.make("golden_shovel"), BaseItem.goldShovel);
		ITEM_REGISTRY.register(Identifier.make("golden_pickaxe"), BaseItem.goldPickaxe);
		ITEM_REGISTRY.register(Identifier.make("golden_axe"), BaseItem.goldAxe);
		ITEM_REGISTRY.register(Identifier.make("string"), BaseItem.string);
		ITEM_REGISTRY.register(Identifier.make("feather"), BaseItem.feather);
		ITEM_REGISTRY.register(Identifier.make("gunpowder"), BaseItem.gunpowder);
		ITEM_REGISTRY.register(Identifier.make("wooden_hoe"), BaseItem.woodHoe);
		ITEM_REGISTRY.register(Identifier.make("stone_hoe"), BaseItem.stoneHoe);
		ITEM_REGISTRY.register(Identifier.make("iron_hoe"), BaseItem.ironHoe);
		ITEM_REGISTRY.register(Identifier.make("diamond_hoe"), BaseItem.diamondHoe);
		ITEM_REGISTRY.register(Identifier.make("golden_hoe"), BaseItem.goldHoe);
		ITEM_REGISTRY.register(Identifier.make("wheat_seeds"), BaseItem.seeds);
		ITEM_REGISTRY.register(Identifier.make("wheat"), BaseItem.wheat);
		ITEM_REGISTRY.register(Identifier.make("bread"), BaseItem.bread);
		ITEM_REGISTRY.register(Identifier.make("leather_helmet"), BaseItem.leatherHelmet);
		ITEM_REGISTRY.register(Identifier.make("leather_chestplate"), BaseItem.leatherChestplate);
		ITEM_REGISTRY.register(Identifier.make("leather_leggings"), BaseItem.leatherLeggings);
		ITEM_REGISTRY.register(Identifier.make("leather_boots"), BaseItem.leatherBoots);
		ITEM_REGISTRY.register(Identifier.make("chainmail_helmet"), BaseItem.chainHelmet);
		ITEM_REGISTRY.register(Identifier.make("chainmail_chestplate"), BaseItem.chainChestplate);
		ITEM_REGISTRY.register(Identifier.make("chainmail_leggings"), BaseItem.chainLeggings);
		ITEM_REGISTRY.register(Identifier.make("chainmail_boots"), BaseItem.chainBoots);
		ITEM_REGISTRY.register(Identifier.make("iron_helmet"), BaseItem.ironHelmet);
		ITEM_REGISTRY.register(Identifier.make("iron_chestplate"), BaseItem.ironChestplate);
		ITEM_REGISTRY.register(Identifier.make("iron_leggings"), BaseItem.ironLeggings);
		ITEM_REGISTRY.register(Identifier.make("iron_boots"), BaseItem.ironBoots);
		ITEM_REGISTRY.register(Identifier.make("diamond_helmet"), BaseItem.diamondHelmet);
		ITEM_REGISTRY.register(Identifier.make("diamond_chestplate"), BaseItem.diamondChestplate);
		ITEM_REGISTRY.register(Identifier.make("diamond_leggings"), BaseItem.diamondLeggings);
		ITEM_REGISTRY.register(Identifier.make("diamond_boots"), BaseItem.diamondBoots);
		ITEM_REGISTRY.register(Identifier.make("golden_helmet"), BaseItem.goldHelmet);
		ITEM_REGISTRY.register(Identifier.make("golden_chestplate"), BaseItem.goldChestplate);
		ITEM_REGISTRY.register(Identifier.make("golden_leggings"), BaseItem.goldLeggings);
		ITEM_REGISTRY.register(Identifier.make("golden_boots"), BaseItem.goldBoots);
		ITEM_REGISTRY.register(Identifier.make("flint"), BaseItem.flint);
		ITEM_REGISTRY.register(Identifier.make("porkchop"), BaseItem.rawPorkchop);
		ITEM_REGISTRY.register(Identifier.make("cooked_porkchop"), BaseItem.cookedPorkchop);
		ITEM_REGISTRY.register(Identifier.make("painting"), BaseItem.painting);
		ITEM_REGISTRY.register(Identifier.make("golden_apple"), BaseItem.goldenApple);
		ITEM_REGISTRY.register(Identifier.make("sign"), BaseItem.sign);
		ITEM_REGISTRY.register(Identifier.make("wooden_door"), BaseItem.woodDoor);
		ITEM_REGISTRY.register(Identifier.make("bucket"), BaseItem.bucket);
		ITEM_REGISTRY.register(Identifier.make("water_bucket"), BaseItem.waterBucket);
		ITEM_REGISTRY.register(Identifier.make("lava_bucket"), BaseItem.lavaBucket);
		ITEM_REGISTRY.register(Identifier.make("minecart"), BaseItem.minecart);
		ITEM_REGISTRY.register(Identifier.make("saddle"), BaseItem.saddle);
		ITEM_REGISTRY.register(Identifier.make("iron_door"), BaseItem.ironDoor);
		ITEM_REGISTRY.register(Identifier.make("redstone"), BaseItem.redstoneDust);
		ITEM_REGISTRY.register(Identifier.make("snowball"), BaseItem.snowball);
		ITEM_REGISTRY.register(Identifier.make("boat"), BaseItem.boat);
		ITEM_REGISTRY.register(Identifier.make("leather"), BaseItem.leather);
		ITEM_REGISTRY.register(Identifier.make("milk_bucket"), BaseItem.milk);
		ITEM_REGISTRY.register(Identifier.make("brick"), BaseItem.brick);
		ITEM_REGISTRY.register(Identifier.make("clay_ball"), BaseItem.clay);
		ITEM_REGISTRY.register(Identifier.make("sugar_cane"), BaseItem.sugarCanes);
		ITEM_REGISTRY.register(Identifier.make("paper"), BaseItem.paper);
		ITEM_REGISTRY.register(Identifier.make("book"), BaseItem.book);
		ITEM_REGISTRY.register(Identifier.make("slime_ball"), BaseItem.slimeball);
		ITEM_REGISTRY.register(Identifier.make("chest_minecart"), BaseItem.minecartChest);
		ITEM_REGISTRY.register(Identifier.make("furnace_minecart"), BaseItem.minecartFurnace);
		ITEM_REGISTRY.register(Identifier.make("egg"), BaseItem.egg);
		ITEM_REGISTRY.register(Identifier.make("compass"), BaseItem.compass);
		ITEM_REGISTRY.register(Identifier.make("fishing_rod"), BaseItem.fishingRod);
		ITEM_REGISTRY.register(Identifier.make("clock"), BaseItem.clock);
		ITEM_REGISTRY.register(Identifier.make("glowstone_dust"), BaseItem.glowstoneDust);
		ITEM_REGISTRY.register(Identifier.make("fish"), BaseItem.rawFish);
		ITEM_REGISTRY.register(Identifier.make("cooked_fish"), BaseItem.cookedFish);
		ITEM_REGISTRY.register(Identifier.make("dye"), BaseItem.dyePowder);
		ITEM_REGISTRY.register(Identifier.make("bone"), BaseItem.bone);
		ITEM_REGISTRY.register(Identifier.make("sugar"), BaseItem.sugar);
		ITEM_REGISTRY.register(Identifier.make("cake"), BaseItem.cake);
		ITEM_REGISTRY.register(Identifier.make("bed"), BaseItem.bed);
		ITEM_REGISTRY.register(Identifier.make("repeater"), BaseItem.redstoneRepeater);
		ITEM_REGISTRY.register(Identifier.make("cookie"), BaseItem.cookie);
		ITEM_REGISTRY.register(Identifier.make("map"), BaseItem.map);
		ITEM_REGISTRY.register(Identifier.make("shears"), BaseItem.shears);
		ITEM_REGISTRY.register(Identifier.make("record_13"), BaseItem.record13);
		ITEM_REGISTRY.register(Identifier.make("record_cat"), BaseItem.recordCat);
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
			BaseItem item = CommonRegistries.ITEM_REGISTRY.get(id);
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
		new ArmourRecipes().register(registry);
		new DyeRecipes().register(registry);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.paper, 3), "###", '#', BaseItem.sugarCanes);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.book, 1), "#", "#", "#", '#', BaseItem.paper);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.FENCE, 2), "###", "###", '#', BaseItem.stick);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.JUKEBOX, 1), "###", "#X#", "###", '#', BaseBlock.WOOD, 'X', BaseItem.diamond);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.NOTEBLOCK, 1), "###", "#X#", "###", '#', BaseBlock.WOOD, 'X', BaseItem.redstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.BOOKSHELF, 1), "###", "XXX", "###", '#', BaseBlock.WOOD, 'X', BaseItem.book);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.SNOW_BLOCK, 1), "##", "##", '#', BaseItem.snowball);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.CLAY, 1), "##", "##", '#', BaseItem.clay);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.BRICKS, 1), "##", "##", '#', BaseItem.brick);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.GLOWSTONE, 1), "##", "##", '#', BaseItem.glowstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.WOOL, 1), "##", "##", '#', BaseItem.string);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.TNT, 1), "X#X", "#X#", "X#X", 'X', BaseItem.gunpowder, '#', BaseBlock.SAND);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.STONE_SLAB, 3, 3), "###", '#', BaseBlock.COBBLESTONE);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.STONE_SLAB, 3, 0), "###", '#', BaseBlock.STONE);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.STONE_SLAB, 3, 1), "###", '#', BaseBlock.SANDSTONE);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.STONE_SLAB, 3, 2), "###", '#', BaseBlock.WOOD);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.LADDER, 2), "# #", "###", "# #", '#', BaseItem.stick);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.woodDoor, 1), "##", "##", "##", '#', BaseBlock.WOOD);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.TRAPDOOR, 2), "###", "###", '#', BaseBlock.WOOD);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.ironDoor, 1), "##", "##", "##", '#', BaseItem.ironIngot);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.sign, 1), "###", "###", " X ", '#', BaseBlock.WOOD, 'X', BaseItem.stick);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.cake, 1), "AAA", "BEB", "CCC", 'A', BaseItem.milk, 'B', BaseItem.sugar, 'C', BaseItem.wheat, 'E', BaseItem.egg);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.sugar, 1), "#", '#', BaseItem.sugarCanes);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.WOOD, 4), "#", '#', BaseBlock.LOG);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.stick, 4), "#", "#", '#', BaseBlock.WOOD);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.TORCH, 4), "X", "#", 'X', BaseItem.coal, '#', BaseItem.stick);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.TORCH, 4), "X", "#", 'X', new ItemStack(BaseItem.coal, 1, 1), '#', BaseItem.stick);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.bowl, 4), "# #", " # ", '#', BaseBlock.WOOD);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.RAIL, 16), "X X", "X#X", "X X", 'X', BaseItem.ironIngot, '#', BaseItem.stick);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.GOLDEN_RAIL, 6), "X X", "X#X", "XRX", 'X', BaseItem.goldIngot, 'R', BaseItem.redstoneDust, '#', BaseItem.stick);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.DETECTOR_RAIL, 6), "X X", "X#X", "XRX", 'X', BaseItem.ironIngot, 'R', BaseItem.redstoneDust, '#', BaseBlock.WOODEN_PRESSURE_PLATE);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.minecart, 1), "# #", "###", '#', BaseItem.ironIngot);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.JACK_O_LANTERN, 1), "A", "B", 'A', BaseBlock.PUMPKIN, 'B', BaseBlock.TORCH);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.minecartChest, 1), "A", "B", 'A', BaseBlock.CHEST, 'B', BaseItem.minecart);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.minecartFurnace, 1), "A", "B", 'A', BaseBlock.FURNACE, 'B', BaseItem.minecart);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.boat, 1), "# #", "###", '#', BaseBlock.WOOD);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.bucket, 1), "# #", " # ", '#', BaseItem.ironIngot);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.flintAndSteel, 1), "A ", " B", 'A', BaseItem.ironIngot, 'B', BaseItem.flint);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.bread, 1), "###", '#', BaseItem.wheat);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.WOOD_STAIRS, 4), "#  ", "## ", "###", '#', BaseBlock.WOOD);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.fishingRod, 1), "  #", " #X", "# X", '#', BaseItem.stick, 'X', BaseItem.string);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.COBBLESTONE_STAIRS, 4), "#  ", "## ", "###", '#', BaseBlock.COBBLESTONE);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.painting, 1), "###", "#X#", "###", '#', BaseItem.stick, 'X', BaseBlock.WOOL);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.goldenApple, 1), "###", "#X#", "###", '#', BaseBlock.GOLD_BLOCK, 'X', BaseItem.apple);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.LEVER, 1), "X", "#", '#', BaseBlock.COBBLESTONE, 'X', BaseItem.stick);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.REDSTONE_TORCH_LIT, 1), "X", "#", '#', BaseItem.stick, 'X', BaseItem.redstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.redstoneRepeater, 1), "#X#", "III", '#', BaseBlock.REDSTONE_TORCH_LIT, 'X', BaseItem.redstoneDust, 'I', BaseBlock.STONE);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.clock, 1), " # ", "#X#", " # ", '#', BaseItem.goldIngot, 'X', BaseItem.redstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.compass, 1), " # ", "#X#", " # ", '#', BaseItem.ironIngot, 'X', BaseItem.redstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.map, 1), "###", "#X#", "###", '#', BaseItem.paper, 'X', BaseItem.compass);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.BUTTON, 1), "#", "#", '#', BaseBlock.STONE);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.WOODEN_PRESSURE_PLATE, 1), "##", '#', BaseBlock.STONE);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.STONE_PRESSURE_PLATE, 1), "##", '#', BaseBlock.WOOD);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.DISPENSER, 1), "###", "#X#", "#R#", '#', BaseBlock.COBBLESTONE, 'X', BaseItem.bow, 'R', BaseItem.redstoneDust);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.PISTON, 1), "TTT", "#X#", "#R#", '#', BaseBlock.COBBLESTONE, 'X', BaseItem.ironIngot, 'R', BaseItem.redstoneDust, 'T', BaseBlock.WOOD);
		accessor.callAddShapedRecipe(new ItemStack(BaseBlock.STICKY_PISTON, 1), "S", "P", 'S', BaseItem.slimeball, 'P', BaseBlock.PISTON);
		accessor.callAddShapedRecipe(new ItemStack(BaseItem.bed, 1), "###", "XXX", '#', BaseBlock.WOOL, 'X', BaseBlock.WOOD);
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
