package net.bhapi.storage;

import net.bhapi.util.MathUtil;

import java.util.Random;
import java.util.random.RandomGenerator;

public class PermutationTable implements RandomGenerator {
	private final short[] values;
	private final boolean fastWrap;
	private short increment = 1;
	private short index;
	
	public PermutationTable() {
		this(System.nanoTime(), (short) 8192, false);
	}
	
	public PermutationTable(long seed) {
		this(seed, (short) 8192, false);
	}
	
	public PermutationTable(long seed, short capacity) {
		this(seed, capacity, false);
	}
	
	public PermutationTable(long seed, short capacity, boolean randomFill) {
		Random random = new Random(seed);
		values = new short[capacity];
		for (short i = 0; i < values.length; i++) {
			values[i] = randomFill ? (short) random.nextInt(capacity) : i;
		}
		MathUtil.shuffle(values, random);
		fastWrap = MathUtil.isPowerOfTwo(capacity);
	}
	
	public void setIncrement(int increment) {
		this.increment = (short) wrap(increment);
	}
	
	public int getIncrement() {
		return increment;
	}
	
	public int getInt(int index) {
		return values[index & 255] & 255;
	}
	
	public int getInt(int x, int y) {
		return getInt(getInt(x) + y);
	}
	
	public float getInt(int x, int y, int z) {
		return getInt(getInt(x, y) + z);
	}
	
	public float getFloat(int index) {
		return getInt(index) / 255F;
	}
	
	public float getFloat(int x, int y) {
		return getFloat(getInt(x) + y);
	}
	
	public float getFloat(int x, int y, int z) {
		return getFloat(getInt(x, y) + z);
	}
	
	private int wrap(int value) {
		if (fastWrap) return value & (values.length - 1);
		else return value % (values.length);
	}
	
	public short nextShort() {
		short value = values[index];
		index = (short) wrap(index + increment);
		return value;
	}
	
	@Override
	public void nextBytes(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (nextShort() & 255);
		}
	}
	
	@Override
	public int nextInt() {
		short a = nextShort();
		short b = nextShort();
		return (int) a << 16 | (int) b;
	}
	
	@Override
	public long nextLong() {
		int a = nextInt();
		int b = nextInt();
		return (long) a << 32 | (long) b;
	}
	
	@Override
	public float nextFloat() {
		return (float) nextShort() / values.length;
	}
	
	@Override
	public int nextInt(int bound) {
		int value = (bound <= values.length) ? nextShort() : nextInt();
		return MathUtil.isPowerOfTwo(bound) ? value & (bound - 1) : value % bound;
	}
}
