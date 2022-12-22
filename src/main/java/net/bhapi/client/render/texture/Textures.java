package net.bhapi.client.render.texture;

import net.bhapi.BHAPI;
import net.bhapi.client.BHAPIClient;
import net.bhapi.mixin.client.TextureManagerAccessor;
import net.bhapi.util.BufferUtil;
import net.bhapi.util.Identifier;
import net.bhapi.util.ImageUtil;
import net.bhapi.util.ImageUtil.FormatConvert;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.TextureBinder;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Textures {
	private static final Map<Integer, Identifier> ID_PARTICLE = new HashMap<>();
	private static final Map<Integer, Identifier> ID_BLOCK = new HashMap<>();
	private static final Map<Integer, Identifier> ID_ITEM = new HashMap<>();
	private static final Map<Integer, TextureSample> VANILLA_BLOCKS = new HashMap<>();
	private static final Map<Integer, TextureSample> VANILLA_ITEMS = new HashMap<>();
	
	private static final Map<Byte, boolean[]> BREAKING_EXIST = new HashMap<>();
	private static final Map<Byte, int[]> BREAKING_SCALED = new HashMap<>();
	private static final BufferedImage[] BREAKING_CACHE = new BufferedImage[10];
	private static final int[] BREAKING = new int[10];
	private static TextureAtlas atlas;
	private static TextureSample empty;
	private static boolean building;
	
	public static final Map<Identifier, BufferedImage> LOADED_TEXTURES = new HashMap<>();
	
	public static void preInit() {
		building = true;
		BufferedImage terrain = loadTexture("/terrain.png");
		addTextures(terrain, ID_BLOCK);
		addTextures(loadTexture("/gui/items.png"), ID_ITEM);
		addTextures(loadTexture("/particles.png"), ID_PARTICLE);
		
		IntStream.range(0, 10).forEach(index -> {
			int width = terrain.getWidth() / 16;
			int height = terrain.getHeight() / 16;
			int x = ((240 + index) & 15) * width;
			int y = ((240 + index) / 16) * height;
			BREAKING_CACHE[index] = terrain.getSubimage(x, y, width, height);
			BREAKING[index] = BHAPIClient.getMinecraft().textureManager.bindImage(BREAKING_CACHE[index]);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, BREAKING[index]);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		});
		
		loadModsAssets();
	}
	
	public static void init() {
		BHAPI.log("Making texture atlas");
		
		atlas = new TextureAtlas(LOADED_TEXTURES);
		empty = atlas.getSample(Identifier.make("empty"));
		ID_BLOCK.forEach((index, id) -> VANILLA_BLOCKS.put(index, atlas.getSample(id)));
		ID_ITEM.forEach((index, id) -> VANILLA_ITEMS.put(index, atlas.getSample(id)));
		
		List<?> binders = ((TextureManagerAccessor) BHAPIClient.getMinecraft().textureManager).getTextureBinders();
		binders.forEach(obj -> {
			TextureBinder binder = (TextureBinder) obj;
			Identifier id = binder.renderMode == 0 ? ID_BLOCK.get(binder.index) : ID_ITEM.get(binder.index);
			binder.index = atlas.getTextureIndex(id);
		});
		
		building = false;
	}
	
	public static void preReload() {
		building = true;
		BufferedImage terrain = loadTexture("/terrain.png");
		addTextures(terrain, ID_BLOCK);
		addTextures(loadTexture("/gui/items.png"), ID_ITEM);
		addTextures(loadTexture("/particles.png"), ID_PARTICLE);
		
		IntStream.range(0, 10).forEach(index -> {
			int width = terrain.getWidth() / 16;
			int height = terrain.getHeight() / 16;
			int x = ((240 + index) & 15) * width;
			int y = ((240 + index) / 16) * height;
			BREAKING_CACHE[index] = terrain.getSubimage(x, y, width, height);
			BHAPIClient.getMinecraft().textureManager.bindImage(BREAKING_CACHE[index], BREAKING[index]);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, BREAKING[index]);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		});
		
		loadModsAssets();
	}
	
	public static void reload() {
		atlas.rebuild(LOADED_TEXTURES);
		BREAKING_EXIST.clear();
		building = false;
	}
	
	public static boolean isBuilding() {
		return building;
	}
	
	public static TextureAtlas getAtlas() {
		return atlas;
	}
	
	public static TextureSample getVanillaBlockSample(int texture) {
		if (texture < 0) texture = -texture;
		return VANILLA_BLOCKS.getOrDefault(texture, empty);
	}
	
	public static TextureSample getVanillaItemSample(int texture) {
		return VANILLA_ITEMS.getOrDefault(texture, empty);
	}
	
	public static int getBlockBreaking(int stage) {
		return BREAKING[stage];
	}
	
	public static int getBlockBreaking(int stage, int width, int height) {
		byte index = (byte) (width << 4 | height);
		int[] textures = BREAKING_SCALED.computeIfAbsent(index, i -> new int[10]);
		boolean[] exist = BREAKING_EXIST.computeIfAbsent(index, i -> new boolean[10]);
		if (textures[stage] == 0 || !exist[stage]) {
			BufferedImage cache = BREAKING_CACHE[stage];
			BufferedImage breaking = ImageUtil.makeImage(width * cache.getWidth(), height * cache.getHeight());
			Graphics g = breaking.getGraphics();
			for (int x = 0; x < breaking.getWidth(); x += cache.getWidth()) {
				for (int y = 0; y < breaking.getHeight(); y += cache.getHeight()) {
					g.drawImage(cache, x, y, null);
				}
			}
			if (textures[stage] > 0) {
				BHAPIClient.getMinecraft().textureManager.bindImage(breaking, textures[stage]);
			}
			else textures[stage] = BHAPIClient.getMinecraft().textureManager.bindImage(breaking);
			exist[stage] = true;
		}
		return textures[stage];
	}
	
	private static BufferedImage loadTexture(String name) {
		int id = BHAPIClient.getMinecraft().textureManager.getTextureId(name);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		IntBuffer buffer = BufferUtil.createIntBuffer(width * height);
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		
		BufferedImage image = ImageUtil.makeImage(width, height);
		int[] data = ImageUtil.getPixelData(image);
		buffer.rewind();
		buffer.get(data);
		ImageUtil.convertFormat(data, FormatConvert.ABGR_TO_ARGB);
		
		return image;
	}
	
	private static void addTextures(BufferedImage atlas, Map<Integer, Identifier> idMap) {
		int width = atlas.getWidth() >> 4;
		int height = atlas.getHeight() >> 4;
		
		idMap.forEach((index, id) -> {
			byte x = (byte) (index & 15);
			byte y = (byte) (index >> 4);
			
			if (idMap == ID_BLOCK) {
				// Liquids, using 2x2 tiles texture
				if (index == 238 || index == 206) {
					BufferedImage img = ImageUtil.makeImage(32, 32);
					Graphics g = img.getGraphics();
					g.setColor(new Color(128, 128, 128, index == 238 ? 255 : 128));
					g.fillRect(0, 0, 32, 32);
					LOADED_TEXTURES.put(id, img);
					return;
				}
				
				// Vanilla block animations (portal, fire, static liquids)
				if (index == 14 || index == 31 || index == 47 || index == 205 || index == 237) {
					BufferedImage img = ImageUtil.makeImage(16, 16);
					Graphics g = img.getGraphics();
					boolean isFire = index == 31 || index == 47;
					boolean isLava = index == 237;
					g.setColor(new Color(128, 128, 128, isFire ? 0 : isLava ? 255 : 128));
					g.fillRect(0, 0, 16, 16);
					LOADED_TEXTURES.put(id, img);
					return;
				}
			}
			
			BufferedImage img = ImageUtil.makeImage(width, height);
			img.getGraphics().drawImage(atlas, -x * width, -y * height, null);
			LOADED_TEXTURES.put(id, img);
		});
	}
	
	private static void loadModsAssets() {
		FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
			String id = modContainer.getMetadata().getId();
			LOADED_TEXTURES.putAll(ImageUtil.loadTexturesFromPathDir(Identifier.make(id, "block")));
			LOADED_TEXTURES.putAll(ImageUtil.loadTexturesFromPathDir(Identifier.make(id, "item")));
			LOADED_TEXTURES.putAll(ImageUtil.loadTexturesFromPathDir(Identifier.make(id, "particle")));
		});
	}
	
	static {
		ID_BLOCK.put(0, Identifier.make("block/grass_block_top"));
		ID_BLOCK.put(1, Identifier.make("block/stone"));
		ID_BLOCK.put(2, Identifier.make("block/dirt"));
		ID_BLOCK.put(3, Identifier.make("block/grass_block_side"));
		ID_BLOCK.put(4, Identifier.make("block/planks"));
		ID_BLOCK.put(5, Identifier.make("block/smooth_stone_slab_side"));
		ID_BLOCK.put(6, Identifier.make("block/smooth_stone"));
		ID_BLOCK.put(7, Identifier.make("block/bricks"));
		ID_BLOCK.put(8, Identifier.make("block/tnt_side"));
		ID_BLOCK.put(9, Identifier.make("block/tnt_top"));
		ID_BLOCK.put(10, Identifier.make("block/tnt_bottom"));
		ID_BLOCK.put(11, Identifier.make("block/cobweb"));
		ID_BLOCK.put(12, Identifier.make("block/rose"));
		ID_BLOCK.put(13, Identifier.make("block/dandelion"));
		ID_BLOCK.put(14, Identifier.make("block/nether_portal"));
		ID_BLOCK.put(15, Identifier.make("block/sapling_oak"));
		ID_BLOCK.put(16, Identifier.make("block/cobblestone"));
		ID_BLOCK.put(17, Identifier.make("block/bedrock"));
		ID_BLOCK.put(18, Identifier.make("block/sand"));
		ID_BLOCK.put(19, Identifier.make("block/gravel"));
		ID_BLOCK.put(20, Identifier.make("block/oak_log"));
		ID_BLOCK.put(21, Identifier.make("block/oak_log_top"));
		ID_BLOCK.put(22, Identifier.make("block/iron_block"));
		ID_BLOCK.put(23, Identifier.make("block/gold_block"));
		ID_BLOCK.put(24, Identifier.make("block/diamond_block"));
		ID_BLOCK.put(25, Identifier.make("block/chest_top"));
		ID_BLOCK.put(26, Identifier.make("block/chest_side"));
		ID_BLOCK.put(27, Identifier.make("block/chest_front"));
		ID_BLOCK.put(28, Identifier.make("block/red_mushroom"));
		ID_BLOCK.put(29, Identifier.make("block/brown_mushroom"));
		ID_BLOCK.put(31, Identifier.make("block/fire_0"));
		ID_BLOCK.put(32, Identifier.make("block/gold_ore"));
		ID_BLOCK.put(33, Identifier.make("block/iron_ore"));
		ID_BLOCK.put(34, Identifier.make("block/coal_ore"));
		ID_BLOCK.put(35, Identifier.make("block/bookshelf"));
		ID_BLOCK.put(36, Identifier.make("block/mossy_cobblestone"));
		ID_BLOCK.put(37, Identifier.make("block/obsidian"));
		ID_BLOCK.put(38, Identifier.make("block/grass_block_side_overlay"));
		ID_BLOCK.put(39, Identifier.make("block/tall_grass"));
		ID_BLOCK.put(40, Identifier.make("block/grass_block_top_snow"));
		ID_BLOCK.put(41, Identifier.make("block/double_chest_front_left"));
		ID_BLOCK.put(42, Identifier.make("block/double_chest_front_right"));
		ID_BLOCK.put(43, Identifier.make("block/crafting_table_top"));
		ID_BLOCK.put(44, Identifier.make("block/furnace_front"));
		ID_BLOCK.put(45, Identifier.make("block/furnace_side"));
		ID_BLOCK.put(46, Identifier.make("block/dispenser_front"));
		ID_BLOCK.put(47, Identifier.make("block/fire_1"));
		ID_BLOCK.put(48, Identifier.make("block/sponge"));
		ID_BLOCK.put(49, Identifier.make("block/glass"));
		ID_BLOCK.put(50, Identifier.make("block/diamond_ore"));
		ID_BLOCK.put(51, Identifier.make("block/redstone_ore"));
		ID_BLOCK.put(52, Identifier.make("block/oak_leaves"));
		ID_BLOCK.put(53, Identifier.make("block/oak_leaves_opaque"));
		ID_BLOCK.put(55, Identifier.make("block/dead_bush"));
		ID_BLOCK.put(56, Identifier.make("block/fern"));
		ID_BLOCK.put(57, Identifier.make("block/double_chest_side_left"));
		ID_BLOCK.put(58, Identifier.make("block/double_chest_side_right"));
		ID_BLOCK.put(59, Identifier.make("block/crafting_table_side"));
		ID_BLOCK.put(60, Identifier.make("block/crafting_table_front"));
		ID_BLOCK.put(61, Identifier.make("block/furnace_front_on"));
		ID_BLOCK.put(62, Identifier.make("block/furnace_top"));
		ID_BLOCK.put(63, Identifier.make("block/spruce_sapling"));
		ID_BLOCK.put(64, Identifier.make("block/white_wool"));
		ID_BLOCK.put(65, Identifier.make("block/spawner"));
		ID_BLOCK.put(66, Identifier.make("block/snow"));
		ID_BLOCK.put(67, Identifier.make("block/ice"));
		ID_BLOCK.put(68, Identifier.make("block/grass_block_snow"));
		ID_BLOCK.put(69, Identifier.make("block/cactus_top"));
		ID_BLOCK.put(70, Identifier.make("block/cactus_side"));
		ID_BLOCK.put(71, Identifier.make("block/cactus_bottom"));
		ID_BLOCK.put(72, Identifier.make("block/clay"));
		ID_BLOCK.put(73, Identifier.make("block/sugar_cane"));
		ID_BLOCK.put(74, Identifier.make("block/note_block"));
		ID_BLOCK.put(75, Identifier.make("block/jukebox_top"));
		ID_BLOCK.put(79, Identifier.make("block/birch_sapling"));
		ID_BLOCK.put(80, Identifier.make("block/torch"));
		ID_BLOCK.put(81, Identifier.make("block/wood_door_upper"));
		ID_BLOCK.put(82, Identifier.make("block/iron_door_upper"));
		ID_BLOCK.put(83, Identifier.make("block/ladder"));
		ID_BLOCK.put(84, Identifier.make("block/trapdoor"));
		ID_BLOCK.put(86, Identifier.make("block/farmland_wet"));
		ID_BLOCK.put(87, Identifier.make("block/farmland_dry"));
		ID_BLOCK.put(88, Identifier.make("block/wheat_stage_0"));
		ID_BLOCK.put(89, Identifier.make("block/wheat_stage_1"));
		ID_BLOCK.put(90, Identifier.make("block/wheat_stage_2"));
		ID_BLOCK.put(91, Identifier.make("block/wheat_stage_3"));
		ID_BLOCK.put(92, Identifier.make("block/wheat_stage_4"));
		ID_BLOCK.put(93, Identifier.make("block/wheat_stage_5"));
		ID_BLOCK.put(94, Identifier.make("block/wheat_stage_6"));
		ID_BLOCK.put(95, Identifier.make("block/wheat_stage_7"));
		ID_BLOCK.put(96, Identifier.make("block/lever"));
		ID_BLOCK.put(97, Identifier.make("block/wood_door_lower"));
		ID_BLOCK.put(98, Identifier.make("block/iron_door_lower"));
		ID_BLOCK.put(99, Identifier.make("block/redstone_torch_on"));
		ID_BLOCK.put(102, Identifier.make("block/pumpkin_top"));
		ID_BLOCK.put(103, Identifier.make("block/netherrack"));
		ID_BLOCK.put(104, Identifier.make("block/soul_sand"));
		ID_BLOCK.put(105, Identifier.make("block/glowstone"));
		ID_BLOCK.put(106, Identifier.make("block/piston_top_sticky"));
		ID_BLOCK.put(107, Identifier.make("block/piston_top_normal"));
		ID_BLOCK.put(108, Identifier.make("block/piston_side"));
		ID_BLOCK.put(109, Identifier.make("block/piston_top"));
		ID_BLOCK.put(110, Identifier.make("block/piston_inner"));
		ID_BLOCK.put(112, Identifier.make("block/rail_corner"));
		ID_BLOCK.put(113, Identifier.make("block/black_wool"));
		ID_BLOCK.put(114, Identifier.make("block/gray_wool"));
		ID_BLOCK.put(115, Identifier.make("block/redstone_torch_off"));
		ID_BLOCK.put(116, Identifier.make("block/spruce_log"));
		ID_BLOCK.put(117, Identifier.make("block/birch_log"));
		ID_BLOCK.put(118, Identifier.make("block/pumpkin_side"));
		ID_BLOCK.put(119, Identifier.make("block/carved_pumpkin"));
		ID_BLOCK.put(120, Identifier.make("block/jack_o_lantern"));
		ID_BLOCK.put(121, Identifier.make("block/cake_top"));
		ID_BLOCK.put(122, Identifier.make("block/cake_side"));
		ID_BLOCK.put(123, Identifier.make("block/cake_inner"));
		ID_BLOCK.put(124, Identifier.make("block/cake_bottom"));
		ID_BLOCK.put(128, Identifier.make("block/rail"));
		ID_BLOCK.put(129, Identifier.make("block/red_wool"));
		ID_BLOCK.put(130, Identifier.make("block/pink_wool"));
		ID_BLOCK.put(131, Identifier.make("block/redstone_repeater_off"));
		ID_BLOCK.put(132, Identifier.make("block/spruce_leaves"));
		ID_BLOCK.put(133, Identifier.make("block/spruce_leaves_opaque"));
		ID_BLOCK.put(134, Identifier.make("block/bed_feet_top"));
		ID_BLOCK.put(135, Identifier.make("block/bed_head_top"));
		ID_BLOCK.put(140, Identifier.make("item/cake"));
		ID_BLOCK.put(144, Identifier.make("block/lapis_block"));
		ID_BLOCK.put(145, Identifier.make("block/green_wool"));
		ID_BLOCK.put(146, Identifier.make("block/lime_wool"));
		ID_BLOCK.put(147, Identifier.make("block/redstone_repeater_on"));
		ID_BLOCK.put(149, Identifier.make("block/bed_feet_end"));
		ID_BLOCK.put(150, Identifier.make("block/bed_feet_side"));
		ID_BLOCK.put(151, Identifier.make("block/bed_head_side"));
		ID_BLOCK.put(152, Identifier.make("block/bed_head_end"));
		ID_BLOCK.put(160, Identifier.make("block/lapis_ore"));
		ID_BLOCK.put(161, Identifier.make("block/brown_wool"));
		ID_BLOCK.put(162, Identifier.make("block/yellow_wool"));
		ID_BLOCK.put(163, Identifier.make("block/powered_rail"));
		ID_BLOCK.put(164, Identifier.make("block/redstone_dust_dot"));
		ID_BLOCK.put(165, Identifier.make("block/redstone_dust_overlay"));
		ID_BLOCK.put(176, Identifier.make("block/sandstone_top"));
		ID_BLOCK.put(177, Identifier.make("block/blue_wool"));
		ID_BLOCK.put(178, Identifier.make("block/light_blue_wool"));
		ID_BLOCK.put(179, Identifier.make("block/powered_rail_on"));
		ID_BLOCK.put(192, Identifier.make("block/sandstone_side"));
		ID_BLOCK.put(193, Identifier.make("block/purple_wool"));
		ID_BLOCK.put(194, Identifier.make("block/magenta_wool"));
		ID_BLOCK.put(195, Identifier.make("block/detector_rail"));
		ID_BLOCK.put(205, Identifier.make("block/water_still"));
		ID_BLOCK.put(206, Identifier.make("block/water_flow"));
		ID_BLOCK.put(208, Identifier.make("block/sandstone_bottom"));
		ID_BLOCK.put(209, Identifier.make("block/cyan_wool"));
		ID_BLOCK.put(210, Identifier.make("block/orange_wool"));
		ID_BLOCK.put(225, Identifier.make("block/light_gray_wool"));
		ID_BLOCK.put(237, Identifier.make("block/lava_still"));
		ID_BLOCK.put(238, Identifier.make("block/lava_flow"));
		
		ID_ITEM.put(0, Identifier.make("item/leather_helmet"));
		ID_ITEM.put(1, Identifier.make("item/chainmail_helmet"));
		ID_ITEM.put(2, Identifier.make("item/iron_helmet"));
		ID_ITEM.put(3, Identifier.make("item/diamond_helmet"));
		ID_ITEM.put(4, Identifier.make("item/golden_helmet"));
		ID_ITEM.put(5, Identifier.make("item/flint_and_steel"));
		ID_ITEM.put(6, Identifier.make("item/flint"));
		ID_ITEM.put(7, Identifier.make("item/coal"));
		ID_ITEM.put(8, Identifier.make("item/string"));
		ID_ITEM.put(9, Identifier.make("item/wheat_seeds"));
		ID_ITEM.put(10, Identifier.make("item/apple"));
		ID_ITEM.put(11, Identifier.make("item/golden_apple"));
		ID_ITEM.put(12, Identifier.make("item/egg"));
		ID_ITEM.put(13, Identifier.make("item/sugar"));
		ID_ITEM.put(14, Identifier.make("item/snowball"));
		ID_ITEM.put(15, Identifier.make("item/empty_armor_slot_helmet"));
		ID_ITEM.put(16, Identifier.make("item/leather_chestplate"));
		ID_ITEM.put(17, Identifier.make("item/chainmail_chestplate"));
		ID_ITEM.put(18, Identifier.make("item/iron_chestplate"));
		ID_ITEM.put(19, Identifier.make("item/diamond_chestplate"));
		ID_ITEM.put(20, Identifier.make("item/golden_chestplate"));
		ID_ITEM.put(21, Identifier.make("item/bow"));
		ID_ITEM.put(22, Identifier.make("item/brick"));
		ID_ITEM.put(23, Identifier.make("item/iron_ingot"));
		ID_ITEM.put(24, Identifier.make("item/feather"));
		ID_ITEM.put(25, Identifier.make("item/wheat"));
		ID_ITEM.put(26, Identifier.make("item/painting"));
		ID_ITEM.put(27, Identifier.make("item/sugar_cane"));
		ID_ITEM.put(28, Identifier.make("item/bone"));
		ID_ITEM.put(29, Identifier.make("item/cake"));
		ID_ITEM.put(30, Identifier.make("item/slime_ball"));
		ID_ITEM.put(31, Identifier.make("item/empty_armor_slot_chestplate"));
		ID_ITEM.put(32, Identifier.make("item/leather_leggings"));
		ID_ITEM.put(33, Identifier.make("item/chainmail_leggings"));
		ID_ITEM.put(34, Identifier.make("item/iron_leggings"));
		ID_ITEM.put(35, Identifier.make("item/diamond_leggings"));
		ID_ITEM.put(36, Identifier.make("item/golden_leggings"));
		ID_ITEM.put(37, Identifier.make("item/arrow"));
		ID_ITEM.put(38, Identifier.make("item/quiver"));
		ID_ITEM.put(39, Identifier.make("item/gold_ingot"));
		ID_ITEM.put(40, Identifier.make("item/gunpowder"));
		ID_ITEM.put(41, Identifier.make("item/bread"));
		ID_ITEM.put(42, Identifier.make("item/sign"));
		ID_ITEM.put(43, Identifier.make("item/wood_door"));
		ID_ITEM.put(44, Identifier.make("item/iron_door"));
		ID_ITEM.put(45, Identifier.make("item/bed"));
		ID_ITEM.put(47, Identifier.make("item/empty_armor_slot_leggings"));
		ID_ITEM.put(48, Identifier.make("item/leather_boots"));
		ID_ITEM.put(49, Identifier.make("item/chainmail_boots"));
		ID_ITEM.put(50, Identifier.make("item/iron_boots"));
		ID_ITEM.put(51, Identifier.make("item/diamond_boots"));
		ID_ITEM.put(52, Identifier.make("item/golden_boots"));
		ID_ITEM.put(53, Identifier.make("item/stick"));
		ID_ITEM.put(54, Identifier.make("item/compass"));
		ID_ITEM.put(55, Identifier.make("item/diamond"));
		ID_ITEM.put(56, Identifier.make("item/redstone_dust"));
		ID_ITEM.put(57, Identifier.make("item/clay_ball"));
		ID_ITEM.put(58, Identifier.make("item/paper"));
		ID_ITEM.put(59, Identifier.make("item/book"));
		ID_ITEM.put(60, Identifier.make("item/map"));
		ID_ITEM.put(63, Identifier.make("item/empty_armor_slot_boots"));
		ID_ITEM.put(64, Identifier.make("item/wooden_sword"));
		ID_ITEM.put(65, Identifier.make("item/stone_sword"));
		ID_ITEM.put(66, Identifier.make("item/iron_sword"));
		ID_ITEM.put(67, Identifier.make("item/diamond_sword"));
		ID_ITEM.put(68, Identifier.make("item/golden_sword"));
		ID_ITEM.put(69, Identifier.make("item/fishing_rod"));
		ID_ITEM.put(70, Identifier.make("item/clock"));
		ID_ITEM.put(71, Identifier.make("item/bowl"));
		ID_ITEM.put(72, Identifier.make("item/mushroom_stew"));
		ID_ITEM.put(73, Identifier.make("item/glowstone_dust"));
		ID_ITEM.put(74, Identifier.make("item/bucket"));
		ID_ITEM.put(75, Identifier.make("item/water_bucket"));
		ID_ITEM.put(76, Identifier.make("item/lava_bucket"));
		ID_ITEM.put(77, Identifier.make("item/milk_bucket"));
		ID_ITEM.put(78, Identifier.make("item/ink_sac"));
		ID_ITEM.put(79, Identifier.make("item/gray_dye"));
		ID_ITEM.put(80, Identifier.make("item/wooden_shovel"));
		ID_ITEM.put(81, Identifier.make("item/stone_shovel"));
		ID_ITEM.put(82, Identifier.make("item/iron_shovel"));
		ID_ITEM.put(83, Identifier.make("item/diamond_shovel"));
		ID_ITEM.put(84, Identifier.make("item/golden_shovel"));
		ID_ITEM.put(85, Identifier.make("item/fishing_rod_cast"));
		ID_ITEM.put(86, Identifier.make("item/repeater"));
		ID_ITEM.put(87, Identifier.make("item/porkchop"));
		ID_ITEM.put(88, Identifier.make("item/cooked_porkchop"));
		ID_ITEM.put(89, Identifier.make("item/fish"));
		ID_ITEM.put(90, Identifier.make("item/cooked_fish"));
		ID_ITEM.put(92, Identifier.make("item/cookie"));
		ID_ITEM.put(93, Identifier.make("item/shears"));
		ID_ITEM.put(94, Identifier.make("item/red_dye"));
		ID_ITEM.put(95, Identifier.make("item/pink_dye"));
		ID_ITEM.put(96, Identifier.make("item/wooden_pickaxe"));
		ID_ITEM.put(97, Identifier.make("item/stone_pickaxe"));
		ID_ITEM.put(98, Identifier.make("item/iron_pickaxe"));
		ID_ITEM.put(99, Identifier.make("item/diamond_pickaxe"));
		ID_ITEM.put(100, Identifier.make("item/golden_pickaxe"));
		ID_ITEM.put(103, Identifier.make("item/leather"));
		ID_ITEM.put(104, Identifier.make("item/saddle"));
		ID_ITEM.put(110, Identifier.make("item/green_dye"));
		ID_ITEM.put(111, Identifier.make("item/lime_dye"));
		ID_ITEM.put(112, Identifier.make("item/wooden_axe"));
		ID_ITEM.put(113, Identifier.make("item/stone_axe"));
		ID_ITEM.put(114, Identifier.make("item/iron_axe"));
		ID_ITEM.put(115, Identifier.make("item/diamond_axe"));
		ID_ITEM.put(116, Identifier.make("item/golden_axe"));
		ID_ITEM.put(126, Identifier.make("item/brown_dye"));
		ID_ITEM.put(127, Identifier.make("item/ellow_dye"));
		ID_ITEM.put(128, Identifier.make("item/wooden_hoe"));
		ID_ITEM.put(129, Identifier.make("item/stone_hoe"));
		ID_ITEM.put(130, Identifier.make("item/iron_hoe"));
		ID_ITEM.put(131, Identifier.make("item/diamond_hoe"));
		ID_ITEM.put(132, Identifier.make("item/golden_hoe"));
		ID_ITEM.put(135, Identifier.make("item/minecart"));
		ID_ITEM.put(136, Identifier.make("item/boat"));
		ID_ITEM.put(142, Identifier.make("item/lapis_lazuli"));
		ID_ITEM.put(143, Identifier.make("item/light_blue_dye"));
		ID_ITEM.put(151, Identifier.make("item/chest_minecart"));
		ID_ITEM.put(158, Identifier.make("item/purple_dye"));
		ID_ITEM.put(159, Identifier.make("item/magenta_dye"));
		ID_ITEM.put(167, Identifier.make("item/furnace_minecart"));
		ID_ITEM.put(174, Identifier.make("item/cyan_dye"));
		ID_ITEM.put(175, Identifier.make("item/orange_dye"));
		ID_ITEM.put(190, Identifier.make("item/light_gray_dye"));
		ID_ITEM.put(191, Identifier.make("item/bone_meal"));
		ID_ITEM.put(240, Identifier.make("item/music_disc_13"));
		ID_ITEM.put(241, Identifier.make("item/music_disc_cat"));
		
		ID_PARTICLE.put(0, Identifier.make("particle/generic_0"));
		ID_PARTICLE.put(1, Identifier.make("particle/generic_1"));
		ID_PARTICLE.put(2, Identifier.make("particle/generic_2"));
		ID_PARTICLE.put(3, Identifier.make("particle/generic_3"));
		ID_PARTICLE.put(4, Identifier.make("particle/generic_4"));
		ID_PARTICLE.put(5, Identifier.make("particle/generic_5"));
		ID_PARTICLE.put(6, Identifier.make("particle/generic_6"));
		ID_PARTICLE.put(7, Identifier.make("particle/generic_7"));
		ID_PARTICLE.put(16, Identifier.make("particle_16"));
		ID_PARTICLE.put(17, Identifier.make("particle_17"));
		ID_PARTICLE.put(19, Identifier.make("particle/splash_0"));
		ID_PARTICLE.put(20, Identifier.make("particle/splash_1"));
		ID_PARTICLE.put(21, Identifier.make("particle/splash_2"));
		ID_PARTICLE.put(22, Identifier.make("particle/splash_3"));
		ID_PARTICLE.put(32, Identifier.make("particle/bubble"));
		ID_PARTICLE.put(33, Identifier.make("particle/fishing_hook"));
		ID_PARTICLE.put(48, Identifier.make("particle/flame"));
		ID_PARTICLE.put(49, Identifier.make("particle/lava"));
		ID_PARTICLE.put(64, Identifier.make("particle/note"));
		ID_PARTICLE.put(80, Identifier.make("particle/heart"));
		ID_PARTICLE.put(96, Identifier.make("particle_96"));
		ID_PARTICLE.put(97, Identifier.make("particle_97"));
	}
}
