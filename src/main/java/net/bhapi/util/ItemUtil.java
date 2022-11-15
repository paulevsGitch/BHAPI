package net.bhapi.util;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.properties.BlockPropertyType;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.item.ItemProvider;
import net.bhapi.registry.CommonRegistries;
import net.fabricmc.tinyremapper.extension.mixin.common.data.Pair;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {
	private static final List<Pair<BlockState, ItemStack>> POST_PROCESSING_STACKS = new ArrayList<>();
	public static final int MOD_ITEM_ID = 31743;
	private static boolean isFrozen;
	
	public static void init() {}
	
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
	
	public static ItemStack makeStack(Identifier id) {
		return makeStack(id, 1, 0);
	}
	
	public static ItemStack makeStack(Identifier id, int count) {
		return makeStack(id, count, 0);
	}
	
	public static ItemStack makeStack(Identifier id, int count, int damage) {
		BaseItem item = CommonRegistries.ITEM_REGISTRY.get(id);
		if (item == null) {
			BHAPI.warn("No item " + id + " in registry");
			return null;
		}
		return new ItemStack(item, count, damage);
	}
}
