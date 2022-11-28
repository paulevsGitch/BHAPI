package net.bhapi.blockstate.properties;

import org.spongepowered.include.com.google.common.collect.ImmutableList;
import org.spongepowered.include.com.google.common.collect.ImmutableList.Builder;

import java.util.List;

public class EnumProperty<T extends Enum<T>> extends StateProperty<T> {
	private final List<T> values;
	
	public EnumProperty(String name, Class<T> enumClass) {
		super(name);
		if (!enumClass.isEnum()) {
			throw new RuntimeException("Class " + enumClass.getName() + " is not an enum!");
		}
		Builder<T> builder = ImmutableList.builder();
		values = builder.add(enumClass.getEnumConstants()).build();
	}
	
	@Override
	public List<T> getValues() {
		return values;
	}
	
	@Override
	public String getType() {
		return "enum";
	}
	
	@Override
	public T defaultValue() {
		return values.get(0);
	}
	
	@Override
	public int getCount() {
		return values.size();
	}
	
	@Override
	public int getIndex(T value) {
		return value.ordinal();
	}
}
