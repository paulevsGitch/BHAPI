package net.bhapi.storage;

import net.bhapi.util.MathUtil;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WorldCache<T> {
	private final BiConsumer<Vec3I, T> updater;
	private final Consumer<T> processor;
	private final Vec3I[] positions;
	private final Vec3I center;
	private final int deltaXZ;
	private final int deltaY;
	private final int maskXZ;
	private final int bitsXYZ;
	private final int bitsXZ;
	private final int sizeXZ;
	private final int sizeY;
	private final int maskY;
	private final Vec3I pos;
	private final T[] data;
	
	@SuppressWarnings("unchecked")
	public WorldCache(int sizeXZ, int sizeY, BiConsumer<Vec3I, T> updater, Consumer<T> processor) {
		this.deltaXZ = (sizeXZ >> 1);
		this.deltaY = (sizeY >> 1);
		
		this.sizeXZ = sizeXZ;
		this.sizeY = sizeY;
		
		sizeXZ = MathUtil.getClosestPowerOfTwo(sizeXZ);
		sizeY = MathUtil.getClosestPowerOfTwo(sizeY);
		int capacity = sizeXZ * sizeXZ * sizeY;
		
		this.processor = processor;
		this.updater = updater;
		
		this.positions = new Vec3I[capacity];
		this.data = (T[]) new Object[capacity];
		this.center = new Vec3I();
		this.maskXZ = sizeXZ - 1;
		this.maskY = sizeY - 1;
		this.pos = new Vec3I();
		
		this.bitsXZ = MathUtil.getCeilBitIndex(sizeXZ);
		this.bitsXYZ = bitsXZ + MathUtil.getCeilBitIndex(sizeY);
		
		for (int i = 0; i < data.length; i++) {
			this.positions[i] = new Vec3I(0, Integer.MIN_VALUE, 0);
		}
	}
	
	public void fill(Supplier<T> constructor) {
		for (int i = 0; i < data.length; i++) {
			data[i] = constructor.get();
		}
	}
	
	public void setCenter(int x, int y, int z) {
		center.set(x, y, z);
	}
	
	public void process() {
		for (int index = 0; index < data.length; index++) {
			pos.x = (index >> this.bitsXYZ) & maskXZ;
			pos.y = (index >> this.bitsXZ) & maskY;
			pos.z = index & maskXZ;
			if (pos.x >= sizeXZ || pos.z >= sizeXZ || pos.y >= sizeY) continue;
			pos.subtract(deltaXZ, deltaY, deltaXZ).add(center);
			if (!positions[index].equals(pos)) {
				positions[index].set(pos);
				updater.accept(pos, data[index]);
			}
			processor.accept(data[index]);
		}
	}
}
