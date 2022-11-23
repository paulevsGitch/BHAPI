package net.bhapi.client.render.texture;

import net.bhapi.client.BHAPIClient;
import net.bhapi.mixin.client.MinecraftAccessor;
import net.bhapi.util.MathUtil;
import net.minecraft.client.render.TextureBinder;

import java.awt.image.BufferedImage;

public class AnimationTextureBinder extends TextureBinder {
	private final int[][] pixels;
	private final boolean interpolate;
	private final byte[] color = new byte[4];
	private final int speed;
	private final int size;
	
	public AnimationTextureBinder(BufferedImage[] frames, boolean interpolate, int speed) {
		super(0);
		this.interpolate = interpolate;
		BufferedImage img = frames[0];
		this.grid = new byte[(img.getWidth() * img.getHeight()) << 2];
		pixels = new int[frames.length][this.grid.length >> 2];
		for (short i = 0; i < frames.length; i++) {
			frames[i].getRGB(0, 0, img.getWidth(), img.getHeight(), pixels[i], 0, img.getWidth());
		}
		this.size = this.grid.length >> 2;
		this.speed = speed;
	}
	
	@Override
	public void update() {
		int ticks = ((MinecraftAccessor) BHAPIClient.getMinecraft()).bhapi_getTicks();
		int index1 = (ticks / speed) % pixels.length;
		int index2 = (index1 + 1) % pixels.length;
		float delta = (float) (ticks % speed) / speed;
		for (int i = 0; i < this.size; i++) {
			byte[] color = getColor(i, index1, index2, delta);
			int index = i << 2;
			for (byte j = 0; j < 4; j++) {
				this.grid[index++] = color[j];
			}
		}
	}
	
	private byte[] getColor(int xyIndex, int index1, int index2, float delta) {
		int argb1 = pixels[index1][xyIndex];
		int argb2 = pixels[index2][xyIndex];
		if (interpolate) {
			color[3] = (byte) (MathUtil.lerp(((argb1 >> 24) & 255) / 255F, ((argb2 >> 24) & 255) / 255F, delta) * 255F);
			color[0] = (byte) (MathUtil.lerp(((argb1 >> 16) & 255)/ 255F, ((argb2 >> 16) & 255) / 255F, delta) * 255F);
			color[1] = (byte) (MathUtil.lerp(((argb1 >> 8) & 255) / 255F, ((argb2 >> 8) & 255) / 255F, delta) * 255F);
			color[2] = (byte) (MathUtil.lerp((argb1 & 255) / 255F, (argb2 & 255) / 255F, delta) * 255F);
		}
		else {
			color[0] = (byte) ((argb1 >> 24) & 255);
			color[1] = (byte) ((argb1 >> 16) & 255);
			color[2] = (byte) ((argb1 >> 8) & 255);
			color[3] = (byte) (argb1 & 255);
		}
		return color;
	}
}
