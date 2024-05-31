package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.minecraft.client.ClientInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientInteractionManager.class)
public class ClientInteractionManagerMixin {
	@Shadow @Final protected Minecraft minecraft;
	
	@Inject(method = "activateBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_activateBlock(int x, int y, int z, int l, CallbackInfoReturnable<Boolean> info) {
		Level level = this.minecraft.level;
		BlockState state = BlockStateProvider.cast(this.minecraft.level).bhapi_getBlockState(x, y, z);
		level.playLevelEvent(2001, x, y, z, state.getID());
		boolean result = level.setBlock(x, y, z, 0);
		if (result) {
			int meta = level.getBlockMeta(x, y, z);
			state.getBlock().activate(level, x, y, z, meta);
		}
		info.setReturnValue(result);
	}
	
	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_useOnBlock(PlayerEntity player, Level level, ItemStack stack, int x, int y, int z, int l, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockStateProvider.cast(this.minecraft.level).bhapi_getBlockState(x, y, z);
		if (state.getBlock().canUse(level, x, y, z, player)) {
			info.setReturnValue(true);
			return;
		}
		if (stack == null) {
			info.setReturnValue(false);
			return;
		}
		info.setReturnValue(stack.useOnBlock(player, level, x, y, z, l));
	}
}
