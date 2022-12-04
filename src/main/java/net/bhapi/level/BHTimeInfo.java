package net.bhapi.level;

import net.bhapi.blockstate.BlockState;
import net.bhapi.storage.Vec3I;

public class BHTimeInfo {
	private final Vec3I pos = new Vec3I();
	private final BlockState state;
	private final int hash;
	private long time;
	
	public BHTimeInfo(int x, int y, int z, BlockState state) {
		this.state = state;
		this.pos.set(x, y, z);
		this.hash = this.pos.hashCode();
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public BlockState getState() {
		return state;
	}
	
	public long getTime() {
		return time;
	}
	
	public int getX() {
		return pos.x;
	}
	
	public int getY() {
		return pos.y;
	}
	
	public int getZ() {
		return pos.z;
	}
	
	@Override
	public int hashCode() {
		return this.hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof BHTimeInfo)) return false;
		BHTimeInfo info = (BHTimeInfo) obj;
		return info.state == state && info.pos.equals(pos);
	}
}
