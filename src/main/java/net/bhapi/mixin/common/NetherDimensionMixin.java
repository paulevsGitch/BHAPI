package net.bhapi.mixin.common;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.minecraft.block.BaseBlock;
import net.minecraft.level.dimension.BaseDimension;
import net.minecraft.level.dimension.NetherDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherDimension.class)
public abstract class NetherDimensionMixin extends BaseDimension {
	@Inject(method = "canSpawnOn", at = @At("HEAD"), cancellable = true)
	private void bhapi_canSpawnOn(int x, int z, CallbackInfoReturnable<Boolean> info) {
		LevelHeightProvider heightProvider = LevelHeightProvider.cast(this.level);
		BlockStateProvider stateProvider = BlockStateProvider.cast(this.level);
		for (short y = (short) (heightProvider.getLevelHeight() - 1); y >= 0; y--) {
			BlockState state = stateProvider.getBlockState(x, y, z);
			if (state.isAir()) continue;
			if (!state.is(BaseBlock.BEDROCK) && state.isFullOpaque()) {
				info.setReturnValue(true);
				return;
			}
		}
		info.setReturnValue(false);
	}
}
