package net.bhapi.mixin.common;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
	@Shadow public ItemStack[] main;
	
	@Shadow public abstract int getMaxItemCount();
	
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
}
