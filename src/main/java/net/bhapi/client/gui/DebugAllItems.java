package net.bhapi.client.gui;

import net.minecraft.entity.player.PlayerBase;
import net.minecraft.inventory.InventoryBase;
import net.minecraft.item.ItemStack;

public class DebugAllItems implements InventoryBase {
	private final ItemStack[] items;
	
	public DebugAllItems(int size) {
		items = new ItemStack[size];
	}
	
	@Override
	public int getInventorySize() {
		return items.length;
	}
	
	@Override
	public ItemStack getInventoryItem(int i) {
		return items[i];
	}
	
	@Override
	public ItemStack takeInventoryItem(int i, int j) {
		return items[i].copy();
	}
	
	@Override
	public void setInventoryItem(int i, ItemStack arg) {}
	
	@Override
	public String getContainerName() {
		return "All Items";
	}
	
	@Override
	public int getMaxItemCount() {
		return 64;
	}
	
	@Override
	public void markDirty() {}
	
	@Override
	public boolean canPlayerUse(PlayerBase arg) {
		return true;
	}
	
	public ItemStack[] getItems() {
		return items;
	}
}
