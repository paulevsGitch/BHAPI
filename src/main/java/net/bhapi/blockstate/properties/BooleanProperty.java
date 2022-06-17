package net.bhapi.blockstate.properties;

import java.util.List;

public class BooleanProperty extends StateProperty<Boolean> {
	private static final List<Boolean> VALUES = List.of(Boolean.FALSE, Boolean.TRUE);
	private static final String TYPE = "boolean";
	
	public BooleanProperty(String name) {
		super(name);
	}
	
	@Override
	public Boolean parseValue(String value) {
		return Boolean.parseBoolean(value);
	}
	
	@Override
	public List<Boolean> getValues() {
		return VALUES;
	}
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public Boolean defaultValue() {
		return Boolean.FALSE;
	}
	
	@Override
	public int getCount() {
		return 2;
	}
	
	@Override
	public int getIndex(Boolean value) {
		return value.booleanValue() ? 1 : 0;
	}
}
