package net.bhapi.mixin.common;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
	@Shadow public ItemStack[] main;
	
	@Shadow public abstract int getMaxItemCount();
	
	@Shadow protected abstract int getIdenticalStackSlot(ItemStack arg);
	
	@Shadow protected abstract int getFirstEmptySlotIndex();
	
	@Shadow public ItemStack[] armour;
	
	@Inject(method = "getIdenticalStackSlot", at = @At("HEAD"), cancellable = true)
	private void bhapi_getIdenticalStackSlot(ItemStack stack, CallbackInfoReturnable<Integer> info) {
		for (int i = 0; i < this.main.length; ++i) {
			if (
				this.main[i] == null ||
				this.main[i].getType() != stack.getType() ||
				!this.main[i].isStackable() ||
				this.main[i].count >= this.main[i].getMaxStackSize() ||
				this.main[i].count >= this.getMaxItemCount() ||
				this.main[i].usesMeta() && this.main[i].getDamage() != stack.getDamage()
			) continue;
			info.setReturnValue(i);
			return;
		}
		info.setReturnValue(-1);
	}
	
	@Inject(method = "mergeStacks", at = @At("HEAD"), cancellable = true)
	private void bhapi_mergeStacks(ItemStack stack, CallbackInfoReturnable<Integer> info) {
		int difference;
		int count = stack.count;
		int slotIndex = this.getIdenticalStackSlot(stack);
		
		if (slotIndex < 0) {
			slotIndex = this.getFirstEmptySlotIndex();
		}
		
		if (slotIndex < 0) {
			info.setReturnValue(count);
			return;
		}
		
		if (this.main[slotIndex] == null) {
			this.main[slotIndex] = new ItemStack(stack.getType(), 0, stack.getDamage());
		}
		
		if ((difference = count) > this.main[slotIndex].getMaxStackSize() - this.main[slotIndex].count) {
			difference = this.main[slotIndex].getMaxStackSize() - this.main[slotIndex].count;
		}
		
		if (difference > this.getMaxItemCount() - this.main[slotIndex].count) {
			difference = this.getMaxItemCount() - this.main[slotIndex].count;
		}
		
		if (difference == 0) {
			info.setReturnValue(count);
			return;
		}
		
		this.main[slotIndex].count += difference;
		this.main[slotIndex].cooldown = 5;
		
		info.setReturnValue(count - difference);
	}
	
	@Inject(method = "toTag", at = @At("HEAD"), cancellable = true)
	private void bhapi_toTag(ListTag list, CallbackInfoReturnable<ListTag> info) {
		CompoundTag tag;
		byte index;
		
		for (index = 0; index < this.main.length; ++index) {
			ItemStack stack = this.main[index];
			if (stack == null || stack.count == 0) continue;
			tag = new CompoundTag();
			tag.put("Slot", index);
			stack.toTag(tag);
			list.add(tag);
		}
		
		for (index = 0; index < this.armour.length; ++index) {
			ItemStack stack = this.armour[index];
			if (stack == null || stack.count == 0) continue;
			tag = new CompoundTag();
			tag.put("Slot", (byte)(index + 100));
			stack.toTag(tag);
			list.add(tag);
		}
		
		info.setReturnValue(list);
	}
	
	@Inject(method = "fromTag", at = @At("HEAD"), cancellable = true)
	private void bhapi_fromTag(ListTag list, CallbackInfo info) {
		info.cancel();
		this.main = new ItemStack[36];
		this.armour = new ItemStack[4];
		for (byte index = 0; index < list.size(); ++index) {
			CompoundTag tag = (CompoundTag) list.get(index);
			int slot = tag.getByte("Slot") & 0xFF;
			ItemStack stack = new ItemStack(tag);
			if (stack.getType() == null || stack.count == 0) continue;
			if (slot < this.main.length) this.main[slot] = stack;
			if (slot < 100 || slot >= this.armour.length + 100) continue;
			this.armour[slot - 100] = stack;
		}
	}
}
