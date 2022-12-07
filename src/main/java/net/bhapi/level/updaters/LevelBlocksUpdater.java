package net.bhapi.level.updaters;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.Level;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class LevelBlocksUpdater extends ThreadedUpdater {
	private final Set<UpdateInfo> updateRequests = new HashSet<>();
	private final Set<UpdateInfo> updateInfos = new HashSet<>();
	private final Vec3I pos2 = new Vec3I();
	
	@Environment(EnvType.CLIENT)
	private final Set<Vec3I> updateAreas = new HashSet<>();
	
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
		synchronized (updateRequests) {
			updateInfos.addAll(updateRequests);
			updateRequests.clear();
		}
		
		if (BHAPI.isClient()) {
			synchronized (updateAreas) {
				Iterator<Vec3I> iterator = updateAreas.iterator();
				for (short i = 0; i < 64 && iterator.hasNext(); i++) {
					Vec3I pos = iterator.next();
					iterator.remove();
					level.callAreaEvents(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z);
				}
			}
		}
		
		BlockStateProvider provider = BlockStateProvider.cast(level);
		Iterator<UpdateInfo> iterator = updateInfos.iterator();
		for (short i = 0; i < 1024 && iterator.hasNext(); i++) {
			UpdateInfo info = iterator.next();
			iterator.remove();
			Vec3I pos = info.pos();
			BlockDirection facing = info.facing();
			BlockState a = provider.getBlockState(pos);
			BlockState b = provider.getBlockState(pos2.set(pos).move(facing));
			a.onNeighbourBlockUpdate(level, pos.x, pos.y, pos.z, facing, b);
			if (BHAPI.isClient()) {
				synchronized (updateAreas) {
					updateAreas.add(pos.set(pos.x >> 4 | 8, pos.y >> 4 | 8, pos.z >> 4 | 8));
				}
			}
		}
	}
	
	@Override
	protected void onFinish() {
		synchronized (updateInfos) {
			updateInfos.clear();
		}
	}
	
	private record UpdateInfo(Vec3I pos, BlockDirection facing) {}
}
