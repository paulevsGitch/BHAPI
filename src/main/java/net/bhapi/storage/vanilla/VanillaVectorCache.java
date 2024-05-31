package net.bhapi.storage.vanilla;

import net.minecraft.util.maths.Vec3D;

import java.util.ArrayList;
import java.util.List;

public class VanillaVectorCache {
	private final List<Vec3D> data = new ArrayList<>(8192);
	private int index = 0;
	
	public Vec3D get(double x, double y, double z) {
		Vec3D vector;
		if (index < data.size()) {
			vector = data.get(index);
			vector.x = x;
			vector.y = y;
			vector.z = z;
		}
		else {
			vector = Vec3D.make(x, y, z);
			data.add(vector);
		}
		if (++index >= 8192) index = 0;
		return vector;
	}
}
