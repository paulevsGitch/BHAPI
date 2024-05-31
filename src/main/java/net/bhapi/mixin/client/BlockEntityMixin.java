package net.bhapi.mixin.client;

import net.bhapi.level.BlockStateProvider;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
	@Shadow public Level level;
	@Shadow public int x;
	@Shadow public int y;
	@Shadow public int z;
	
	@Inject(method = "getBlock()Lnet/minecraft/block/Block;", at = @At("HEAD"), cancellable = true)
	private void bhapi_getBlock(CallbackInfoReturnable<Block> info) {
		BlockStateProvider provider = BlockStateProvider.cast(level);
		info.setReturnValue(provider.bhapi_getBlockState(this.x, this.y, this.z).getBlock());
	}
}
