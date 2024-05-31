package net.bhapi.client.gui;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class DebugAllItems implements Inventory {
	private final ItemStack[] items;
	
	public DebugAllItems(int size) {
		items = new ItemStack[size];
	}
	
	@Override
	public int getInventorySize() {
		return items.length;
	}
	
	@Override
	public ItemStack getItem(int i) {
		return items[i];
	}
	
	@Override
	public ItemStack takeItem(int i, int j) {
		return items[i].copy();
	}
	
	@Override
	public void setItem(int i, ItemStack arg) {}
	
	@Override
	public String getInventoryName() {
		return "All Items";
	}
	
	@Override
	public int getMaxStackSize() {
		return 64;
	}
	
	@Override
	public void markDirty() {}
	
	@Override
	public boolean canPlayerUse(PlayerEntity arg) {
		return true;
	}
	
	public ItemStack[] getItems() {
		return items;
	}
}
