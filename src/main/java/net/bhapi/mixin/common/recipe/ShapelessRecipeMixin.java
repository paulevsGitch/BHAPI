package net.bhapi.mixin.common.recipe;

import net.minecraft.inventory.Crafting;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapelessRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {
	@Shadow @Final private List input;
	
	@SuppressWarnings("unchecked")
	@Inject(method = "canCraft", at = @At("HEAD"), cancellable = true)
	private void bhapi_canCraft(Crafting crafting, CallbackInfoReturnable<Boolean> info) {
		ArrayList<ItemStack> items = new ArrayList<ItemStack>(this.input);
		for (int dx = 0; dx < 3; ++dx) {
			for (int dy = 0; dy < 3; ++dy) {
				ItemStack stack = crafting.getInventoryItemXY(dy, dx);
				if (stack == null) continue;
				boolean skip = false;
				for (ItemStack inputItem : items) {
					if (stack.getType() != inputItem.getType() || inputItem.getDamage() != -1 && stack.getDamage() != inputItem.getDamage()) continue;
					skip = true;
					items.remove(inputItem);
					break;
				}
				
				if (skip) continue;
				info.setReturnValue(false);
				return;
			}
		}
		info.setReturnValue(items.isEmpty());
	}
}
