package net.bhapi.storage;

import net.minecraft.util.maths.Vec3f;

import java.util.ArrayList;
import java.util.List;

public class VanillaVectorCache {
	private final List<Vec3f> data = new ArrayList<>(8192);
	private int index = 0;
	
	public Vec3f get(double x, double y, double z) {
		Vec3f vector;
		if (index < data.size()) {
			vector = data.get(index);
			vector.x = x;
			vector.y = y;
			vector.z = z;
		}
		else {
			vector = Vec3f.make(x, y, z);
			data.add(vector);
		}
		if (++index >= 8192) index = 0;
		return vector;
	}
}
