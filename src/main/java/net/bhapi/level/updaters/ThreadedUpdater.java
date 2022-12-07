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
	
	private RunnableThread updatingThread;
	private boolean isEmpty = true;
	protected final Level level;
	private final String name;
	private long time;
	
	public ThreadedUpdater(String name, Level level) {
		if (name.endsWith("_")) this.name = name;
		else this.name = name + "_";
		useThreads = BHConfigs.GENERAL.getBool("multithreading.useThreads", true);
		BHConfigs.GENERAL.save();
		this.level = level;
		this.isClient = BHAPI.isClient();
	}
	
	public void process() {
		if (useThreads) {
			if (updatingThread == null) {
				updatingThread = ThreadManager.makeThread(name + level.dimension.id, this::checkedUpdate);
				time = System.currentTimeMillis();
				if (!updatingThread.isAlive()) updatingThread.start();
			}
		}
		else update();
	}
	
	private void checkedUpdate() {
		if (canUpdate()) {
			update();
			check();
		}
		delay();
	}
	
	protected abstract void update();
	
	protected void onFinish() {}
	
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
		else if (delta > 100) {
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
	
	private boolean canUpdate() {
		return !isClient || !BHAPIClient.getMinecraft().paused;
	}
	
	private void check() {
		if (useThreads && isClient) {
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
