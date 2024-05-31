package net.bhapi.blockstate.properties;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class IntegerProperty extends StateProperty<Integer> {
	private final IntList values;
	final int minValue;
	final int maxValue;
	final int count;
	
	public IntegerProperty(String name, int minValue, int maxValue) {
		super(name);
		if (maxValue < minValue) {
			throw new RuntimeException("Maximum is less than minimum for " + name + " property");
		}
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.count = maxValue - minValue + 1;
		this.values = new IntArrayList(this.count);
		for (int i = minValue; i <= maxValue; i++) {
			this.values.add(i);
		}
	}
	
	@Override
	public IntList getValues() {
		return values;
	}
	
	@Override
	public String getType() {
		return "integer";
	}
	
	@Override
	public Integer defaultValue() {
		return minValue;
	}
	
	@Override
	public int getCount() {
		return count;
	}
	
	@Override
	public int getIndex(Integer value) {
		if (!isInRange(value)) {
			throw new RuntimeException(String.format("Value %d is not in range [%d - %d]", value, minValue, maxValue));
		}
		return value - minValue;
	}
	
	public boolean isInRange(int value) {
		return value >= minValue && value <= maxValue;
	}
}
