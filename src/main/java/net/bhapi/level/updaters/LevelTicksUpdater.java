package net.bhapi.level.updaters;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.bhapi.config.BHConfigs;
import net.bhapi.level.BHTimeInfo;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.util.ThreadManager;
import net.bhapi.util.ThreadManager.RunnableThread;
import net.minecraft.level.Level;
import net.minecraft.level.LevelProperties;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class LevelTicksUpdater {
	private final Set<BHTimeInfo> updateRequests = new HashSet<>();
	private final Set<BHTimeInfo> updateInfos = new HashSet<>();
	private final LevelProperties properties;
	private RunnableThread updatingThread;
	private final boolean useThreads;
	private boolean isEmpty = true;
	private final Random random;
	private final Level level;
	private boolean flag;
	private long time;
	
	public LevelTicksUpdater(Level level, LevelProperties properties) {
		random = new Random();
		this.level = level;
		this.properties = properties;
		useThreads = BHConfigs.GENERAL.getBool("multithreading.useThreads", true);
		BHConfigs.GENERAL.save();
	}
	
	public void addInfo(BHTimeInfo info) {
		if (useThreads) {
			synchronized (updateRequests) {
				updateRequests.add(info);
			}
		}
		else updateInfos.add(info);
	}
	
	public void update(boolean flag) {
		this.flag = flag;
		if (useThreads) {
			if (updatingThread == null) {
				updatingThread = ThreadManager.makeThread("ticks_updater_" + level.dimension.id, this::update);
				time = System.currentTimeMillis();
				if (!updatingThread.isAlive()) updatingThread.start();
			}
			synchronized (updateInfos) {
				updateInfos.addAll(updateRequests);
				updateRequests.clear();
			}
		}
		else update();
	}
	
	private void update() {
		final int side = 8;
		synchronized (updateInfos) {
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
		check();
		delay();
	}
	
	private void delay() {
		if (!useThreads) return;
		long t = System.currentTimeMillis();
		int delta = (int) (t - time);
		time = t;
		if (delta < 50) {
			delta = 50 - delta;
			try {
				Thread.sleep(delta);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void check() {
		if (useThreads && BHAPI.isClient()) {
			boolean empty = BHAPIClient.getMinecraft().viewEntity == null;
			if (!isEmpty && empty) {
				ThreadManager.stopThread(updatingThread);
				updatingThread = null;
				synchronized (updateInfos) {
					updateInfos.clear();
				}
			}
			isEmpty = empty;
		}
	}
}
