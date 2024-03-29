package net.bhapi.client.render.texture;

import net.bhapi.BHAPI;
import net.bhapi.client.BHAPIClient;
import net.bhapi.storage.Vec2F;
import net.bhapi.storage.Vec2I;
import net.bhapi.util.Identifier;
import net.bhapi.util.ImageUtil;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.CLIENT)
public class TextureAtlas {
	private static final Identifier EMPTY_ID = Identifier.make("empty");
	private final Map<Identifier, TextureInfo> textures = new HashMap<>();
	private final UVPair[] uvs;
	private final int glTarget;
	
	public TextureAtlas(Map<Identifier, BufferedImage> images) {
		Map<BufferedImage, Identifier> inverted = MathUtil.invertMap(images);
		List<BufferedImage> list = inverted.keySet().stream().filter(img -> img == ImageUtil.EMPTY).toList();
		list.forEach(img -> {
			Identifier id = inverted.get(img);
			inverted.remove(img);
			images.remove(id);
		});
		inverted.put(ImageUtil.EMPTY, EMPTY_ID);
		images.put(EMPTY_ID, ImageUtil.EMPTY);
		
		List<ImageInfo> info = images.values().stream().map(img -> new ImageInfo(
			img,
			img.getWidth() * img.getHeight(),
			MathUtil.getCeilBitIndex(Math.max(img.getWidth(), img.getHeight()))
		)).sorted().toList();
		
		int totalArea = 0;
		for (ImageInfo imageInfo: info) totalArea += imageInfo.area;
		
		BufferedImage atlasImage = null;
		short start = (short) MathUtil.getClosestPowerOfTwo((int) Math.sqrt(totalArea));
		List<Layer> layers = new ArrayList<>(8);
		
		short side = start;
		while (side <= 16384 && (atlasImage = pack(info, side, layers)) == null) {side <<= 1;}
		
		if (atlasImage == null) {
			throw new RuntimeException("Can't create texture atlas! Size is larger than 16384 pixels");
		}
		
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			try {
				ImageIO.write(atlasImage, "png", new File("./debug_atlas.png"));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		uvs = new UVPair[info.size()];
		int[] data = new int[] {0, atlasImage.getWidth()};
		layers.forEach(layer -> layer.images.forEach((pos, imgInfo) -> {
			int px = pos.x << layer.index;
			int py = pos.y << layer.index;
			BufferedImage img = imgInfo.img();
			Identifier id = inverted.get(img);
			Vec2I tpos = new Vec2I(px, py);
			Vec2I size = new Vec2I(img.getWidth(), img.getHeight());
			Vec2F uv1 = new Vec2F(px, py).divide(data[1]);
			Vec2F uv2 = new Vec2F(img.getWidth(), img.getHeight()).divide(data[1]).add(uv1);
			int uvID = data[0]++;
			uvs[uvID] = new UVPair(tpos, size, uv1, uv2);
			RenderLayer renderLayer = ImageUtil.getLayer(img);
			textures.put(id, new TextureInfo(uvID, renderLayer));
		}));
		
		glTarget = BHAPIClient.getMinecraft().textureManager.bindImage(atlasImage);
	}
	
	public void rebuild(Map<Identifier, BufferedImage> images) {
		List<Identifier> keys = images.keySet().stream().filter(i -> !textures.containsKey(i)).toList();
		keys.forEach(images::remove);
		
		Map<BufferedImage, Identifier> inverted = MathUtil.invertMap(images);
		List<BufferedImage> list = inverted.keySet().stream().filter(img -> img == ImageUtil.EMPTY).toList();
		list.forEach(img -> {
			Identifier id = inverted.get(img);
			inverted.remove(img);
			images.remove(id);
		});
		inverted.put(ImageUtil.EMPTY, EMPTY_ID);
		images.put(EMPTY_ID, ImageUtil.EMPTY);
		
		List<ImageInfo> info = images.values().stream().map(img -> new ImageInfo(
			img,
			img.getWidth() * img.getHeight(),
			MathUtil.getCeilBitIndex(Math.max(img.getWidth(), img.getHeight()))
		)).sorted().toList();
		
		int totalArea = 0;
		for (ImageInfo imageInfo: info) totalArea += imageInfo.area;
		
		BufferedImage atlasImage = null;
		short start = (short) MathUtil.getClosestPowerOfTwo((int) Math.sqrt(totalArea));
		List<Layer> layers = new ArrayList<>(8);
		
		short side = start;
		while (side <= 16384 && (atlasImage = pack(info, side, layers)) == null) {
			side <<= 1;
		}
		
		if (atlasImage == null) {
			throw new RuntimeException("Can't create texture atlas! Size is larger than 16384 pixels");
		}
		
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			try {
				ImageIO.write(atlasImage, "png", new File("./debug_atlas.png"));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		int[] data = new int[] {0, atlasImage.getWidth()};
		layers.forEach(layer -> layer.images.forEach((pos, imgInfo) -> {
			int px = pos.x << layer.index;
			int py = pos.y << layer.index;
			BufferedImage img = imgInfo.img();
			Identifier id = inverted.get(img);
			int uvID = textures.get(id).uvID;
			Vec2I tpos = new Vec2I(px, py);
			Vec2I size = new Vec2I(img.getWidth(), img.getHeight());
			Vec2F uv1 = new Vec2F(px, py).divide(data[1]);
			Vec2F uv2 = new Vec2F(img.getWidth(), img.getHeight()).divide(data[1]).add(uv1);
			uvs[uvID] = new UVPair(tpos, size, uv1, uv2);
		}));
		
		BHAPIClient.getMinecraft().textureManager.bindImage(atlasImage, glTarget);
	}
	
	private TextureInfo getInfo(Identifier id, boolean silent) {
		TextureInfo info = textures.get(id);
		if (info == null) {
			if (!silent) BHAPI.warn("No texture " + id + " in atlas");
			return getInfo(EMPTY_ID, true);
		}
		return info;
	}
	
	public int getTextureIndex(Identifier id) {
		return getTextureIndex(id, false);
	}
	
	public int getTextureIndex(Identifier id, boolean silent) {
		return getInfo(id, silent).uvID;
	}
	
	public UVPair getUV(int index) {
		return uvs[index];
	}
	
	public TextureSample getSample(Identifier id) {
		return getSample(id, false);
	}
	
	public TextureSample getSample(Identifier id, boolean silent) {
		TextureInfo info = getInfo(id, silent);
		return new TextureSample(this, info.uvID, info.layer);
	}
	
	public void bind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTarget);
	}
	
