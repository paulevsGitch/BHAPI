package net.bhapi.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadManager {
	private static final Map<String, RunnableThread> THREADS = new HashMap<>();
	private static final Thread MAIN = Thread.currentThread();
	
	public static RunnableThread getThread(String name) {
		return THREADS.get(name);
	}
	
	public static RunnableThread makeThread(String name, Runnable run) {
		return THREADS.computeIfAbsent(name, n -> new RunnableThread(n, run));
	}
	
	public static void stopThread(RunnableThread thread) {
		thread.stopThread();
		THREADS.remove(thread);
	}
	
	public static void stopThread(String name) {
		RunnableThread thread = THREADS.get(name);
		if (thread != null) stopThread(thread);
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
			while (run && MAIN.isAlive()) function.run();
		}
	}
}
