package net.bhapi.level.updaters;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LevelBlocksUpdater extends ThreadedUpdater {
	private final Set<UpdateInfo> updateRequests = new HashSet<>();
	private final List<UpdateInfo> updateInfos = new ArrayList<>();
	private final Vec3I pos2 = new Vec3I();
	
	@Environment(EnvType.CLIENT)
	private final List<Vec3I> updateAreas = new ArrayList<>();
	
	public LevelBlocksUpdater(Level level) {
		super("neighbours_updater_", level);
	}
	
	public void add(Vec3I pos, BlockDirection facing) {
		synchronized (updateRequests) {
			updateRequests.add(new UpdateInfo(pos, facing));
		}
	}
	
	@Override
	protected void update() {
		if (updateInfos.isEmpty()) {
			synchronized (updateRequests) {
				updateInfos.addAll(updateRequests);
				updateRequests.clear();
			}
		}
		
		if (isClient) {
			synchronized (updateAreas) {
				byte count = (byte) Math.min(64, updateAreas.size());
				for (short i = 0; i < count; i++) {
					Vec3I pos = updateAreas.get(0);
					updateAreas.remove(0);
					level.updateBlock(pos.x, pos.y, pos.z);
				}
			}
		}
		
		BlockStateProvider provider = BlockStateProvider.cast(level);
		short count = (short) Math.min(1024, updateInfos.size());
		for (short i = 0; i < count; i++) {
			UpdateInfo info = updateInfos.get(i);
			updateInfos.remove(i--);
			count--;
			Vec3I pos = info.pos();
			BlockDirection facing = info.facing();
			BlockState a = provider.bhapi_getBlockState(pos);
			if (a.isAir()) continue;
			BlockState b = provider.bhapi_getBlockState(pos2.set(pos).move(facing));
			a.onNeighbourBlockUpdate(level, pos.x, pos.y, pos.z, facing, b);
			if (isClient) {
				synchronized (updateAreas) {
					updateAreas.add(pos.set(pos.x >> 4 | 8, pos.y >> 4 | 8, pos.z >> 4 | 8));
				}
			}
		}
	}
	
	@Override
	protected void onFinish() {
		synchronized (updateInfos) {
			updateRequests.clear();
			updateAreas.clear();
			updateInfos.clear();
		}
	}
	
	private record UpdateInfo(Vec3I pos, BlockDirection facing) {}
}
