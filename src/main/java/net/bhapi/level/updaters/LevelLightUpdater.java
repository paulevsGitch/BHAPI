package net.bhapi.level.updaters;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.level.ClientChunks;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.light.BHLightArea;
import net.bhapi.level.light.BHLightScatter;
import net.bhapi.level.light.ClientLightLevel;
import net.bhapi.storage.CircleCache;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LevelLightUpdater extends ThreadedUpdater {
	private final CircleCache<Vec3I> vectorCache = new CircleCache<>(131072);
	private final Set<BHLightArea> updateRequests = new HashSet<>(8192);
	private final List<BHLightArea> updateAreas = new ArrayList<>(8192);
	private final BHLightScatter scatter = new BHLightScatter();
	private final List<Vec3I> positions = new ArrayList<>(4096);
	private final List<Byte> lights = new ArrayList<>(4096);
	private final Vec3I blockPos = new Vec3I();
	
	@Environment(EnvType.CLIENT)
	private final Set<Vec3I> clientUpdateRequests = new HashSet<>();
	
	public LevelLightUpdater(Level level) {
		super("light_updater_", level, true);
		vectorCache.fill(Vec3I::new);
	}
	
	public void addArea(BHLightArea area) {
		synchronized (updateRequests) {
			updateRequests.add(area);
		}
	}
	
	@Override
	protected void update() {
		synchronized (updateRequests) {
			updateAreas.addAll(updateRequests);
			updateRequests.clear();
		}
		
		BlockStateProvider provider = BlockStateProvider.cast(level);
		
		/*if (updateAreas.size() > 0 && level.dimension.id == -1) {
			System.out.println(updateAreas.size());
		}*/
		
		for (BHLightArea area: updateAreas) {
			Vec3I min = area.getMinPos();
			Vec3I max = area.getMaxPos();
			byte light;
			
			if (max.equals(min)) {
				light = (byte) provider.getBlockState(min).getEmittance();
				if (light == 0) {
					light = getMaxLight(min);
					if (light > 1) {
						light = (byte) (light + 1);
						Vec3I minClean = vectorCache.get().set(min).subtract(light);
						Vec3I maxClean = vectorCache.get().set(max).add(light);
						clearLight(minClean, maxClean);
					}
				}
				else if (!canPropagate(provider, min, light)) {
					continue;
				}
			}
			
			addLights(provider, min, max);
			
			for (int j = 0; j < positions.size(); j++) {
				Vec3I pos = positions.get(j);
				light = lights.get(j);
				scatter.update(level, pos, light);
			}
			
			if (isClient) {
				markToUpdate(min, max);
			}
			
			positions.clear();
			lights.clear();
		}
		
		updateAreas.clear();
		
		if (isClient) {
			clientUpdateRequests.forEach(pos -> {
				if (ClientLightLevel.fillSection(pos)) {
					ClientChunks.update(pos);
				}
			});
			clientUpdateRequests.clear();
		}
	}
	
	private void clearLight(Vec3I min, Vec3I max) {
		for (blockPos.x = min.x; blockPos.x <= max.x; blockPos.x++) {
			for (blockPos.y = min.y; blockPos.y <= max.y; blockPos.y++) {
				for (blockPos.z = min.z; blockPos.z <= max.z; blockPos.z++) {
					clearLight(level, blockPos.x, blockPos.y, blockPos.z);
				}
			}
		}
	}
	
	private boolean isOutOfRange(byte light, int pos, int min, int max) {
		return pos + light < min || pos - light > max;
	}
	
	private void addLights(BlockStateProvider provider, Vec3I min, Vec3I max) {
		Vec3I p1 = vectorCache.get().set(min).subtract(15);
		Vec3I p2 = vectorCache.get().set(max).add(15);
		for (blockPos.x = p1.x; blockPos.x <= p2.x; blockPos.x++) {
			for (blockPos.y = p1.y; blockPos.y <= p2.y; blockPos.y++) {
				for (blockPos.z = p1.z; blockPos.z <= p2.z; blockPos.z++) {
					BlockState state = provider.getBlockState(blockPos);
					byte light = (byte) state.getEmittance();
					
					if (light == 0) continue;
					if (isOutOfRange(light, blockPos.x, min.x, max.x)) continue;
					if (isOutOfRange(light, blockPos.y, min.y, max.y)) continue;
					if (isOutOfRange(light, blockPos.z, min.z, max.z)) continue;
					
					positions.add(vectorCache.get().set(blockPos));
					lights.add(light);
				}
			}
		}
	}
	
	private byte getMaxLight(Vec3I pos) {
		byte light = (byte) getLight(level, pos.x, pos.y, pos.z);
		for (BlockDirection face: BlockDirection.VALUES) {
			blockPos.set(pos).move(face);
			byte light2 = (byte) getLight(level, blockPos.x, blockPos.y, blockPos.z);
			if (light2 > light) light = light2;
		}
		return light;
	}
	
	private void clearLight(Level level, int x, int y, int z) {
		if (isClient) ClientLightLevel.setLight(x, y, z, 0);
		else level.getChunk(x, z).setLight(LightType.BLOCK, x & 15, y, z & 15, 0);
	}
	
	private int getLight(Level level, int x, int y, int z) {
		if (isClient) return ClientLightLevel.getLight(x, y, z);
		else return level.getChunk(x, z).getLight(LightType.BLOCK, x & 15, y, z & 15);
	}
	
	@Environment(EnvType.CLIENT)
	private void markToUpdate(Vec3I min, Vec3I max) {
		int x1 = min.x >> 4;
		int y1 = min.y >> 4;
		int z1 = min.z >> 4;
		int x2 = max.x >> 4;
		int y2 = max.y >> 4;
		int z2 = max.z >> 4;
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				for (int z = z1; z <= z2; z++) {
					clientUpdateRequests.add(new Vec3I(x, y, z));
				}
			}
		}
	}
	
	private boolean canPropagate(BlockStateProvider provider, Vec3I pos, int light) {
		for (BlockDirection face: BlockDirection.VALUES) {
			blockPos.set(pos).move(face);
			BlockState state = provider.getBlockState(blockPos);
			int emittance = state.getEmittance();
			int light2 = light - state.getLightOpacity();
			if (light2 > 0 && emittance < light) return true;
		}
		return false;
	}
}
