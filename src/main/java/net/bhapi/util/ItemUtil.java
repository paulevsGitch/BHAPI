package net.bhapi.util;

import net.bhapi.storage.ExpandableArray;
import net.minecraft.item.BaseItem;

import java.util.Arrays;
import java.util.Objects;

public class ItemUtil {
	private static final ExpandableArray<BaseItem> LEGACY_ITEMS = new ExpandableArray<>();
	public static final int MOD_ITEM_ID = 31743;
	private static boolean isFrozen;
	
	public static void init() {
	
	}
	
	public static BaseItem getLegacyItem(int id) {
		return LEGACY_ITEMS.get(id);
	}
	
	public static void setFrozen(boolean frozen) {
		isFrozen = frozen;
	}
	
	public static void checkFrozen() {
		if (isFrozen) {
			throw new RuntimeException("Item was initiated after registry was formed");
		}
	}
	
	static {
		Arrays.stream(BaseItem.byId).filter(Objects::nonNull).forEach(item -> {
			int id = item.id;
			LEGACY_ITEMS.put(id, item);
		});
	}
}
