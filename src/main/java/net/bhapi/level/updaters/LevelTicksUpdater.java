package net.bhapi.level.updaters;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BHTimeInfo;
import net.bhapi.level.BlockStateProvider;
import net.minecraft.level.Level;
import net.minecraft.level.LevelProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LevelTicksUpdater extends ThreadedUpdater {
	private final Set<BHTimeInfo> updateRequests = new HashSet<>();
	private final List<BHTimeInfo> updateInfos = new ArrayList<>();
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
		if (updateInfos.isEmpty()) {
			synchronized (updateRequests) {
				updateInfos.addAll(updateRequests);
				updateRequests.clear();
			}
		}
		final int side = 8;
		short count = (short) Math.min(updateInfos.size(), 1024);
		for (short i = 0; i < count; i++) {
			BHTimeInfo info = updateInfos.get(i);
			if (!flag && info.getTime() > properties.getTime()) {
				continue;
			}
			updateInfos.remove(i--);
			count--;
			int x = info.getX();
			int y = info.getY();
			int z = info.getZ();
			if (!level.isAreaLoaded(x - side, y - side, z - side, x + side, y + side, z + side)) continue;
			BlockState state = BlockStateProvider.cast(level).bhapi_getBlockState(x, y, z);
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
