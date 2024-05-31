package net.bhapi.util;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.item.BHBlockItem;
import net.bhapi.item.ItemProvider;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.storage.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {
	private static final List<Pair<BlockState, ItemStack>> POST_PROCESSING_STACKS = new ArrayList<>();
	public static final ItemStack EMPTY_STACK = new ItemStack(256, 0, 0);
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
	
	public static boolean isFrozen() {
		return isFrozen;
	}
	
	public static void addStackForPostProcessing(BlockState state, ItemStack stack) {
		POST_PROCESSING_STACKS.add(new Pair<>(state, stack));
	}
	
	public static void postProcessStacks() {
		POST_PROCESSING_STACKS.forEach(pair -> {
			BlockState state = pair.first();
			ItemStack stack = pair.second();
			Item item = BHBlockItem.get(state);
			if (item != null) ItemProvider.cast(stack).bhapi_setItem(item);
		});
	}
	
	public static ItemStack makeStack(Identifier id) {
		return makeStack(id, 1, 0);
	}
	
	public static ItemStack makeStack(Identifier id, int count) {
		return makeStack(id, count, 0);
	}
	
	public static ItemStack makeStack(Identifier id, int count, int damage) {
		Item item = CommonRegistries.ITEM_REGISTRY.get(id);
		if (item == null) {
			BHAPI.warn("No item " + id + " in registry");
			return null;
		}
		return new ItemStack(item, count, damage);
	}
	
	public static ItemStack makeStack(BlockState state, int count) {
		if (state == null) return EMPTY_STACK;
		BHBlockItem item = BHBlockItem.get(state);
		return item == null ? EMPTY_STACK : new ItemStack(item, count);
	}
}
