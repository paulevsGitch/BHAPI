package net.bhapi.util;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.properties.BlockPropertyType;
import net.bhapi.blockstate.properties.IntegerProperty;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.item.ItemProvider;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.storage.ExpandableArray;
import net.fabricmc.tinyremapper.extension.mixin.common.data.Pair;
import net.minecraft.block.BaseBlock;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ItemUtil {
	private static final List<Pair<BlockState, ItemStack>> POST_PROCESSING_STACKS = new ArrayList<>();
	private static final ExpandableArray<BaseItem> LEGACY_ITEMS = new ExpandableArray<>();
	public static final int MOD_ITEM_ID = 31743;
	private static boolean isFrozen;
	
	public static void init() {}
	
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
	
	public static void addStackForPostProcessing(BlockState state, ItemStack stack) {
		POST_PROCESSING_STACKS.add(Pair.of(state, stack));
	}
	
	public static void postProcessStacks() {
		POST_PROCESSING_STACKS.forEach(pair -> {
			BlockState state = pair.first();
			ItemStack stack = pair.second();
			Identifier id = CommonRegistries.BLOCK_REGISTRY.getID(state.getBlock());
			Identifier id2 = null;
			StateProperty<?> meta = state.getProperty("meta");
			if (meta != null && meta.getType() == BlockPropertyType.INTEGER) {
				int m = (int) state.getValue(meta);
				if (m > 0) {
					id2 = Identifier.make(id.getModID(), id.getName() + "_" + m);
				}
			}
			BaseItem item = null;
			if (id2 != null) item = CommonRegistries.ITEM_REGISTRY.get(id);
			if (item == null && id != null) item = CommonRegistries.ITEM_REGISTRY.get(id);
			if (item != null) ItemProvider.cast(stack).setItem(item);
		});
	}
	
	static {
		Arrays.stream(BaseItem.byId).filter(Objects::nonNull).forEach(item -> {
			int id = item.id;
			LEGACY_ITEMS.put(id, item);
		});
	}
}
