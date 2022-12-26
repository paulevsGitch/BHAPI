package net.bhapi.storage;

import java.util.function.Supplier;

public class SwapCache<T> {
	//private final ReentrantLock lock;
	private final T[] data;
	private byte indexF;
	private byte indexS;
	private boolean lock;
	
	@SuppressWarnings("unchecked")
	public SwapCache() {
		//lock = new ReentrantLock();
		data = (T[]) new Object[2];
		indexS = 1;
	}
	
	public SwapCache(Supplier<T> constructor) {
		this();
		fill(constructor);
	}
	
	public void setData(int index, T data) {
		this.data[index & 1] = data;
	}
	
	public void fill(Supplier<T> constructor) {
		this.data[0] = constructor.get();
		this.data[1] = constructor.get();
	}
	
	public void swap() {
		if (lock) return;
		byte i = indexF;
		indexF = indexS;
		indexS = i;
	}
	
	public T getFirst() {
		return data[indexF];
	}
	
	public T getSecond() {
		return data[indexS];
	}
	
	public void lock() {
		//lock.lock();
		lock = true;
	}
	
	public void unlock() {
		//lock.unlock();
		lock = false;
	}
}
