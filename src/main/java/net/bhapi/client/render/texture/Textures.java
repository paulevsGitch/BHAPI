package net.bhapi.client.render.texture;

import net.bhapi.BHAPI;
import net.bhapi.client.BHAPIClient;
import net.bhapi.mixin.client.BaseItemAccessor;
import net.bhapi.mixin.client.TextureManagerAccessor;
import net.bhapi.util.BufferUtil;
import net.bhapi.util.Identifier;
import net.bhapi.util.ImageUtil;
import net.bhapi.util.ImageUtil.FormatConvert;
import net.minecraft.block.BaseBlock;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.item.BaseItem;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Textures {
	private static final int[] EXCLUDE_TERRAIN = new int[] {
		206, 207, // Water first row
		222, 223, // Water second row
		238, 239, // Lava first row
		254, 255, // Lava second row
		240, 241, 242, 243, 244, 245, 246, 247, 248, 249 // Block breaking
	};
	private static TextureAtlas atlas;
	
	public static void init() {
		BHAPI.log("Making texture atlas");
		
		Map<Identifier, BufferedImage> textures = new HashMap<>();
		addTextures("terrain", loadTexture("/terrain.png"), 16, textures);
		excludeTextures("terrain", textures, EXCLUDE_TERRAIN);
		addTextures("item", loadTexture("/gui/items.png"), 16, textures);
		addTextures("particle", loadTexture("/particles.png"), 16, textures);
		
		atlas = new TextureAtlas(textures);
		
		Arrays.stream(BaseBlock.BY_ID).filter(Objects::nonNull).forEach(block -> {
			Identifier id = Identifier.make("terrain_" + block.texture);
			block.texture = atlas.getTextureIndex(id);
		});
		
		List<?> binders = ((TextureManagerAccessor) BHAPIClient.getMinecraft().textureManager).getTextureBinders();
		binders.forEach(obj -> {
			TextureBinder binder = (TextureBinder) obj;
			Identifier id = Identifier.make("terrain_" + binder.index);
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
	}
	
	public static void bindAtlas() {
		atlas.bind();
	}
	
	public static TextureAtlas getAtlas() {
		return atlas;
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
