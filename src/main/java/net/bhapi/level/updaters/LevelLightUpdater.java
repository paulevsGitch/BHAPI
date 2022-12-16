package net.bhapi.level.updaters;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.AreaRenderers;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.light.BHLightArea;
import net.bhapi.level.light.VoxelLightScatter;
import net.bhapi.storage.CircleCache;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LevelLightUpdater extends ThreadedUpdater {
	private final CircleCache<Vec3I> vectorCache = new CircleCache<>(131072);
	private final Set<BHLightArea> updateRequests = new HashSet<>();
	private final Set<BHLightArea> updateAreas = new HashSet<>();
	private final VoxelLightScatter scatter = new VoxelLightScatter();
	private final List<Vec3I> positions = new ArrayList<>(4096);
	private final List<Byte> lights = new ArrayList<>(4096);
	private final Vec3I blockPos = new Vec3I();
	
	@Environment(EnvType.CLIENT)
	private final CircleCache<Vec3I> updatesCache = new CircleCache<>(4096);
	@Environment(EnvType.CLIENT)
	private final Set<Vec3I> clientUpdateRequests = new HashSet<>();
	//@Environment(EnvType.CLIENT)
	//private final Set<Vec3I> clientUpdateAreas = new HashSet<>();
	
	public LevelLightUpdater(Level level) {
		super("light_updater_", level);
		vectorCache.fill(Vec3I::new);
		if (BHAPI.isClient()) {
			updatesCache.fill(Vec3I::new);
		}
	}
	
	public void addArea(BHLightArea area) {
		synchronized (updateRequests) {
			updateRequests.add(area);
		}
	}
	
	@Override
	public void process() {
		super.process();
		if (BHAPI.isClient()) {
			clientUpdate();
		}
	}
	
	@Override
	protected void update() {
		synchronized (updateRequests) {
			updateAreas.addAll(updateRequests);
			updateRequests.clear();
		}
		
		System.out.println("Lights: " + updateAreas.size());
		BlockStateProvider provider = BlockStateProvider.cast(level);
		Iterator<BHLightArea> iterator = updateAreas.iterator();
		
		for (short i = 0; i < 64 && iterator.hasNext(); i++) {
			BHLightArea area = iterator.next();
			iterator.remove();
			
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
			
			min.subtract(15);
			max.add(15);
			
			addLights(provider, min, max);
			
			for (int j = 0; j < positions.size(); j++) {
				Vec3I pos = positions.get(j);
				light = lights.get(j);
				scatter.update(level, pos, light);
			}
			
			if (BHAPI.isClient()) {
				markToUpdate(min, max);
			}
			
			positions.clear();
			lights.clear();
		}
	}
	
	private void clearLight(Vec3I min, Vec3I max) {
		for (blockPos.x = min.x; blockPos.x <= max.x; blockPos.x++) {
			for (blockPos.y = min.y; blockPos.y <= max.y; blockPos.y++) {
				for (blockPos.z = min.z; blockPos.z <= max.z; blockPos.z++) {
					level.setLight(LightType.BLOCK, blockPos.x, blockPos.y, blockPos.z, 0);
				}
			}
		}
	}
	
	private void addLights(BlockStateProvider provider, Vec3I min, Vec3I max) {
		for (blockPos.x = min.x; blockPos.x <= max.x; blockPos.x++) {
			for (blockPos.y = min.y; blockPos.y <= max.y; blockPos.y++) {
				for (blockPos.z = min.z; blockPos.z <= max.z; blockPos.z++) {
					BlockState state = provider.getBlockState(blockPos);
					byte light = (byte) state.getEmittance();
					if (light > 0) {
						positions.add(vectorCache.get().set(blockPos));
						lights.add(light);
					}
				}
			}
		}
	}
	
	private byte getMaxLight(Vec3I pos) {
		byte light = (byte) level.getLight(LightType.BLOCK, pos.x, pos.y, pos.z);
		for (BlockDirection face: BlockDirection.VALUES) {
			blockPos.set(pos).move(face);
			byte light2 = (byte) level.getLight(LightType.BLOCK, blockPos.x, blockPos.y, blockPos.z);
			if (light2 > light) light = light2;
		}
		return light;
	}
	
	@Environment(EnvType.CLIENT)
	private void markToUpdate(Vec3I min, Vec3I max) {
		int x1 = min.x >> 4;
		int y1 = min.y >> 4;
		int z1 = min.z >> 4;
		int x2 = max.x >> 4;
		int y2 = max.y >> 4;
		int z2 = max.z >> 4;
		for (blockPos.x = x1; blockPos.x <= x2; blockPos.x++) {
			for (blockPos.y = y1; blockPos.y <= y2; blockPos.y++) {
				for (blockPos.z = z1; blockPos.z <= z2; blockPos.z++) {
					synchronized (clientUpdateRequests) {
						clientUpdateRequests.add(updatesCache.get().set(blockPos));
						//AreaRenderers.update(blockPos);
					}
				}
			}
		}
	}
	
	@Environment(EnvType.CLIENT)
	private void clientUpdate() {
		AreaRenderers.clean();
		synchronized (clientUpdateRequests) {
			//clientUpdateAreas.addAll(clientUpdateRequests);
			clientUpdateRequests.forEach(AreaRenderers::update);
			clientUpdateRequests.clear();
		}
		//clientUpdateAreas.forEach(AreaRenderers::update);
		//clientUpdateAreas.clear();
		/*Iterator<Vec3I> iterator = clientUpdateAreas.iterator();
		for (byte i = 0; i < 4 && iterator.hasNext(); i++) {
			Vec3I pos = iterator.next();
			iterator.remove();
			AreaRenderers.update(pos);
		}*/
	}
	
	private boolean canPropagate(BlockStateProvider provider, Vec3I pos, int light) {
		for (BlockDirection face: BlockDirection.VALUES) {
			blockPos.set(pos).move(face);
			int light2 = light - provider.getBlockState(blockPos).getLightOpacity();
			if (light2 > 0) return true;
		}
		return false;
	}
}
