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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class Textures {
	private static final int[] EXCLUDE_TERRAIN = new int[] {
		207, // Water first row
		222, 223, // Water second row
		239, // Lava first row
		254, 255, // Lava second row
		240, 241, 242, 243, 244, 245, 246, 247, 248, 249 // Block breaking
	};
	
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
		addTextures("terrain", terrain, 16, LOADED_TEXTURES);
		excludeTextures("terrain", LOADED_TEXTURES, EXCLUDE_TERRAIN);
		addTextures("item", loadTexture("/gui/items.png"), 16, LOADED_TEXTURES);
		addTextures("particle", loadTexture("/particles.png"), 16, LOADED_TEXTURES);
		
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
		addTextures("terrain", terrain, 16, LOADED_TEXTURES);
		excludeTextures("terrain", LOADED_TEXTURES, EXCLUDE_TERRAIN);
		addTextures("item", loadTexture("/gui/items.png"), 16, LOADED_TEXTURES);
		addTextures("particle", loadTexture("/particles.png"), 16, LOADED_TEXTURES);
		
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
	
	private static void addTextures(String prefix, BufferedImage atlas, int size, Map<Identifier, BufferedImage> textures) {
		int width = atlas.getWidth() / size;
		int height = atlas.getHeight() / size;
		for (byte x = 0; x < 16; x++) {
			for (byte y = 0; y < 16; y++) {
				BufferedImage img = ImageUtil.makeImage(width, height);
				img.getGraphics().drawImage(atlas, -x * width, -y * height, null);
				String name = String.format(Locale.ROOT, "%s_%d", prefix, y * size + x);
				if (!isEmpty(img)) textures.put(Identifier.make(name), img);
			}
		}
	}
	
	private static boolean isEmpty(BufferedImage img) {
		int[] data = ImageUtil.getPixelData(img);
		int countAlpha = 0;
		int countPurple = 0;
		for (int i = 0; i < data.length; i++) {
			int argb = data[i];
			if ((argb & 0xFF000000) == 0) countAlpha++;
			if (argb == 0XFFD67FFF || argb == 0XFF6B3F7F) countPurple++;
		}
		return countAlpha == data.length || countPurple == data.length;
	}
	
	private static void excludeTextures(String prefix, Map<Identifier, BufferedImage> textures, int[] ids) {
		Arrays.stream(ids).forEach(id -> {
			String name = String.format(Locale.ROOT, "%s_%d", prefix, id);
			textures.remove(Identifier.make(name));
		});
	}
}
