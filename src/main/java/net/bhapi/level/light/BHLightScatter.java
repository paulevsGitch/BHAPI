package net.bhapi.level.light;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.CircleCache;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BHLightScatter {
	private static final int SIDE = 14 * 2 + 1;
	private static final int SIDE2 = SIDE * SIDE;
	private static final int CAPACITY = SIDE * SIDE * SIDE;
	private static final int CENTER = getIndex(14, 14, 14);
	private static final boolean IS_CLIENT = BHAPI.isClient();
	
	private final CircleCache<Vec3I> vectorCache = new CircleCache<>(CAPACITY >> 1);
	private final CircleCache<List<Vec3I>> buffers = new CircleCache<>(2);
	private final boolean[] mask = new boolean[CAPACITY];
	private final byte[] data = new byte[CAPACITY];
	private final Vec3I blockPos = new Vec3I();
	
	public BHLightScatter() {
		buffers.fill(() -> new ArrayList<>(CAPACITY >> 2));
		vectorCache.fill(Vec3I::new);
	}
	
	public void update(Level level, Vec3I center, byte light) {
		Arrays.fill(data, (byte) 0);
		Arrays.fill(mask, false);
		
		data[CENTER] = light;
		mask[CENTER] = true;
		
		List<Vec3I> startPoints = buffers.get();
		startPoints.add(vectorCache.get().set(14, 14, 14));
		Vec3I center2 = center.clone().subtract(14);
		setLight(level, center.x, center.y, center.z, light);
		
		BlockStateProvider provider = BlockStateProvider.cast(level);
		
		for (byte i = 1; i < light; i++) {
			List<Vec3I> endPoints = startPoints;
			startPoints = buffers.get();
			endPoints.clear();
			
			startPoints.forEach(pos -> {
				int index = getIndex(pos.x, pos.y, pos.z);
				byte sideLight = (byte) (data[index] - 1);
				for (BlockDirection face: BlockDirection.VALUES) {
					Vec3I side = vectorCache.get().set(pos).move(face);
					if (isInside(side.x) && isInside(side.y) && isInside(side.z)) {
						index = getIndex(side.x, side.y, side.z);
						if (!mask[index]) {
							blockPos.set(center2).add(side);
							BlockState state = provider.getBlockState(blockPos);
							if (!checkState(state, sideLight)) continue;
							byte realLight = (byte) (sideLight - state.getLightOpacity());
							if (!checkWorld(level, blockPos, realLight)) continue;
							endPoints.add(side);
							mask[index] = true;
							data[index] = realLight;
							setLight(level, blockPos.x, blockPos.y, blockPos.z, data[index]);
						}
					}
				}
			});
			startPoints.clear();
			
			if (endPoints.isEmpty()) return;
		}
	}
	
	private boolean isInside(int value) {
		return value >= 0 && value < SIDE;
	}
	
	private static int getIndex(int x, int y, int z) {
		return x * SIDE2 + y * SIDE + z;
	}
	
	private boolean checkState(BlockState state, int light) {
		return state.getLightOpacity() < light && state.getEmittance() == 0;
	}
	
	private boolean checkWorld(Level level, Vec3I pos, int light) {
		return getLight(level, pos.x, pos.y, pos.z) <= light;
	}
	
	private void setLight(Level level, int x, int y, int z, int light) {
		if (IS_CLIENT) ClientLightLevel.setLight(x, y, z, light);
		else level.getChunk(x, z).setLight(LightType.BLOCK, x & 15, y, z & 15, light);
	}
	
	private int getLight(Level level, int x, int y, int z) {
		if (IS_CLIENT) return ClientLightLevel.getLight(x, y, z);
		else return level.getChunk(x, z).getLight(LightType.BLOCK, x & 15, y, z & 15);
	}
}
