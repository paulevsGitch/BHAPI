package net.bhapi.mixin.common.recipe;

import net.minecraft.inventory.Crafting;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
	@Shadow private ItemStack output;
	@Shadow private int width;
	@Shadow private int height;
	@Shadow private ItemStack[] ingredients;
	
	@Inject(method = "craft", at = @At("HEAD"), cancellable = true)
	private void bhapi_craft(Crafting arg, CallbackInfoReturnable<ItemStack> info) {
		info.setReturnValue(this.output.copy());
	}
	
	@Inject(method = "matches", at = @At("HEAD"), cancellable = true)
	private void bhapi_matches(Crafting crafting, int x, int y, boolean flag, CallbackInfoReturnable<Boolean> info) {
		for (int dx = 0; dx < 3; ++dx) {
			for (int dy = 0; dy < 3; ++dy) {
				ItemStack stack;
				int px = dx - x;
				int py = dy - y;
				
				ItemStack ingredient = null;
				if (px >= 0 && py >= 0 && px < this.width && py < this.height) {
					ingredient = flag ? this.ingredients[this.width - px - 1 + py * this.width] : this.ingredients[px + py * this.width];
				}
				
				stack = crafting.getInventoryItemXY(dx, dy);
				
				if (stack == null && ingredient == null) continue;
				if (stack == null || ingredient == null) {
					info.setReturnValue(false);
					return;
				}
				
				if (ingredient.getType() != stack.getType()) {
					info.setReturnValue(false);
					return;
				}
				
				if (ingredient.getDamage() == -1 || ingredient.getDamage() == stack.getDamage()) continue;
				
				info.setReturnValue(false);
				return;
			}
		}
		info.setReturnValue(true);
	}
}
