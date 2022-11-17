package net.bhapi.client.render.texture;

import net.bhapi.BHAPI;
import net.bhapi.client.BHAPIClient;
import net.bhapi.mixin.client.BaseItemAccessor;
import net.bhapi.mixin.client.TextureManagerAccessor;
import net.bhapi.util.BufferUtil;
import net.bhapi.util.Identifier;
import net.bhapi.util.ImageUtil;
import net.bhapi.util.ImageUtil.FormatConvert;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.item.BaseItem;
import org.lwjgl.opengl.GL11;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class Textures {
	private static final Map<Integer, Identifier> ID_PARTICLE = new HashMap<>();
	private static final Map<Integer, Identifier> ID_TERRAIN = new HashMap<>();
	private static final Map<Integer, Identifier> ID_ITEM = new HashMap<>();
	
	private static final TextureSample[] VANILLA_BLOCKS = new TextureSample[256];
	private static final Map<Byte, boolean[]> BREAKING_EXIST = new HashMap<>();
	private static final Map<Byte, int[]> BREAKING_SCALED = new HashMap<>();
	private static final BufferedImage[] BREAKING_CACHE = new BufferedImage[10];
	private static final int[] BREAKING = new int[10];
	private static TextureAtlas atlas;
	private static TextureSample empty;
	private static boolean building;
	
	public static final Map<Identifier, BufferedImage> LOADED_TEXTURES = new HashMap<>();
	
	public static void init() {
		building = true;
		BHAPI.log("Making texture atlas");
		
		BufferedImage terrain = loadTexture("/terrain.png");
		addTextures(terrain, ID_TERRAIN, LOADED_TEXTURES);
		addTextures(loadTexture("/gui/items.png"), ID_ITEM, LOADED_TEXTURES);
		addTextures(loadTexture("/particles.png"), ID_PARTICLE, LOADED_TEXTURES);
		
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
		
		atlas = new TextureAtlas(LOADED_TEXTURES);
		empty = atlas.getSample(Identifier.make("empty"));
		
		IntStream.range(0, 255).forEach(index -> {
			Identifier id = Identifier.make("terrain_" + index);
			VANILLA_BLOCKS[index] = atlas.getSample(id, true);
		});
		
		List<?> binders = ((TextureManagerAccessor) BHAPIClient.getMinecraft().textureManager).getTextureBinders();
		binders.forEach(obj -> {
			TextureBinder binder = (TextureBinder) obj;
			Identifier id = Identifier.make((binder.renderMode == 0 ? "terrain_" : "item_") + binder.index);
			binder.index = atlas.getTextureIndex(id);
		});
		
		Arrays.stream(BaseItem.byId, 0, 2002).filter(Objects::nonNull).forEach(item -> {
			BaseItemAccessor accessor = (BaseItemAccessor) item;
			int texture = accessor.bhapi_getTexturePosition();
			Identifier id = Identifier.make((item.id < 256 ? "block_" : "item_") + texture);
			texture = atlas.getTextureIndex(id, true);
			if (texture != -1) {
				accessor.bhapi_setTexturePosition(texture);
			}
		});
		building = false;
	}
	
	public static void reload() {
		building = true;
		BufferedImage terrain = loadTexture("/terrain.png");
		addTextures(terrain, ID_TERRAIN, LOADED_TEXTURES);
		addTextures(loadTexture("/gui/items.png"), ID_ITEM, LOADED_TEXTURES);
		addTextures(loadTexture("/particles.png"), ID_PARTICLE, LOADED_TEXTURES);
		
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
		if (texture < 0 || texture > 255) return empty;
		return VANILLA_BLOCKS[texture];
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
	
	private static void addTextures(BufferedImage atlas, Map<Integer, Identifier> idMap, Map<Identifier, BufferedImage> textures) {
		int width = atlas.getWidth() >> 4;
		int height = atlas.getHeight() >> 4;
		
		idMap.forEach((index, id) -> {
			byte x = (byte) (index & 15);
			byte y = (byte) (index >> 4);
			BufferedImage img = ImageUtil.makeImage(width, height);
			img.getGraphics().drawImage(atlas, -x * width, -y * height, null);
			textures.put(id, img);
		});
	}
	
	static {
		ID_TERRAIN.put(0, Identifier.make("terrain_0"));
		ID_TERRAIN.put(1, Identifier.make("terrain_1"));
		ID_TERRAIN.put(2, Identifier.make("terrain_2"));
		ID_TERRAIN.put(3, Identifier.make("terrain_3"));
		ID_TERRAIN.put(4, Identifier.make("terrain_4"));
		ID_TERRAIN.put(5, Identifier.make("terrain_5"));
		ID_TERRAIN.put(6, Identifier.make("terrain_6"));
		ID_TERRAIN.put(7, Identifier.make("terrain_7"));
		ID_TERRAIN.put(8, Identifier.make("terrain_8"));
		ID_TERRAIN.put(9, Identifier.make("terrain_9"));
		ID_TERRAIN.put(10, Identifier.make("terrain_10"));
		ID_TERRAIN.put(11, Identifier.make("terrain_11"));
		ID_TERRAIN.put(12, Identifier.make("terrain_12"));
		ID_TERRAIN.put(13, Identifier.make("terrain_13"));
		ID_TERRAIN.put(14, Identifier.make("terrain_14"));
		ID_TERRAIN.put(15, Identifier.make("terrain_15"));
		ID_TERRAIN.put(16, Identifier.make("terrain_16"));
		ID_TERRAIN.put(17, Identifier.make("terrain_17"));
		ID_TERRAIN.put(18, Identifier.make("terrain_18"));
		ID_TERRAIN.put(19, Identifier.make("terrain_19"));
		ID_TERRAIN.put(20, Identifier.make("terrain_20"));
		ID_TERRAIN.put(21, Identifier.make("terrain_21"));
		ID_TERRAIN.put(22, Identifier.make("terrain_22"));
		ID_TERRAIN.put(23, Identifier.make("terrain_23"));
		ID_TERRAIN.put(24, Identifier.make("terrain_24"));
		ID_TERRAIN.put(25, Identifier.make("terrain_25"));
		ID_TERRAIN.put(26, Identifier.make("terrain_26"));
		ID_TERRAIN.put(27, Identifier.make("terrain_27"));
		ID_TERRAIN.put(28, Identifier.make("terrain_28"));
		ID_TERRAIN.put(29, Identifier.make("terrain_29"));
		ID_TERRAIN.put(31, Identifier.make("terrain_31"));
		ID_TERRAIN.put(32, Identifier.make("terrain_32"));
		ID_TERRAIN.put(33, Identifier.make("terrain_33"));
		ID_TERRAIN.put(34, Identifier.make("terrain_34"));
		ID_TERRAIN.put(35, Identifier.make("terrain_35"));
		ID_TERRAIN.put(36, Identifier.make("terrain_36"));
		ID_TERRAIN.put(37, Identifier.make("terrain_37"));
		ID_TERRAIN.put(38, Identifier.make("terrain_38"));
		ID_TERRAIN.put(39, Identifier.make("terrain_39"));
		ID_TERRAIN.put(40, Identifier.make("terrain_40"));
		ID_TERRAIN.put(41, Identifier.make("terrain_41"));
		ID_TERRAIN.put(42, Identifier.make("terrain_42"));
		ID_TERRAIN.put(43, Identifier.make("terrain_43"));
		ID_TERRAIN.put(44, Identifier.make("terrain_44"));
		ID_TERRAIN.put(45, Identifier.make("terrain_45"));
		ID_TERRAIN.put(46, Identifier.make("terrain_46"));
		ID_TERRAIN.put(47, Identifier.make("terrain_47"));
		ID_TERRAIN.put(48, Identifier.make("terrain_48"));
		ID_TERRAIN.put(49, Identifier.make("terrain_49"));
		ID_TERRAIN.put(50, Identifier.make("terrain_50"));
		ID_TERRAIN.put(51, Identifier.make("terrain_51"));
		ID_TERRAIN.put(52, Identifier.make("terrain_52"));
		ID_TERRAIN.put(53, Identifier.make("terrain_53"));
		ID_TERRAIN.put(55, Identifier.make("terrain_55"));
		ID_TERRAIN.put(56, Identifier.make("terrain_56"));
		ID_TERRAIN.put(57, Identifier.make("terrain_57"));
		ID_TERRAIN.put(58, Identifier.make("terrain_58"));
		ID_TERRAIN.put(59, Identifier.make("terrain_59"));
		ID_TERRAIN.put(60, Identifier.make("terrain_60"));
		ID_TERRAIN.put(61, Identifier.make("terrain_61"));
		ID_TERRAIN.put(62, Identifier.make("terrain_62"));
		ID_TERRAIN.put(63, Identifier.make("terrain_63"));
		ID_TERRAIN.put(64, Identifier.make("terrain_64"));
		ID_TERRAIN.put(65, Identifier.make("terrain_65"));
		ID_TERRAIN.put(66, Identifier.make("terrain_66"));
		ID_TERRAIN.put(67, Identifier.make("terrain_67"));
		ID_TERRAIN.put(68, Identifier.make("terrain_68"));
		ID_TERRAIN.put(69, Identifier.make("terrain_69"));
		ID_TERRAIN.put(70, Identifier.make("terrain_70"));
		ID_TERRAIN.put(71, Identifier.make("terrain_71"));
		ID_TERRAIN.put(72, Identifier.make("terrain_72"));
		ID_TERRAIN.put(73, Identifier.make("terrain_73"));
		ID_TERRAIN.put(74, Identifier.make("terrain_74"));
		ID_TERRAIN.put(75, Identifier.make("terrain_75"));
		ID_TERRAIN.put(79, Identifier.make("terrain_79"));
		ID_TERRAIN.put(80, Identifier.make("terrain_80"));
		ID_TERRAIN.put(81, Identifier.make("terrain_81"));
		ID_TERRAIN.put(82, Identifier.make("terrain_82"));
		ID_TERRAIN.put(83, Identifier.make("terrain_83"));
		ID_TERRAIN.put(84, Identifier.make("terrain_84"));
		ID_TERRAIN.put(86, Identifier.make("terrain_86"));
		ID_TERRAIN.put(87, Identifier.make("terrain_87"));
		ID_TERRAIN.put(88, Identifier.make("terrain_88"));
		ID_TERRAIN.put(89, Identifier.make("terrain_89"));
		ID_TERRAIN.put(90, Identifier.make("terrain_90"));
		ID_TERRAIN.put(91, Identifier.make("terrain_91"));
		ID_TERRAIN.put(92, Identifier.make("terrain_92"));
		ID_TERRAIN.put(93, Identifier.make("terrain_93"));
		ID_TERRAIN.put(94, Identifier.make("terrain_94"));
		ID_TERRAIN.put(95, Identifier.make("terrain_95"));
		ID_TERRAIN.put(96, Identifier.make("terrain_96"));
		ID_TERRAIN.put(97, Identifier.make("terrain_97"));
		ID_TERRAIN.put(98, Identifier.make("terrain_98"));
		ID_TERRAIN.put(99, Identifier.make("terrain_99"));
		ID_TERRAIN.put(102, Identifier.make("terrain_102"));
		ID_TERRAIN.put(103, Identifier.make("terrain_103"));
		ID_TERRAIN.put(104, Identifier.make("terrain_104"));
		ID_TERRAIN.put(105, Identifier.make("terrain_105"));
		ID_TERRAIN.put(106, Identifier.make("terrain_106"));
		ID_TERRAIN.put(107, Identifier.make("terrain_107"));
		ID_TERRAIN.put(108, Identifier.make("terrain_108"));
		ID_TERRAIN.put(109, Identifier.make("terrain_109"));
		ID_TERRAIN.put(110, Identifier.make("terrain_110"));
		ID_TERRAIN.put(112, Identifier.make("terrain_112"));
		ID_TERRAIN.put(113, Identifier.make("terrain_113"));
		ID_TERRAIN.put(114, Identifier.make("terrain_114"));
		ID_TERRAIN.put(115, Identifier.make("terrain_115"));
		ID_TERRAIN.put(116, Identifier.make("terrain_116"));
		ID_TERRAIN.put(117, Identifier.make("terrain_117"));
		ID_TERRAIN.put(118, Identifier.make("terrain_118"));
		ID_TERRAIN.put(119, Identifier.make("terrain_119"));
		ID_TERRAIN.put(120, Identifier.make("terrain_120"));
		ID_TERRAIN.put(121, Identifier.make("terrain_121"));
		ID_TERRAIN.put(122, Identifier.make("terrain_122"));
		ID_TERRAIN.put(123, Identifier.make("terrain_123"));
		ID_TERRAIN.put(124, Identifier.make("terrain_124"));
		ID_TERRAIN.put(128, Identifier.make("terrain_128"));
		ID_TERRAIN.put(129, Identifier.make("terrain_129"));
		ID_TERRAIN.put(130, Identifier.make("terrain_130"));
		ID_TERRAIN.put(131, Identifier.make("terrain_131"));
		ID_TERRAIN.put(132, Identifier.make("terrain_132"));
		ID_TERRAIN.put(133, Identifier.make("terrain_133"));
		ID_TERRAIN.put(134, Identifier.make("terrain_134"));
		ID_TERRAIN.put(135, Identifier.make("terrain_135"));
		ID_TERRAIN.put(140, Identifier.make("terrain_140"));
		ID_TERRAIN.put(144, Identifier.make("terrain_144"));
		ID_TERRAIN.put(145, Identifier.make("terrain_145"));
		ID_TERRAIN.put(146, Identifier.make("terrain_146"));
		ID_TERRAIN.put(147, Identifier.make("terrain_147"));
		ID_TERRAIN.put(149, Identifier.make("terrain_149"));
		ID_TERRAIN.put(150, Identifier.make("terrain_150"));
		ID_TERRAIN.put(151, Identifier.make("terrain_151"));
		ID_TERRAIN.put(152, Identifier.make("terrain_152"));
		ID_TERRAIN.put(160, Identifier.make("terrain_160"));
		ID_TERRAIN.put(161, Identifier.make("terrain_161"));
		ID_TERRAIN.put(162, Identifier.make("terrain_162"));
		ID_TERRAIN.put(163, Identifier.make("terrain_163"));
		ID_TERRAIN.put(164, Identifier.make("terrain_164"));
		ID_TERRAIN.put(165, Identifier.make("terrain_165"));
		ID_TERRAIN.put(176, Identifier.make("terrain_176"));
		ID_TERRAIN.put(177, Identifier.make("terrain_177"));
		ID_TERRAIN.put(178, Identifier.make("terrain_178"));
		ID_TERRAIN.put(179, Identifier.make("terrain_179"));
		ID_TERRAIN.put(192, Identifier.make("terrain_192"));
		ID_TERRAIN.put(193, Identifier.make("terrain_193"));
		ID_TERRAIN.put(194, Identifier.make("terrain_194"));
		ID_TERRAIN.put(195, Identifier.make("terrain_195"));
		ID_TERRAIN.put(205, Identifier.make("terrain_205"));
		ID_TERRAIN.put(206, Identifier.make("terrain_206"));
		ID_TERRAIN.put(207, Identifier.make("terrain_207"));
		ID_TERRAIN.put(208, Identifier.make("terrain_208"));
		ID_TERRAIN.put(209, Identifier.make("terrain_209"));
		ID_TERRAIN.put(210, Identifier.make("terrain_210"));
		ID_TERRAIN.put(222, Identifier.make("terrain_222"));
		ID_TERRAIN.put(223, Identifier.make("terrain_223"));
		ID_TERRAIN.put(225, Identifier.make("terrain_225"));
		ID_TERRAIN.put(237, Identifier.make("terrain_237"));
		ID_TERRAIN.put(238, Identifier.make("terrain_238"));
		ID_TERRAIN.put(239, Identifier.make("terrain_239"));
		ID_TERRAIN.put(240, Identifier.make("terrain_240"));
		ID_TERRAIN.put(241, Identifier.make("terrain_241"));
		ID_TERRAIN.put(242, Identifier.make("terrain_242"));
		ID_TERRAIN.put(243, Identifier.make("terrain_243"));
		ID_TERRAIN.put(244, Identifier.make("terrain_244"));
		ID_TERRAIN.put(245, Identifier.make("terrain_245"));
		ID_TERRAIN.put(246, Identifier.make("terrain_246"));
		ID_TERRAIN.put(247, Identifier.make("terrain_247"));
		ID_TERRAIN.put(248, Identifier.make("terrain_248"));
		ID_TERRAIN.put(249, Identifier.make("terrain_249"));
		ID_TERRAIN.put(254, Identifier.make("terrain_254"));
		ID_TERRAIN.put(255, Identifier.make("terrain_255"));
		
		ID_ITEM.put(0, Identifier.make("item_0"));
		ID_ITEM.put(1, Identifier.make("item_1"));
		ID_ITEM.put(2, Identifier.make("item_2"));
		ID_ITEM.put(3, Identifier.make("item_3"));
		ID_ITEM.put(4, Identifier.make("item_4"));
		ID_ITEM.put(5, Identifier.make("item_5"));
		ID_ITEM.put(6, Identifier.make("item_6"));
		ID_ITEM.put(7, Identifier.make("item_7"));
		ID_ITEM.put(8, Identifier.make("item_8"));
		ID_ITEM.put(9, Identifier.make("item_9"));
		ID_ITEM.put(10, Identifier.make("item_10"));
		ID_ITEM.put(11, Identifier.make("item_11"));
		ID_ITEM.put(12, Identifier.make("item_12"));
		ID_ITEM.put(13, Identifier.make("item_13"));
		ID_ITEM.put(14, Identifier.make("item_14"));
		ID_ITEM.put(15, Identifier.make("item_15"));
		ID_ITEM.put(16, Identifier.make("item_16"));
		ID_ITEM.put(17, Identifier.make("item_17"));
		ID_ITEM.put(18, Identifier.make("item_18"));
		ID_ITEM.put(19, Identifier.make("item_19"));
		ID_ITEM.put(20, Identifier.make("item_20"));
		ID_ITEM.put(21, Identifier.make("item_21"));
		ID_ITEM.put(22, Identifier.make("item_22"));
		ID_ITEM.put(23, Identifier.make("item_23"));
		ID_ITEM.put(24, Identifier.make("item_24"));
		ID_ITEM.put(25, Identifier.make("item_25"));
		ID_ITEM.put(26, Identifier.make("item_26"));
		ID_ITEM.put(27, Identifier.make("item_27"));
		ID_ITEM.put(28, Identifier.make("item_28"));
		ID_ITEM.put(29, Identifier.make("item_29"));
		ID_ITEM.put(30, Identifier.make("item_30"));
		ID_ITEM.put(31, Identifier.make("item_31"));
		ID_ITEM.put(32, Identifier.make("item_32"));
		ID_ITEM.put(33, Identifier.make("item_33"));
		ID_ITEM.put(34, Identifier.make("item_34"));
		ID_ITEM.put(35, Identifier.make("item_35"));
		ID_ITEM.put(36, Identifier.make("item_36"));
		ID_ITEM.put(37, Identifier.make("item_37"));
		ID_ITEM.put(38, Identifier.make("item_38"));
		ID_ITEM.put(39, Identifier.make("item_39"));
		ID_ITEM.put(40, Identifier.make("item_40"));
		ID_ITEM.put(41, Identifier.make("item_41"));
		ID_ITEM.put(42, Identifier.make("item_42"));
		ID_ITEM.put(43, Identifier.make("item_43"));
		ID_ITEM.put(44, Identifier.make("item_44"));
		ID_ITEM.put(45, Identifier.make("item_45"));
		ID_ITEM.put(47, Identifier.make("item_47"));
		ID_ITEM.put(48, Identifier.make("item_48"));
		ID_ITEM.put(49, Identifier.make("item_49"));
		ID_ITEM.put(50, Identifier.make("item_50"));
		ID_ITEM.put(51, Identifier.make("item_51"));
		ID_ITEM.put(52, Identifier.make("item_52"));
		ID_ITEM.put(53, Identifier.make("item_53"));
		ID_ITEM.put(54, Identifier.make("item_54"));
		ID_ITEM.put(55, Identifier.make("item_55"));
		ID_ITEM.put(56, Identifier.make("item_56"));
		ID_ITEM.put(57, Identifier.make("item_57"));
		ID_ITEM.put(58, Identifier.make("item_58"));
		ID_ITEM.put(59, Identifier.make("item_59"));
		ID_ITEM.put(60, Identifier.make("item_60"));
		ID_ITEM.put(63, Identifier.make("item_63"));
		ID_ITEM.put(64, Identifier.make("item_64"));
		ID_ITEM.put(65, Identifier.make("item_65"));
		ID_ITEM.put(66, Identifier.make("item_66"));
		ID_ITEM.put(67, Identifier.make("item_67"));
		ID_ITEM.put(68, Identifier.make("item_68"));
		ID_ITEM.put(69, Identifier.make("item_69"));
		ID_ITEM.put(70, Identifier.make("item_70"));
		ID_ITEM.put(71, Identifier.make("item_71"));
		ID_ITEM.put(72, Identifier.make("item_72"));
		ID_ITEM.put(73, Identifier.make("item_73"));
		ID_ITEM.put(74, Identifier.make("item_74"));
		ID_ITEM.put(75, Identifier.make("item_75"));
		ID_ITEM.put(76, Identifier.make("item_76"));
		ID_ITEM.put(77, Identifier.make("item_77"));
		ID_ITEM.put(78, Identifier.make("item_78"));
		ID_ITEM.put(79, Identifier.make("item_79"));
		ID_ITEM.put(80, Identifier.make("item_80"));
		ID_ITEM.put(81, Identifier.make("item_81"));
		ID_ITEM.put(82, Identifier.make("item_82"));
		ID_ITEM.put(83, Identifier.make("item_83"));
		ID_ITEM.put(84, Identifier.make("item_84"));
		ID_ITEM.put(85, Identifier.make("item_85"));
		ID_ITEM.put(86, Identifier.make("item_86"));
		ID_ITEM.put(87, Identifier.make("item_87"));
		ID_ITEM.put(88, Identifier.make("item_88"));
		ID_ITEM.put(89, Identifier.make("item_89"));
		ID_ITEM.put(90, Identifier.make("item_90"));
		ID_ITEM.put(92, Identifier.make("item_92"));
		ID_ITEM.put(93, Identifier.make("item_93"));
		ID_ITEM.put(94, Identifier.make("item_94"));
		ID_ITEM.put(95, Identifier.make("item_95"));
		ID_ITEM.put(96, Identifier.make("item_96"));
		ID_ITEM.put(97, Identifier.make("item_97"));
		ID_ITEM.put(98, Identifier.make("item_98"));
		ID_ITEM.put(99, Identifier.make("item_99"));
		ID_ITEM.put(100, Identifier.make("item_100"));
		ID_ITEM.put(103, Identifier.make("item_103"));
		ID_ITEM.put(104, Identifier.make("item_104"));
		ID_ITEM.put(109, Identifier.make("item_109"));
		ID_ITEM.put(110, Identifier.make("item_110"));
		ID_ITEM.put(111, Identifier.make("item_111"));
		ID_ITEM.put(112, Identifier.make("item_112"));
		ID_ITEM.put(113, Identifier.make("item_113"));
		ID_ITEM.put(114, Identifier.make("item_114"));
		ID_ITEM.put(115, Identifier.make("item_115"));
		ID_ITEM.put(116, Identifier.make("item_116"));
		ID_ITEM.put(125, Identifier.make("item_125"));
		ID_ITEM.put(126, Identifier.make("item_126"));
		ID_ITEM.put(127, Identifier.make("item_127"));
		ID_ITEM.put(128, Identifier.make("item_128"));
		ID_ITEM.put(129, Identifier.make("item_129"));
		ID_ITEM.put(130, Identifier.make("item_130"));
		ID_ITEM.put(131, Identifier.make("item_131"));
		ID_ITEM.put(132, Identifier.make("item_132"));
		ID_ITEM.put(135, Identifier.make("item_135"));
		ID_ITEM.put(136, Identifier.make("item_136"));
		ID_ITEM.put(141, Identifier.make("item_141"));
		ID_ITEM.put(142, Identifier.make("item_142"));
		ID_ITEM.put(143, Identifier.make("item_143"));
		ID_ITEM.put(151, Identifier.make("item_151"));
		ID_ITEM.put(157, Identifier.make("item_157"));
		ID_ITEM.put(158, Identifier.make("item_158"));
		ID_ITEM.put(159, Identifier.make("item_159"));
		ID_ITEM.put(167, Identifier.make("item_167"));
		ID_ITEM.put(173, Identifier.make("item_173"));
		ID_ITEM.put(174, Identifier.make("item_174"));
		ID_ITEM.put(175, Identifier.make("item_175"));
		ID_ITEM.put(189, Identifier.make("item_189"));
		ID_ITEM.put(190, Identifier.make("item_190"));
		ID_ITEM.put(191, Identifier.make("item_191"));
		ID_ITEM.put(205, Identifier.make("item_205"));
		ID_ITEM.put(206, Identifier.make("item_206"));
		ID_ITEM.put(207, Identifier.make("item_207"));
		ID_ITEM.put(240, Identifier.make("item_240"));
		ID_ITEM.put(241, Identifier.make("item_241"));
		
		ID_PARTICLE.put(0, Identifier.make("particle_0"));
		ID_PARTICLE.put(1, Identifier.make("particle_1"));
		ID_PARTICLE.put(2, Identifier.make("particle_2"));
		ID_PARTICLE.put(3, Identifier.make("particle_3"));
		ID_PARTICLE.put(4, Identifier.make("particle_4"));
		ID_PARTICLE.put(5, Identifier.make("particle_5"));
		ID_PARTICLE.put(6, Identifier.make("particle_6"));
		ID_PARTICLE.put(7, Identifier.make("particle_7"));
		ID_PARTICLE.put(16, Identifier.make("particle_16"));
		ID_PARTICLE.put(17, Identifier.make("particle_17"));
		ID_PARTICLE.put(19, Identifier.make("particle_19"));
		ID_PARTICLE.put(20, Identifier.make("particle_20"));
		ID_PARTICLE.put(21, Identifier.make("particle_21"));
		ID_PARTICLE.put(22, Identifier.make("particle_22"));
		ID_PARTICLE.put(32, Identifier.make("particle_32"));
		ID_PARTICLE.put(33, Identifier.make("particle_33"));
		ID_PARTICLE.put(48, Identifier.make("particle_48"));
		ID_PARTICLE.put(49, Identifier.make("particle_49"));
		ID_PARTICLE.put(64, Identifier.make("particle_64"));
		ID_PARTICLE.put(80, Identifier.make("particle_80"));
		ID_PARTICLE.put(96, Identifier.make("particle_96"));
		ID_PARTICLE.put(97, Identifier.make("particle_97"));
	}
}
