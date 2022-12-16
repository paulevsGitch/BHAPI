package net.bhapi.level.light;

import net.bhapi.storage.Vec3I;
import net.minecraft.level.LightType;

import java.util.Objects;

public class BHLightArea {
	private final Vec3I minPos = new Vec3I();
	private final Vec3I maxPos = new Vec3I();
	private final LightType type;
	private final int hash;
	
	public BHLightArea(LightType type, int x1, int y1, int z1, int x2, int y2, int z2) {
		this.minPos.set(x1, y1, z1);
		this.maxPos.set(x2, y2, z2);
		this.type = type;
		this.hash = Objects.hash(minPos, maxPos, type);
	}
	
	public Vec3I getMinPos() {
		return minPos;
	}
	
	public Vec3I getMaxPos() {
		return maxPos;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BHLightArea area)) return false;
		return type == area.type && minPos.equals(area.minPos) && maxPos.equals(area.maxPos);
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
}
