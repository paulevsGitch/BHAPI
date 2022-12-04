package net.bhapi.level.updaters;

import net.bhapi.BHAPI;
import net.bhapi.client.BHAPIClient;
import net.bhapi.config.BHConfigs;
import net.bhapi.util.ThreadManager;
import net.bhapi.util.ThreadManager.RunnableThread;
import net.minecraft.level.Level;

public abstract class ThreadedUpdater {
	protected final boolean useThreads;
	private RunnableThread updatingThread;
	private boolean isEmpty = true;
	private final Level level;
	private final String name;
	private long time;
	
	public ThreadedUpdater(String name, Level level) {
		if (name.endsWith("_")) this.name = name;
		else this.name = name + "_";
		useThreads = BHConfigs.GENERAL.getBool("multithreading.useThreads", true);
		BHConfigs.GENERAL.save();
		this.level = level;
	}
	
	public void process() {
		if (useThreads) {
			if (updatingThread == null) {
				updatingThread = ThreadManager.makeThread(name + level.dimension.id, this::update);
				time = System.currentTimeMillis();
				if (!updatingThread.isAlive()) updatingThread.start();
			}
		}
		else update();
	}
	
	protected abstract void update();
	
	protected void onFinish() {}
	
	protected void delay() {
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
	
	protected void check() {
		if (useThreads && BHAPI.isClient()) {
			boolean empty = BHAPIClient.getMinecraft().viewEntity == null;
			if (!isEmpty && empty) {
				ThreadManager.stopThread(updatingThread);
				updatingThread = null;
				onFinish();
			}
			isEmpty = empty;
		}
	}
}