	public int getGlTarget() {
		return glTarget;
	}
	
	private BufferedImage pack(List<ImageInfo> info, final short side, List<Layer> layers) {
		Map<Integer, Layer> preLayers = new HashMap<>();
		info.forEach(imageInfo -> preLayers.computeIfAbsent(imageInfo.layer, key -> new Layer(key, side)));
		
		layers.clear();
		layers.addAll(preLayers.values().stream().sorted().toList());
		
		int layersCount = layers.size();
		for (short i = 0; i < layersCount; i++) {
			Layer layer = layers.get(i);
			for (short j = (short) (i + 1); j < layersCount; j++) {
				layer.connect(layers.get(j));
			}
		}
		
		BufferedImage image = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		
		AtomicBoolean packed = new AtomicBoolean(true);
		info.forEach(imageInfo -> {
			Layer layer = preLayers.get(imageInfo.layer);
			Vec2I pos = layer.addImage(imageInfo);
			if (pos == null) {
				packed.set(false);
				return;
			}
			g.drawImage(imageInfo.img, pos.x << layer.index, pos.y << layer.index, null);
		});
		
		return packed.get() ? image : null;
	}
	
	private record ImageInfo(BufferedImage img, int area, int layer) implements Comparable<ImageInfo> {
		@Override
		public int compareTo(ImageInfo info) {
			return Integer.compare(info.layer, layer);
		}
	}
	
	private static class Layer implements Comparable<Layer> {
		Map<Vec2I, ImageInfo> images = new HashMap<>();
		List<Layer> layers = new ArrayList<>();
		final int index;
		final int width;
		
		Layer(int index, int width) {
			this.index = index;
			this.width = width / (1 << index);
		}
		
		void connect(Layer layer) {
			layers.add(layer);
		}
		
		Vec2I addImage(ImageInfo info) {
			Vec2I pos = new Vec2I();
			Vec2I checkPos = new Vec2I();
			for (pos.y = 0; pos.y < width; pos.y++) {
				for (pos.x = 0; pos.x < width; pos.x++) {
					if (!images.containsKey(pos) && hasSpace(pos, checkPos)) {
						images.put(pos, info);
						return pos;
					}
				}
			}
			return null;
		}
		
		boolean hasSpace(Vec2I pos, Vec2I checkPos) {
			int px = pos.x << index;
			int py = pos.y << index;
			for (Layer layer: layers) {
				checkPos.x = px >> layer.index;
				checkPos.y = py >> layer.index;
				ImageInfo info = layer.images.get(checkPos);
				if (info == null) continue;
				int sx = px - (checkPos.x << layer.index);
				int sy = py - (checkPos.y << layer.index);
				if (sx >= info.img.getWidth() || sy >= info.img.getHeight()) continue;
				return false;
			}
			return true;
		}
		
		@Override
		public int compareTo(Layer layer) {
			return Integer.compare(index, layer.index);
		}
	}
	
	record TextureInfo(int uvID, RenderLayer layer) {}
}
