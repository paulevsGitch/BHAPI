package net.bhapi.registry;

import net.bhapi.BHAPI;
import net.bhapi.interfaces.IDProvider;
import net.bhapi.storage.ExpandableArray;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;
import net.minecraft.util.io.NBTIO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SerialisationMap<T> {
	private static final String KEY_ID = "rawID";
	
	private final Map<Integer, T> loadingObjects = new HashMap<>();
	private final ExpandableArray<T> idToOBJ = new ExpandableArray<>();
	private final Map<T, Integer> objToID = new HashMap<>();
	private final Function<CompoundTag, T> deserializer;
	private final Function<T, CompoundTag> serializer;
	private byte[] compressedData;
	private final String dataKey;
	private boolean requireSave;
	private boolean isLoading;
	private int globalIndex;
	
	public SerialisationMap(String dataKey, Function<T, CompoundTag> serializer, Function<CompoundTag, T> deserializer) {
		this.deserializer = deserializer;
		this.serializer = serializer;
		this.dataKey = dataKey;
	}
	
	public void add(T obj) {
		if (isLoading) return;
		if (objToID.containsKey(obj)) return;
		int index = idToOBJ.getFreeID();
		idToOBJ.put(index, obj);
		objToID.put(obj, index);
		requireSave = true;
		if (obj instanceof IDProvider) {
			((IDProvider) obj).setID(index);
		}
	}
	
	public T get(int rawID) {
		return idToOBJ.get(rawID);
	}
	
	public int getID(T obj) {
		if (obj instanceof IDProvider) return ((IDProvider) obj).getID();
		return objToID.getOrDefault(obj, -1);
	}
	
	private int getFreeID(Map<Integer, T> map) {
		while (map.containsKey(globalIndex)) globalIndex++;
		return globalIndex;
	}
	
	public boolean save(CompoundTag tag) {
		if (!requireSave || objToID.isEmpty()) return false;
		
		ListTag list = new ListTag();
		tag.put(dataKey, list);
		
		idToOBJ.forEach((rawID, obj) -> {
			CompoundTag entry = serializer.apply(obj);
			entry.put(KEY_ID, rawID);
			list.add(entry);
		});
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		NBTIO.writeGzipped(tag, stream);
		compressedData = stream.toByteArray();
		try {
			stream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		requireSave = false;
		return true;
	}
	
	public void load(CompoundTag tag) {
		isLoading = true;
		
		ListTag list = tag.getListTag(dataKey);
		int size = list.size();
		for (int i = 0; i < size; i++) {
			CompoundTag entry = (CompoundTag) list.get(i);
			T obj = deserializer.apply(entry);
			int rawID = entry.getInt(KEY_ID);
			
			if (obj == null) {
				BHAPI.warn("Object " + rawID + " is null! Skipping");
				continue;
			}
			
			if (loadingObjects.containsKey(rawID)) {
				StringBuilder builder = new StringBuilder("Object [");
				builder.append(loadingObjects.get(rawID));
				builder.append("] and [");
				builder.append(obj);
				builder.append("] have identical rawID: ");
				builder.append(rawID);
				throw new RuntimeException(builder.toString());
			}
			loadingObjects.put(rawID, obj);
		}
		
		globalIndex = 0;
		idToOBJ.forEach((rawID, obj) -> {
			if (!loadingObjects.containsValue(obj)) {
				int newID = getFreeID(loadingObjects);
				loadingObjects.put(newID, obj);
			}
		});
		
		idToOBJ.clear();
		objToID.clear();
		
		idToOBJ.putAll(loadingObjects);
		idToOBJ.forEach((rawID, obj) -> {
			objToID.put(obj, rawID);
			if (obj instanceof IDProvider) {
				((IDProvider) obj).setID(rawID);
			}
		});
		loadingObjects.clear();
		
		requireSave = true;
		isLoading = false;
	}
	
	public byte[] getCompressedData() {
		return compressedData;
	}
	
	public void readFromData(byte[] data) {
		isLoading = true;
		this.compressedData = data;
		ByteArrayInputStream stream = new ByteArrayInputStream(data);
		CompoundTag tag = NBTIO.readGzipped(stream);
		load(tag);
		requireSave = false;
		isLoading = false;
	}
}
