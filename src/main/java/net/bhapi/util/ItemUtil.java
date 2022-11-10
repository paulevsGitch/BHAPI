package net.bhapi.util;

import net.bhapi.blockstate.BlockState;
import net.bhapi.storage.ExpandableArray;
import net.bhapi.util.BlockUtil.BlockInfo;
import net.minecraft.block.BaseBlock;
import net.minecraft.item.BaseItem;

import java.util.Arrays;
import java.util.Objects;

public class ItemUtil {
	private static final ExpandableArray<BaseItem> LEGACY_ITEMS = new ExpandableArray<>();
	public static final int MOD_ITEM_ID = 31743;
	
	public static void init() {
	
	}
	
	public static BaseItem getLegacyItem(int id) {
		return LEGACY_ITEMS.get(id);
	}
	
	static {
		Arrays.stream(BaseItem.byId).filter(Objects::nonNull).forEach(item -> {
			int id = item.id;
			LEGACY_ITEMS.put(id, item);
		});
	}
}
