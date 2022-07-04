package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockStateProvider;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseBlockEntity.class)
public class BaseBlockEntityMixin {
	@Shadow public Level level;
	@Shadow public int x;
	@Shadow public int y;
	@Shadow public int z;
	
	@Inject(method = "getBlock()Lnet/minecraft/block/BaseBlock;", at = @At("HEAD"), cancellable = true)
	private void bhapi_getBlock(CallbackInfoReturnable<BaseBlock> info) {
		BlockStateProvider provider = BlockStateProvider.cast(level);
		info.setReturnValue(provider.getBlockState(this.x, this.y, this.z).getBlock());
	}
}
