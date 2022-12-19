package net.bhapi.storage;

import net.bhapi.client.render.level.UpdateCondition;
import net.bhapi.util.MathUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class WorldCache<T> {
	private final UpdateCondition<T> updateCondition;
	private final BiConsumer<Vec3I, T> updater;
	private final Vec3I[] updateOrder;
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
	public WorldCache(int sizeXZ, int sizeY, BiConsumer<Vec3I, T> updater, UpdateCondition<T> updateCondition) {
		this.deltaXZ = (sizeXZ >> 1);
		this.deltaY = (sizeY >> 1);
		
		this.sizeXZ = sizeXZ;
		this.sizeY = sizeY;
		
		sizeXZ = MathUtil.getClosestPowerOfTwo(sizeXZ);
		sizeY = MathUtil.getClosestPowerOfTwo(sizeY);
		int capacity = sizeXZ * sizeXZ * sizeY;
		
		this.updateCondition = updateCondition;
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
		
		int index = 0;
		capacity = (deltaXZ << 1) + 1;
		capacity *= capacity;
		capacity *= (deltaY << 1) + 1;
		this.updateOrder = new Vec3I[capacity];
		System.out.println(capacity);
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
	
	public int getCapacity() {
		return positions.length;
	}
	
	public void fill(Supplier<T> constructor) {
		for (int i = 0; i < data.length; i++) {
			data[i] = constructor.get();
		}
	}
	
	public void setCenter(int x, int y, int z) {
		center.set(x, y, z);
	}
	
	public void update(int maxUpdates) {
		int updatesCounter = 0;
		for (Vec3I delta : this.updateOrder) {
			if (updatesCounter >= maxUpdates) return;
			
			pos.set(center).add(delta);
			int index = (pos.x & maskXZ) << bitsXYZ | (pos.y & maskY) << bitsXZ | (pos.z & maskXZ);
			
			if (!positions[index].equals(pos)) {
				positions[index].set(pos);
				updater.accept(pos, data[index]);
				updatesCounter++;
			}
			else if (updateCondition.needUpdate(pos, data[index])) {
				updater.accept(pos, data[index]);
				updatesCounter++;
			}
		}
	}
	
	public T get(Vec3I pos) {
		int index = (pos.x & maskXZ) << bitsXYZ | (pos.y & maskY) << bitsXZ | (pos.z & maskXZ);
		return data[index];
	}
	
	public void forEach(BiConsumer<Vec3I, T> processor) {
		for (Vec3I delta : this.updateOrder) {
			pos.set(center).add(delta);
			int index = (pos.x & maskXZ) << bitsXYZ | (pos.y & maskY) << bitsXZ | (pos.z & maskXZ);
			processor.accept(pos, data[index]);
		}
	}
}
