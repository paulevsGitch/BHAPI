package net.bhapi.util;

import net.bhapi.BHAPI;
import net.bhapi.client.BHAPIClient;
import net.minecraft.client.resource.TexturePack;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {
	public static final BufferedImage EMPTY = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
	
	public static BufferedImage load(File file) {
		BufferedImage img = EMPTY;
		try {
			img = ImageIO.read(file);
		}
		catch (IOException e) {
			BHAPI.warn("Missing image: " + file);
		}
		return img;
	}
	
	public static BufferedImage load(String path) {
		TexturePack pack = BHAPIClient.getMinecraft().texturePackManager.texturePack;
		BufferedImage img = EMPTY;
		try {
			InputStream stream = pack.getResourceAsStream(path);
			if (stream != null) {
				img = ImageIO.read(stream);
				stream.close();
			}
			else {
				BHAPI.warn("Missing image: " + path);
			}
		}
		catch (IOException e) {
			BHAPI.warn("Missing image: " + path);
		}
		return img;
	}
	
	public static BufferedImage load(Identifier id) {
		String path = "/assets/" + id.getModID() + "/" + id.getName() + ".png";
		return load(path);
	}
	
	public static int[] getPixelData(BufferedImage image) {
		DataBuffer buffer = image.getRaster().getDataBuffer();
		return buffer instanceof DataBufferInt ? ((DataBufferInt) buffer).getData() : null;
	}
	
	public static void convertFormat(int[] pixels, FormatConvert format) {
		switch (format) {
			case ABGR_TO_ARGB -> {
				for (int i = 0; i < pixels.length; i++) {
					int abgr = pixels[i];
					int r = abgr & 255;
					int g = abgr & 0x0000FF00;
					int b = (abgr >> 16) & 255;
					int a = abgr & 0xFF000000;
					pixels[i] = a | r << 16 | g | b;
				}
			}
			case ARGB_TO_ABGR -> {
				for (int i = 0; i < pixels.length; i++) {
					int argb = pixels[i];
					int r = (argb >> 16) & 255;
					int g = argb & 0x0000FF00;
					int b = argb & 255;
					int a = argb & 0xFF000000;
					pixels[i] = a | b << 16 | g | r;
				}
			}
		}
	}
	
	public static BufferedImage makeImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
	
	static {
		EMPTY.setRGB(0, 0, Color.MAGENTA.getRGB());
		EMPTY.setRGB(1, 1, Color.MAGENTA.getRGB());
		EMPTY.setRGB(0, 1, Color.BLACK.getRGB());
		EMPTY.setRGB(1, 0, Color.BLACK.getRGB());
	}
	
	public enum FormatConvert {
		ABGR_TO_ARGB, ARGB_TO_ABGR
	}
}
