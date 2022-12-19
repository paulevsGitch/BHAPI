package net.bhapi.level.updaters;

import net.bhapi.BHAPI;
import net.bhapi.client.BHAPIClient;
import net.bhapi.config.BHConfigs;
import net.bhapi.util.ThreadManager;
import net.bhapi.util.ThreadManager.RunnableThread;
import net.minecraft.level.Level;

import java.util.Locale;

public abstract class ThreadedUpdater {
	protected final boolean useThreads;
	protected final boolean isClient;
	protected final boolean noDelay;
	
	private RunnableThread updatingThread;
	private boolean isEmpty = true;
	protected final Level level;
	private final String name;
	private long time;
	
	public ThreadedUpdater(String name, Level level) {
		this(name, level, false);
	}
	
	public ThreadedUpdater(String name, Level level, boolean noDelay) {
		this.noDelay = noDelay;
		if (name.endsWith("_")) this.name = name + level.dimension.id;
		else this.name = name + "_" + level.dimension.id;
		useThreads = BHConfigs.GENERAL.getBool("multithreading.useThreads", true);
		BHConfigs.GENERAL.save();
		this.level = level;
		this.isClient = BHAPI.isClient();
	}
	
	public void process() {
		if (useThreads) {
			if (updatingThread == null) {
				BHAPI.log("Start thread: " + name);
				updatingThread = ThreadManager.makeThread(name, this::checkedUpdate);
				time = System.currentTimeMillis();
				if (!updatingThread.isAlive()) updatingThread.start();
			}
		}
		else update();
	}
	
	private void checkedUpdate() {
		if (canUpdate()) {
			update();
		}
		check();
		delay();
	}
	
	protected abstract void update();
	
	protected void onFinish() {}
	
	private void delay() {
		if (!useThreads || noDelay) return;
		long currentTime = System.currentTimeMillis();
		int delta = (int) (currentTime - time);
		time = currentTime;
		if (delta < 50) delay(delta);
		else if (delta > 100) warning(delta);
	}
	
	private boolean canUpdate() {
		return !isClient || !BHAPIClient.getMinecraft().paused;
	}
	
	private void check() {
		if (useThreads) {
			if (updatingThread != null && !updatingThread.isAlive()) {
				BHAPI.log("Thread " + name + " is inactive, marked to restart");
				ThreadManager.remove(name);
				updatingThread = null;
			}
			else if (isClient) {
				boolean empty = BHAPIClient.getMinecraft().viewEntity == null;
				if (!isEmpty && empty) {
					BHAPI.log("Stop thread: " + name);
					ThreadManager.stopThread(updatingThread);
					updatingThread = null;
					onFinish();
				}
				isEmpty = empty;
			}
		}
	}
	
	private void delay(int delta) {
		delta = 50 - delta;
		try {
			Thread.sleep(delta);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void warning(int delta) {
		StringBuilder builder = new StringBuilder("Update in thread ");
		builder.append(Thread.currentThread().getName());
		builder.append(" take ");
		builder.append(delta);
		builder.append("ms instead of 50 (");
		builder.append(String.format(Locale.ROOT, "%.1f", (float) delta / 50F));
		builder.append(" ticks)");
		BHAPI.warn(builder.toString());
	}
}
