package net.bhapi.mixin.common;

import net.minecraft.container.ContainerBase;
import net.minecraft.container.slot.Slot;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ContainerBase.class)
public abstract class ContainerBaseMixin {
	@Shadow public List slots;
	
	@Shadow public abstract ItemStack transferSlot(int i);
	@Shadow public abstract ItemStack clickSlot(int i, int j, boolean bl, PlayerBase arg);
	
	@Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
	private void bhapi_clickSlot(int i, int j, boolean flag, PlayerBase player, CallbackInfoReturnable<ItemStack> info) {
		ItemStack stack = null;
		if (j == 0 || j == 1) {
			PlayerInventory playerInventory = player.inventory;
			
			if (i == -999) {
				if (playerInventory.getCursorItem() != null) {
					if (j == 0) {
						player.dropItem(playerInventory.getCursorItem());
						playerInventory.setCursorItem(null);
					}
					if (j == 1) {
						player.dropItem(playerInventory.getCursorItem().split(1));
						if (playerInventory.getCursorItem().count == 0) {
							playerInventory.setCursorItem(null);
						}
					}
				}
			}
			else if (flag) {
				ItemStack slotStack = this.transferSlot(i);
				if (slotStack != null) {
					int count = slotStack.count;
					stack = slotStack.copy();
					Slot slot = (Slot) this.slots.get(i);
					if (slot != null && slot.getItem() != null && slot.getItem().count < count) {
						this.clickSlot(i, j, flag, player);
					}
				}
			}
			else {
				Slot slot = (Slot) this.slots.get(i);
				if (slot != null) {
					slot.markDirty();
					ItemStack slotStack = slot.getItem();
					ItemStack cursorStack = playerInventory.getCursorItem();
					
					if (slotStack != null) {
						stack = slotStack.copy();
					}
					
					int split;
					if (slotStack == null) {
						if (cursorStack != null && slot.canInsert(cursorStack)) {
							int count = j == 0 ? cursorStack.count : 1;
							if (count > slot.getMaxStackCount()) {
								count = slot.getMaxStackCount();
							}
							slot.setStack(cursorStack.split(count));
							if (cursorStack.count == 0) {
								playerInventory.setCursorItem(null);
							}
						}
					}
					else if (cursorStack == null) {
						int n5 = j == 0 ? slotStack.count : (slotStack.count + 1) / 2;
						ItemStack itemStack5 = slot.takeItem(n5);
						playerInventory.setCursorItem(itemStack5);
						if (slotStack.count == 0) {
							slot.setStack(null);
						}
						slot.onCrafted(playerInventory.getCursorItem());
					}
					else if (slot.canInsert(cursorStack)) {
						if (slotStack.getType() != cursorStack.getType() || slotStack.usesMeta() && slotStack.getDamage() != cursorStack.getDamage()) {
							if (cursorStack.count <= slot.getMaxStackCount()) {
								slot.setStack(cursorStack);
								playerInventory.setCursorItem(slotStack);
							}
						}
						else {
							int count = j == 0 ? cursorStack.count : 1;
							if (count > slot.getMaxStackCount() - slotStack.count) {
								count = slot.getMaxStackCount() - slotStack.count;
							}
							if (count > cursorStack.getMaxStackSize() - slotStack.count) {
								count = cursorStack.getMaxStackSize() - slotStack.count;
							}
							cursorStack.split(count);
							if (cursorStack.count == 0) {
								playerInventory.setCursorItem(null);
							}
							slotStack.count += count;
						}
					}
					else if (!(slotStack.getType() != cursorStack.getType() || cursorStack.getMaxStackSize() <= 1 || slotStack.usesMeta() && slotStack.getDamage() != cursorStack.getDamage() || (split = slotStack.count) <= 0 || split + cursorStack.count > cursorStack.getMaxStackSize())) {
						cursorStack.count += split;
						slotStack.split(split);
						if (slotStack.count == 0) {
							slot.setStack(null);
						}
						slot.onCrafted(playerInventory.getCursorItem());
					}
				}
			}
		}
		
		info.setReturnValue(stack);
	}
}
