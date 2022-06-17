package net.bhapi.blockstate.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IntegerProperty extends StateProperty<Integer> {
	private static final String TYPE = "integer";
	private final List<Integer> values;
	final int minValue;
	final int maxValue;
	final int count;
	
	public IntegerProperty(String name, int minValue, int maxValue) {
		super(name);
		if (maxValue < minValue) throw new RuntimeException("Maximum is less than minimum for " + name + " property");
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.count = maxValue - minValue + 1;
		this.values = IntStream.rangeClosed(minValue, maxValue).boxed().collect(Collectors.toUnmodifiableList());
		System.out.println(values.size() + " " + count);
	}
	
	@Override
	public List<Integer> getValues() {
		return values;
	}
	
	@Override
	public String getType() {
		return TYPE;
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
		if (value < minValue || value > maxValue) {
			throw new RuntimeException(String.format("Value %d is not in range [%d - %d]", value, minValue, maxValue));
		}
		return value - minValue;
	}
	
	@Override
	public Integer parseValue(String value) {
		return Integer.parseInt(value);
	}
}
