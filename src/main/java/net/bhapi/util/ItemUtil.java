package net.bhapi.util;

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
	private static final List<Pair<BaseBlock, ItemStack>> POST_PROCESSING_STACKS = new ArrayList<>();
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
	
	public static void addStackForPostProcessing(BaseBlock block, ItemStack stack) {
		POST_PROCESSING_STACKS.add(Pair.of(block, stack));
	}
	
	public static void postProcessStacks() {
		POST_PROCESSING_STACKS.forEach(pair -> {
			Identifier id = CommonRegistries.BLOCK_REGISTRY.getID(pair.first());
			System.out.println("Processing " + id);
			if (id != null) ItemProvider.cast(pair.second()).setItem(CommonRegistries.ITEM_REGISTRY.get(id));
		});
	}
	
	static {
		Arrays.stream(BaseItem.byId).filter(Objects::nonNull).forEach(item -> {
			int id = item.id;
			LEGACY_ITEMS.put(id, item);
		});
	}
}
