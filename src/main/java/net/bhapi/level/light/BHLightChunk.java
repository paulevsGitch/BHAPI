package net.bhapi.level.light;

import net.bhapi.storage.Vec3I;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BHLightChunk {
	private final byte[] light = new byte[2048];
	private final Vec3I pos = new Vec3I(0, -1000, 0);
	private boolean filled;
	
	private int getLight(int index) {
		int dataIndex = index >> 1;
		byte light = this.light[dataIndex];
		return (index & 1) == 0 ? light & 15 : (light >> 4) & 15;
	}
	
	private void setLight(int index, int value) {
		int dataIndex = index >> 1;
		byte light = this.light[dataIndex];
		if ((index & 1) == 0) light = (byte) ((light & 0xF0) | value);
		else light = (byte) ((light & 0x0F) | (value << 4));
		this.light[dataIndex] = light;
	}
	
	public int getLight(int x, int y, int z) {
		return getLight(getIndex(x, y, z));
	}
	
	public void setLight(int x, int y, int z, int value) {
		setLight(getIndex(x, y, z), value);
	}
	
	private int getIndex(int x, int y, int z) {
		return x << 8 | y << 4 | z;
	}
	
	public byte[] getData() {
		return light;
	}
	
	public boolean isFilled() {
		return filled;
	}
	
	public void setFilled(boolean filled) {
		this.filled = filled;
	}
	
	public boolean wrongPos(Vec3I pos) {
		return !pos.equals(this.pos);
	}
	
	public void setPos(Vec3I pos) {
		this.pos.set(pos);
	}
}
