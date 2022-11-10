package net.bhapi.mixin.common;

import net.bhapi.util.ItemUtil;
import net.minecraft.item.BaseItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseItem.class)
public class BaseItemMixin {
	@Shadow @Final public int id;
	
	// Reset block and all its values to default
	// Allows to register same block multiple times
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_resetItem(int id, CallbackInfo info) {
		if (id == ItemUtil.MOD_ITEM_ID) {
			BaseItem.byId[this.id] = null;
		}
	}
}
