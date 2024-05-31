package net.bhapi.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadManager {
	private static final Map<String, RunnableThread> THREADS = new HashMap<>();
	
	public static RunnableThread getThread(String name) {
		return THREADS.get(name);
	}
	
	public static RunnableThread makeThread(String name, Runnable run) {
		return THREADS.computeIfAbsent(name, n -> new RunnableThread(n, run));
	}
	
	public static void stopThread(RunnableThread thread) {
		stopThread(thread.getName());
	}
	
	@SuppressWarnings("deprecation")
	public static void stopThread(String name) {
		RunnableThread thread = THREADS.get(name);
		if (thread != null) {
			thread.stopThread();
			THREADS.remove(name);
		}
		// Kill thread if it stuck
		new Thread(() -> {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (thread != null && thread.isAlive()) {
				try {
					thread.stop();
				}
				catch (Exception ignored) {}
			}
		}).start();
	}
	
	public static void remove(String name) {
		THREADS.remove(name);
	}
	
	public static void stopAll() {
		THREADS.values().forEach(RunnableThread::stopThread);
	}
	
	public static class RunnableThread extends Thread {
		private final Runnable function;
		private boolean run = true;
		
		public RunnableThread(String name, Runnable function) {
			this.function = function;
			this.setName(name);
		}
		
		public void stopThread() {
			run = false;
		}
		
		@Override
		public void run() {
			while (run) function.run();
		}
	}
}
