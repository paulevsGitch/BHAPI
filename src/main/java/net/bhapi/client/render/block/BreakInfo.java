package net.bhapi.client.render.block;

import net.bhapi.storage.Vec3I;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BreakInfo {
	public static final Vec3I POS = new Vec3I();
	public static int stage = -1;
}
