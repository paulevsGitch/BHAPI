package net.bhapi.util;

import java.util.HashMap;
import java.util.Map;

public class Identifier {
	private static final Map<String, Map<String, Identifier>> CACHE = new HashMap<>();
	private static final String MINECRAFT = "minecraft";
	private final String modID;
	private final String name;
	private final int hash;
	
	private Identifier(String modID, String name) {
		this.modID = modID;
		this.name = name;
		this.hash = toString().hashCode();
	}
	
	public String getModID() {
		return modID;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(modID);
		builder.append(':');
		builder.append(name);
		return builder.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || !(obj instanceof Identifier)) return false;
		Identifier id = (Identifier) obj;
		return modID.equals(id.modID) && name.equals(id.name);
	}
	
	@Override
	public int hashCode() {
		return this.hash;
	}
	
	/**
	 * Make a new {@link Identifier}.
	 * If similar identifier was already created - will return already existing instance from cache.
	 * @param modID {@link String} mod id
	 * @param name {@link String} name
	 * @return {@link Identifier}
	 */
	public static Identifier make(String modID, String name) {
		return CACHE.computeIfAbsent(modID, key -> new HashMap<>()).computeIfAbsent(name, key -> new Identifier(modID, name));
	}
	
	/**
	 * Make a new {@link Identifier} with default minecraft namespace or from string value with colon separator.
	 * If similar identifier was already created - will return already existing instance from cache.
	 * @param name {@link String} name
	 * @return {@link Identifier}
	 */
	public static Identifier make(String name) {
		int index = name.indexOf(':');
		return index < 0 ? make(MINECRAFT, name) : make(name.substring(0, index), name.substring(index + 1));
	}
}
