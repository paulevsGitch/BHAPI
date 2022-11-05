package net.bhapi.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadManager {
	private static final Map<String, Thread> THREADS = new HashMap<>();
	private static final Thread MAIN = Thread.currentThread();
	
	public static Thread getThread(String name) {
		return THREADS.get(name);
	}
	
	public static Thread makeThread(String name, Runnable run) {
		return THREADS.computeIfAbsent(name, n -> {
			Thread t = new Thread(() -> {
				while (MAIN.isAlive()) run.run();
			});
			t.setName(name);
			t.start();
			return t;
		});
	}
}
