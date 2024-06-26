package net.bhapi.mixin.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.technical.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
	@Shadow public ItemStack stack;
	
	public ItemEntityMixin(Level arg) {
		super(arg);
	}
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void bhapi_tick(CallbackInfo info) {
		// Remove invalid entities
		if (stack.count <= 0) {
			this.destroy();
			info.cancel();
		}
	}
}
