package net.bhapi.client.gui;

import net.minecraft.container.ContainerBase;
import net.minecraft.container.slot.Slot;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.inventory.InventoryBase;
import net.minecraft.item.ItemStack;

public class WideChest extends ContainerBase {
	private InventoryBase chestInventory;
	private final int width;
	private final int rows;
	
	public WideChest(InventoryBase playerInventory, InventoryBase chestInventory, int width) {
		int col, row;
		this.chestInventory = chestInventory;
		this.rows = chestInventory.getInventorySize() / width;
		this.width = width;
		int offsetY = (this.rows - 4) * 18;
		int offset = ((width - 9) >> 1) * 18;
		for (row = 0; row < this.rows; ++row) {
			for (col = 0; col < width; ++col) {
				this.addSlot(new Slot(chestInventory, col + row * width, 8 + col * 18 - offset, 18 + row * 18));
			}
		}
		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + offsetY));
			}
		}
		for (row = 0; row < 9; ++row) {
			this.addSlot(new Slot(playerInventory, row, 8 + row * 18, 161 + offsetY));
		}
	}
	
	@Override
	public boolean canUse(PlayerBase arg) {
		return this.chestInventory.canPlayerUse(arg);
	}
	
	@Override
	public ItemStack transferSlot(int i) {
		ItemStack itemStack = null;
		Slot slot = (Slot)this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i < this.rows * width) {
				this.insertItem(itemStack2, this.rows * width, this.slots.size(), true);
			}
			else {
				this.insertItem(itemStack2, 0, this.rows * width, false);
			}
			if (itemStack2.count == 0) {
				slot.setStack(null);
			} else {
				slot.markDirty();
			}
		}
		return itemStack;
	}
}
