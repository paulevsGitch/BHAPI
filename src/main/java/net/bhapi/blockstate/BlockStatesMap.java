package net.bhapi.blockstate;

import net.bhapi.BHAPI;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;
import net.minecraft.util.io.StringTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockStatesMap {
	private static final Map<BlockState, Integer> ID_MAP = new HashMap<>();
	private static final List<BlockState> LOADING_LIST = new ArrayList<>();
	private static final List<BlockState> STATE_LIST = new ArrayList<>();
	private static final String KEY = "states";
	
	public static void addState(BlockState state) {
		if (ID_MAP.containsKey(state)) return;
		int index = STATE_LIST.size();
		STATE_LIST.add(state);
		ID_MAP.put(state, index);
	}
	
	public static int getRawID(BlockState state) {
		return ID_MAP.getOrDefault(state, -1);
	}
	
	public static BlockState getState(int rawID) {
		return rawID < 0 || rawID > STATE_LIST.size() ? null : STATE_LIST.get(rawID);
	}
	
	public static boolean saveData(CompoundTag tag) {
		if (STATE_LIST.isEmpty()) return false;
		ListTag list = new ListTag();
		STATE_LIST.stream().filter(state -> state != null).forEach(state -> list.add(new StringTag(state.toNBTString())));
		tag.put(KEY, list);
		return true;
	}
	
	public static void loadData(CompoundTag tag) {
		LOADING_LIST.clear();
		
		ListTag list = tag.getListTag(KEY);
		int size = list.size();
		for (int i = 0; i < size; i++) {
			BlockState state = BlockState.fromNBTString(list.get(i).toString());
			LOADING_LIST.add(state);
		}
		
		STATE_LIST.clear();
		ID_MAP.clear();
		
		size = LOADING_LIST.size();
		STATE_LIST.addAll(LOADING_LIST);
		for (int i = 0; i < size; i++) {
			BlockState state = LOADING_LIST.get(i);
			if (state != null) {
				ID_MAP.put(state, i);
			}
		}
		
		System.out.println(LOADING_LIST.size());
		System.out.println(STATE_LIST.size());
		System.out.println(ID_MAP.size());
	}
}
