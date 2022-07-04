package net.bhapi.registry;

import net.bhapi.BHAPI;
import net.bhapi.util.Identifier;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SerialisationMap<T> {
	private static final String KEY_OBJECT = "object";
	private static final String KEY_ID = "id";
	
	private final Map<Integer, T> loadingObjects = new HashMap<>();
	private final Map<Integer, T> idToOBJ = new HashMap<>();
	private final Map<T, Integer> objToID = new HashMap<>();
	private final Function<String, T> deserializer;
	private final Function<T, String> serializer;
	private final String dataKey;
	private int globalIndex = 0;
	private boolean requireSave;
	
	public SerialisationMap(String dataKey, Function<T, String> serializer, Function<String, T> deserializer) {
		this.deserializer = deserializer;
		this.serializer = serializer;
		this.dataKey = dataKey;
	}
	
	public void add(T obj) {
		if (objToID.containsKey(obj)) return;
		int index = getFreeID(idToOBJ);
		idToOBJ.put(index, obj);
		objToID.put(obj, index);
		requireSave = true;
	}
	
	public T get(int rawID) {
		return idToOBJ.get(rawID);
	}
	
	public int getID(T obj) {
		return objToID.getOrDefault(obj, -1);
	}
	
	private int getFreeID(Map<Integer, T> map) {
		while (map.containsKey(globalIndex)) globalIndex++;
		return globalIndex;
	}
	
	public boolean save(CompoundTag tag) {
		if (!requireSave || idToOBJ.isEmpty()) return false;
		
		ListTag list = new ListTag();
		tag.put(dataKey, list);
		
		idToOBJ.forEach((rawID, obj) -> {
			CompoundTag entry = new CompoundTag();
			entry.put(KEY_ID, rawID);
			entry.put(KEY_OBJECT, serializer.apply(obj));
			list.add(entry);
		});
		
		requireSave = false;
		return true;
	}
	
	public void load(CompoundTag tag) {
		loadingObjects.clear();
		
		ListTag list = tag.getListTag(dataKey);
		int size = list.size();
		for (int i = 0; i < size; i++) {
			CompoundTag entry = (CompoundTag) list.get(i);
			int rawID = entry.getInt(KEY_ID);
			String serialized = entry.getString(KEY_OBJECT);
			T obj = deserializer.apply(serialized);
			if (obj == null) {
				BHAPI.warn("Object " + serialized + " is null! Skipping");
				continue;
			};
			if (loadingObjects.containsKey(rawID)) {
				StringBuilder builder = new StringBuilder("Object [");
				builder.append(loadingObjects.get(rawID));
				builder.append("] and [");
				builder.append(obj);
				builder.append("] have same rawID: ");
				builder.append(rawID);
				throw new RuntimeException(builder.toString());
			}
			loadingObjects.put(rawID, obj);
		}
		
		idToOBJ.forEach((rawID, obj) -> {
			if (loadingObjects.containsKey(rawID)) {
				loadingObjects.put(getFreeID(loadingObjects), obj);
			}
			else {
				loadingObjects.put(rawID, obj);
			}
		});
		
		idToOBJ.clear();
		objToID.clear();
		
		idToOBJ.putAll(loadingObjects);
		idToOBJ.forEach((rawID, obj) -> objToID.put(obj, rawID));
		requireSave = true;
	}
}
