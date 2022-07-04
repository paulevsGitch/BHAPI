package net.bhapi.blockstate.properties;

import java.util.List;

public abstract class StateProperty <T> {
	public abstract List<T> getValues();
	public abstract String getType();
	public abstract T defaultValue();
	public abstract int getCount();
	public abstract int getIndex(T value);
	private final String name;
	private boolean calculated;
	private int hashCode;
	
	public StateProperty(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public int getCastedIndex(Object obj) {
		return getIndex((T) obj);
	}
	
	public abstract T parseValue(String value);
	
	@Override
	public String toString() {
		return String.format("[%s:%s]", getName(), getType());
	}
	
	@Override
	public int hashCode() {
		if (!calculated) {
			hashCode = toString().hashCode();
			calculated = true;
		}
		return hashCode;
	}
}
