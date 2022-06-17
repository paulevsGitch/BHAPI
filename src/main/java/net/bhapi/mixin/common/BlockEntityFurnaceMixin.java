package net.bhapi.mixin.common;

import net.minecraft.block.BaseBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.tileentity.BlockEntityFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityFurnace.class)
public class BlockEntityFurnaceMixin {
	// TODO make a fuel API, make block items (see original function with WOOD), add saplings
	@Inject(method = "getFuelTime(Lnet/minecraft/item/ItemInstance;)I", at = @At("HEAD"), cancellable = true)
	private void bhapi_getFuelTime(ItemInstance item, CallbackInfoReturnable<Integer> info) {
		if (item == null) info.setReturnValue(0);
		else if (item.getType() == ItemBase.stick) info.setReturnValue(100);
		else if (item.getType() == ItemBase.coal) info.setReturnValue(1600);
		else if (item.getType() == ItemBase.lavaBucket) info.setReturnValue(20000);
		else info.setReturnValue(0);
	}
}
