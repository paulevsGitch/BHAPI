package net.bhapi.mixin.common.item;

import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.item.BHItemRender;
import net.bhapi.util.ItemUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class BaseItemMixin implements BHItemRender {
	@Shadow @Final public int id;
	@Shadow protected int texturePosition;
	
	@Environment(EnvType.CLIENT)
	@Shadow public abstract int getTexturePosition(ItemStack arg);
	
	// Reset block and all its values to default
	// Allows to register same block multiple times
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_resetItem(int id, CallbackInfo info) {
		ItemUtil.checkFrozen();
		if (id == ItemUtil.MOD_ITEM_ID) {
			Item.byId[this.id] = null;
		}
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTexture(@Nullable ItemStack stack) {
		int index = 0;
		if (this.id != ItemUtil.MOD_ITEM_ID) {
			if (stack == null) index = this.texturePosition;
			else index = getTexturePosition(stack);
		}
		return Textures.getVanillaItemSample(index);
	}
}
