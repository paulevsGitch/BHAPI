package net.bhapi.client.render;

import net.bhapi.BHAPI;
import net.bhapi.client.BHAPIClient;
import net.bhapi.util.BufferUtil;
import net.bhapi.util.Identifier;
import net.bhapi.util.ImageUtil;
import net.bhapi.util.ImageUtil.FormatConvert;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Textures {
	private static TextureAtlas atlas;
	
	public static void init() {
		BHAPI.log("Making texture atlas");
		
		Map<Identifier, BufferedImage> textures = new HashMap<>();
		addTextures("terrain", loadTexture("/terrain.png"), 16, textures);
		addTextures("items", loadTexture("/gui/items.png"), 16, textures);
		addTextures("particles", loadTexture("/particles.png"), 16, textures);
		
		TextureAtlas atlas = new TextureAtlas(textures);
		System.out.println("Stone: " + atlas.getTextureIndex(Identifier.make("terrain_1")));
		System.out.println("Dirt: " + atlas.getTextureIndex(Identifier.make("terrain_2")));
		System.out.println("Item: " + atlas.getTextureIndex(Identifier.make("items_0")));
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
}
