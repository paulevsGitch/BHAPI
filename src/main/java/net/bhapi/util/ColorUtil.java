package net.bhapi.util;

public class ColorUtil {
	public static final int WHITE_COLOR = 0xFFFFFF;
	
	public static float getRed(int color) {
		return ((color >> 16) & 255) / 255F;
	}
	
	public static float getGreen(int color) {
		return ((color >> 8) & 255) / 255F;
	}
	
	public static float getBlue(int color) {
		return (color & 255) / 255F;
	}
}
