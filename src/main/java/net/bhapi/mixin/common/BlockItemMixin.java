package net.bhapi.mixin.common;

import net.bhapi.level.LevelHeightProvider;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@Unique private Level bhapi_level;
	
	@Inject(method = "useOnBlock", at = @At("HEAD"))
	private void bhapi_storeLevel(ItemStack item, PlayerBase player, Level level, int x, int y, int z, int facing, CallbackInfoReturnable<Boolean> info) {
		this.bhapi_level = level;
	}
	
	@ModifyConstant(method = "useOnBlock", constant = @Constant(intValue = 127))
	private int bhapi_changeMaxHeight(int value) {
		LevelHeightProvider provider = LevelHeightProvider.cast(bhapi_level.dimension);
		return provider.getLevelHeight() - 1;
	}
}
