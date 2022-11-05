package net.bhapi.util;

public class XorShift128 {
	private static final float DIVIDER = 16777216.0F;
	private static final int MASK = 16777215;
	private final int[] state = new int[4];
	
	public XorShift128() {
		this(System.nanoTime());
	}
	
	public XorShift128(long seed) {
		state[0] = (int) seed;
		state[1] = (int) (seed >> 32);
		seed = xorshift32(seed);
		state[2] = (int) seed;
		state[3] = (int) (seed >> 32);
	}
	
	public XorShift128(int seed1, int seed2, int seed3, int seed4) {
		setState(seed1, seed2, seed3, seed4);
	}
	
	public void setState(int seed1, int seed2, int seed3, int seed4) {
		state[0] = seed1;
		state[1] = seed2;
		state[2] = seed3;
		state[3] = seed4;
	}
	
	public int getInt() {
		return xorshift128();
	}
	
	public int getInt(int max) {
		return wrap(xorshift128(), max);
	}
	
	public int getInt(int min, int max) {
		return wrap(xorshift128(), max - min) + min;
	}
	
	public float getFloat() {
		return (float) (xorshift128() & MASK) / DIVIDER;
	}
	
	private int wrap(int x, int val) {
		int dec = val - 1;
		return (val & dec) == 0 ? x & dec : x % val;
	}
	
	private long xorshift32(long state) {
		state ^= state << 13;
		state ^= state >> 17;
		state ^= state << 5;
		return state;
	}
	
	private int xorshift128() {
		int t = state[3];
		
		final int s = state[0];
		state[3] = state[2];
		state[2] = state[1];
		state[1] = s;
		
		t ^= t << 11;
		t ^= t >> 8;
		return state[0] = t ^ s ^ (s >> 19);
	}
}
