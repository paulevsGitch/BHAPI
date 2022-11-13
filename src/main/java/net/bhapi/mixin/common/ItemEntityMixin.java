package net.bhapi.mixin.common;

import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends BaseEntity {
	@Shadow public ItemStack stack;
	
	public ItemEntityMixin(Level arg) {
		super(arg);
	}
	
	@Inject(method = "<init>(Lnet/minecraft/level/Level;DDDLnet/minecraft/item/ItemStack;)V", at = @At("TAIL"))
	private void bhapi_onItemEntityInit(Level arg, double x, double y, double z, ItemStack arg2, CallbackInfo info) {
		System.out.println("Init: " + stack + " " + arg2 + " " + arg + " " + x + " " + y + " " + z);
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
