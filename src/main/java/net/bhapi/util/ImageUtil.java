package net.bhapi.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.bhapi.BHAPI;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.texture.AnimationTextureBinder;
import net.bhapi.client.render.texture.RenderLayer;
import net.bhapi.client.render.texture.TextureAtlas;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.mixin.client.TextureManagerAccessor;
import net.bhapi.registry.Registry;
import net.bhapi.storage.Resource;
import net.minecraft.client.render.TextureBinder;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageUtil {
	public static final BufferedImage EMPTY = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
	private static final Registry<TextureBinder> BINDERS = new Registry<>();
	
	public static void registerAnimation(Identifier id, TextureBinder binder) {
		BINDERS.register(id, binder);
	}
	
	public static BufferedImage loadFromFile(File file) {
		BufferedImage img = EMPTY;
		try {
			img = ImageIO.read(file);
		}
		catch (IOException e) {
			BHAPI.warn("Missing image: " + file);
		}
		return img;
	}
	
	public static BufferedImage loadFromSource(String path) {
		BufferedImage img = EMPTY;
		try {
			Resource resource = ResourceUtil.getResource(path, ".png");
			if (resource != null) {
				img = ImageIO.read(resource.getStream());
				resource.close();
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
	
	public static BufferedImage loadFromSource(Identifier id) {
		String path = "/assets/" + id.getModID() + "/" + id.getName() + ".png";
		return loadFromSource(path);
	}
	
	public static Map<Identifier, BufferedImage> loadTexturesFromPathDir(Identifier folder) {
		String path = "/assets/" + folder.getModID() + "/textures/" + folder.getName();
		if (!path.endsWith("/")) path += "/";
		Map<Identifier, BufferedImage> result = new HashMap<>();
		
		Map<Resource, Resource> links = new HashMap<>();
		List<Resource> metaList = ResourceUtil.getResources(path, ".png.mcmeta");
		List<Resource> pngList = ResourceUtil.getResources(path, ".png");
		
		List<Resource> remove = metaList.stream().filter(meta -> {
			String name = meta.getName();
			name = name.substring(0, name.length() - 4);
			boolean add = true;
			for (Resource resource: pngList) {
				if (name.equals(resource.getName())) {
					links.put(resource, meta);
					add = false;
					break;
				}
			}
			return add;
		}).toList();
		metaList.removeAll(remove);
		remove.forEach(resource -> {
			try {
				resource.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		pngList.forEach(resource -> {
			try {
				BufferedImage img = ImageIO.read(resource.getStream());
				Resource meta = links.get(resource);
				
				String name = resource.getName();
				String fName = folder.getName();
				Identifier id = Identifier.make(folder.getModID(), fName.isEmpty() ? name : fName + "/" + name);
				
				if (meta != null) {
					int width = img.getWidth();
					int height = img.getHeight();
					int frameCount = height / width;
					
					BufferedImage animImg = makeImage(width, width);
					animImg.getGraphics().drawImage(img, 0, 0, null);
					
					if (frameCount * width != height) {
						BHAPI.warn("Wrong aspect ratio of animation " + meta.getName());
						meta.close();
					}
					else {
						JsonObject json = JsonUtil.read(meta.getStream());
						json = json.getAsJsonObject("animation");
						
						boolean interpolate = json.has("interpolate") && json.get("interpolate").getAsBoolean();
						int frametime = json.has("frametime") ? json.get("frametime").getAsInt() : 1;
						
						BufferedImage[] frames = new BufferedImage[frameCount];
						for (int i = 0; i < frameCount; i++) {
							frames[i] = img.getSubimage(0, i * width, width, width);
						}
						
						BufferedImage[] sortedFrames;
						int[] time;
						if (json.has("frames")) {
							JsonArray arr = json.getAsJsonArray("frames");
							sortedFrames = new BufferedImage[arr.size()];
							time = new int[sortedFrames.length];
							Arrays.fill(time, frametime);
							for (int i = 0; i < sortedFrames.length; i++) {
								JsonElement element = arr.get(i);
								if (element.isJsonPrimitive()) {
									int index = element.getAsInt();
									sortedFrames[i] = frames[index];
								}
								else {
									JsonObject obj = element.getAsJsonObject();
									int index = obj.get("index").getAsInt();
									sortedFrames[i] = frames[index];
									if (obj.has("time")) {
										time[i] = obj.get("time").getAsInt();
									}
								}
							}
						}
						else {
							sortedFrames = frames;
							time = new int[sortedFrames.length];
							Arrays.fill(time, frametime);
						}
						
						AnimationTextureBinder binder = new AnimationTextureBinder(sortedFrames, time, interpolate);
						BINDERS.register(id, binder);
					}
					
					img = animImg;
				}
				
				result.put(id, img);
				resource.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		});
		return result;
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
	
	@SuppressWarnings("unchecked")
	public static void processAnimations() {
		TextureAtlas atlas = Textures.getAtlas();
		List<TextureBinder> binders = (List<TextureBinder>) ((TextureManagerAccessor) BHAPIClient.getMinecraft().textureManager).getTextureBinders();
		
		BINDERS.forEach((id, binder) -> {
			binder.index = atlas.getTextureIndex(id);
			boolean added = false;
			for (int i = 0; i < binders.size(); i++) {
				TextureBinder b = binders.get(i);
				if (b.index == binder.index) {
					binders.set(i, binder);
					added = true;
					break;
				}
			}
			if (!added) binders.add(binder);
		});
	}
	
	public static RenderLayer getLayer(BufferedImage img) {
		int[] data = getPixelData(img);
		RenderLayer layer = RenderLayer.SOLID;
		if (data == null) return layer;
		for (int argb: data) {
			int a = (argb >> 24) & 255;
			if (a == 0) layer = RenderLayer.TRANSPARENT;
			if (a > 0 && a < 255) {
				layer = RenderLayer.TRANSLUCENT;
				break;
			}
		}
		return layer;
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
