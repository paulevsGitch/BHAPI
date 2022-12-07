package net.bhapi.level.updaters;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BHTimeInfo;
import net.bhapi.level.BlockStateProvider;
import net.minecraft.level.Level;
import net.minecraft.level.LevelProperties;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class LevelTicksUpdater extends ThreadedUpdater {
	private final Set<BHTimeInfo> updateRequests = new HashSet<>();
	private final Set<BHTimeInfo> updateInfos = new HashSet<>();
	private final LevelProperties properties;
	private final Random random;
	private boolean flag;
	
	public LevelTicksUpdater(Level level, LevelProperties properties) {
		super("ticks_updater_", level);
		random = new Random();
		this.properties = properties;
	}
	
	public void addInfo(BHTimeInfo info) {
		synchronized (updateRequests) {
			updateRequests.add(info);
		}
	}
	
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	@Override
	protected void update() {
		synchronized (updateRequests) {
			updateInfos.addAll(updateRequests);
			updateRequests.clear();
		}
		final int side = 8;
		Iterator<BHTimeInfo> iterator = updateInfos.iterator();
		for (short i = 0; i < 1024 && iterator.hasNext(); i++) {
			BHTimeInfo info = iterator.next();
			if (!flag && info.getTime() > properties.getTime()) {
				continue;
			}
			iterator.remove();
			int x = info.getX();
			int y = info.getY();
			int z = info.getZ();
			if (!level.isAreaLoaded(x - side, y - side, z - side, x + side, y + side, z + side)) continue;
			BlockState state = BlockStateProvider.cast(level).getBlockState(x, y, z);
			if (state != info.getState()) continue;
			state.onScheduledTick(level, x, y, z, this.random);
		}
	}
	
	@Override
	protected void onFinish() {
		synchronized (updateInfos) {
			updateRequests.clear();
			updateInfos.clear();
		}
	}
}
