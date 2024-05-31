package net.bhapi.mixin.common;

import net.minecraft.container.Container;
import net.minecraft.container.slot.Slot;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Container.class)
public abstract class BaseContainerMixin {
	@SuppressWarnings("rawtypes")
	@Shadow public List slots;
	
	@Shadow public abstract ItemStack transferSlot(int i);
	@Shadow public abstract ItemStack clickSlot(int i, int j, boolean bl, PlayerEntity arg);
	
	@Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
	private void bhapi_clickSlot(int slotIndex, int useStackCount, boolean flag, PlayerEntity player, CallbackInfoReturnable<ItemStack> info) {
		ItemStack stack = null;
		
		if (useStackCount == 0 || useStackCount == 1) {
			PlayerInventory inventory = player.inventory;
			
			if (slotIndex == -999) {
				if (inventory.getCursorItem() != null) {
					if (useStackCount == 0) {
						player.dropItem(inventory.getCursorItem());
						inventory.setCursorItem(null);
					}
					if (useStackCount == 1) {
						player.dropItem(inventory.getCursorItem().split(1));
						if (inventory.getCursorItem().count == 0) {
							inventory.setCursorItem(null);
						}
					}
				}
			}
			else if (flag) {
				ItemStack slotStack = this.transferSlot(slotIndex);
				if (slotStack != null) {
					int count = slotStack.count;
					stack = slotStack.copy();
					Slot slot = (Slot) this.slots.get(slotIndex);
					if (slot != null && slot.getItem() != null && slot.getItem().count < count) {
						this.clickSlot(slotIndex, useStackCount, flag, player);
					}
				}
			}
			else {
				Slot slot = (Slot) this.slots.get(slotIndex);
				if (slot != null) {
					slot.markDirty();
					ItemStack slotStack = slot.getItem();
					ItemStack cursorStack = inventory.getCursorItem();
					
					if (slotStack != null) {
						stack = slotStack.copy();
					}
					
					int split;
					if (slotStack == null) {
						if (cursorStack != null && slot.canInsert(cursorStack)) {
							int count = useStackCount == 0 ? cursorStack.count : 1;
							if (count > slot.getMaxStackCount()) {
								count = slot.getMaxStackCount();
							}
							slot.setStack(cursorStack.split(count));
							if (cursorStack.count == 0) {
								inventory.setCursorItem(null);
							}
						}
					}
					else if (cursorStack == null) {
						int count = useStackCount == 0 ? slotStack.count : (slotStack.count + 1) / 2;
						inventory.setCursorItem(slot.takeItem(count));
						if (slotStack.count == 0) {
							slot.setStack(null);
						}
						slot.onCrafted(inventory.getCursorItem());
					}
					else if (slot.canInsert(cursorStack)) {
						if (slotStack.getType() != cursorStack.getType() || slotStack.usesMeta() && slotStack.getDamage() != cursorStack.getDamage()) {
							if (cursorStack.count <= slot.getMaxStackCount()) {
								slot.setStack(cursorStack);
								inventory.setCursorItem(slotStack);
							}
						}
						else {
							int count = useStackCount == 0 ? cursorStack.count : 1;
							if (count > slot.getMaxStackCount() - slotStack.count) {
								count = slot.getMaxStackCount() - slotStack.count;
							}
							if (count > cursorStack.getMaxStackSize() - slotStack.count) {
								count = cursorStack.getMaxStackSize() - slotStack.count;
							}
							cursorStack.split(count);
							if (cursorStack.count == 0) {
								inventory.setCursorItem(null);
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
						slot.onCrafted(inventory.getCursorItem());
					}
				}
			}
		}
		
		info.setReturnValue(stack);
	}
	
	@Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
	private void bhapi_insertItem(ItemStack stack, int startSlot, int endSlot, boolean flag, CallbackInfo info) {
		ItemStack itemStack;
		Slot slot;
		int slotIndex = startSlot;
		if (flag) {
			slotIndex = endSlot - 1;
		}
		if (stack.isStackable()) {
			while (stack.count > 0 && (!flag && slotIndex < endSlot || flag && slotIndex >= startSlot)) {
				slot = (Slot)this.slots.get(slotIndex);
				itemStack = slot.getItem();
				if (!(itemStack == null || itemStack.getType() != stack.getType() || stack.usesMeta() && stack.getDamage() != itemStack.getDamage())) {
					int n2 = itemStack.count + stack.count;
					if (n2 <= stack.getMaxStackSize()) {
						stack.count = 0;
						itemStack.count = n2;
						slot.markDirty();
					}
					else if (itemStack.count < stack.getMaxStackSize()) {
						stack.count -= stack.getMaxStackSize() - itemStack.count;
						itemStack.count = stack.getMaxStackSize();
						slot.markDirty();
					}
				}
				if (flag) {
					--slotIndex;
					continue;
				}
				++slotIndex;
			}
		}
		if (stack.count > 0) {
			slotIndex = flag ? endSlot - 1 : startSlot;
			while (!flag && slotIndex < endSlot || flag && slotIndex >= startSlot) {
				slot = (Slot) this.slots.get(slotIndex);
				itemStack = slot.getItem();
				if (itemStack == null) {
					slot.setStack(stack.copy());
					slot.markDirty();
					stack.count = 0;
					break;
				}
				if (flag) {
					--slotIndex;
					continue;
				}
				++slotIndex;
			}
		}
	}
}
