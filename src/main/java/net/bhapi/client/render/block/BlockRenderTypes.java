package net.bhapi.client.render.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BlockRenderTypes {
	public static final byte FULL_CUBE = 0;
	public static final byte CROSS = 1;
	public static final byte TORCH = 2;
	public static final byte FIRE = 3;
	public static final byte FLUID = 4;
	public static final byte REDSTONE_DUST = 5;
	public static final byte CROP = 6;
	public static final byte DOOR = 7;
	public static final byte LADDER = 8;
	public static final byte RAILS = 9;
	public static final byte STAIRS = 10;
	public static final byte FENCE = 11;
	public static final byte LEVER = 12;
	public static final byte CACTUS = 13;
	public static final byte BED = 14;
	public static final byte REPEATER = 15;
	public static final byte PISTON = 16;
	public static final byte PISTON_HEAD = 17;
	public static final byte CUSTOM = 18;
	public static final byte EMPTY = -1;
}
