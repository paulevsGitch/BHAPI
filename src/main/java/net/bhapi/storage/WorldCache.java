package net.bhapi.storage;

import net.bhapi.util.MathUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WorldCache<T> {
	private final Supplier<T> constructor;
	private final Vec3I[] updateOrder;
	private final Vec3I center;
	private final int maskXZ;
	private final int bitsXYZ;
	private final int bitsXZ;
	private final int deltaXZ;
	private final int deltaY;
	private final int sizeXZ;
	private final int sizeY;
	private final int maskY;
	private final Vec3I pos;
	private final T[] data;
	
	@SuppressWarnings("unchecked")
	public WorldCache(int sizeXZ, int sizeY, Supplier<T> constructor) {
		deltaXZ = (sizeXZ >> 1);
		deltaY = (sizeY >> 1);
		
		this.sizeXZ = sizeXZ;
		this.sizeY = sizeY;
		
		sizeXZ = MathUtil.getClosestPowerOfTwo(sizeXZ);
		sizeY = MathUtil.getClosestPowerOfTwo(sizeY);
		int capacity = sizeXZ * sizeXZ * sizeY;
		
		this.constructor = constructor;
		
		this.data = (T[]) new Object[capacity];
		this.center = new Vec3I();
		this.maskXZ = sizeXZ - 1;
		this.maskY = sizeY - 1;
		this.pos = new Vec3I();
		
		this.bitsXZ = MathUtil.getCeilBitIndex(sizeXZ);
		this.bitsXYZ = bitsXZ + MathUtil.getCeilBitIndex(sizeY);
		
		int index = 0;
		capacity = (deltaXZ << 1) + 1;
		capacity *= capacity;
		capacity *= (deltaY << 1) + 1;
		
		this.updateOrder = new Vec3I[capacity];
		for (int x = -deltaXZ; x <= deltaXZ; x++) {
			for (int z = -deltaXZ; z <= deltaXZ; z++) {
				for (int y = -deltaY; y <= deltaY; y++) {
					this.updateOrder[index++] = new Vec3I(x, y, z);
				}
			}
		}
		Arrays.sort(this.updateOrder, Comparator.comparingInt(Vec3I::lengthSqr));
	}
	
	public int getSizeXZ() {
		return sizeXZ;
	}
	
	public int getSizeY() {
		return sizeY;
	}
	
	public void setCenter(int x, int y, int z) {
		center.set(x, y, z);
	}
	
	public T get(Vec3I pos) {
		return data[getIndex(pos)];
	}
	
	public T getOrCreate(Vec3I pos) {
		int index = getIndex(pos);
		if (data[index] == null) data[index] = constructor.get();
		return data[index];
	}
	
	public void forEach(BiConsumer<Vec3I, T> processor) {
		forEach(processor, false);
	}
	
	public void forEach(BiConsumer<Vec3I, T> processor, boolean reversed) {
		if (reversed) {
			for (int i = updateOrder.length - 1; i >= 0; i--) {
				pos.set(center).add(this.updateOrder[i]);
				int index = getIndex(pos);
				if (data[index] == null) continue;
				processor.accept(pos, data[index]);
			}
		}
		else {
			for (Vec3I delta : this.updateOrder) {
				pos.set(center).add(delta);
				int index = getIndex(pos);
				if (data[index] == null) continue;
				processor.accept(pos, data[index]);
			}
		}
	}
	
	public void forEach(Consumer<T> processor) {
		for (int i = 0; i < data.length; i++) {
			if (data[i] == null) continue;
			processor.accept(data[i]);
		}
	}
	
	private int getIndex(Vec3I pos) {
		return (pos.x & maskXZ) << bitsXYZ | (pos.y & maskY) << bitsXZ | (pos.z & maskXZ);
	}
	
	public boolean isInside(Vec3I pos) {
		int offset = pos.x - center.x;
		if (offset < -deltaXZ || offset > deltaXZ) return false;
		
		offset = pos.z - center.z;
		if (offset < -deltaXZ || offset > deltaXZ) return false;
		
		offset = pos.y - center.y;
		return offset >= -deltaY && offset <= deltaY;
	}
}
