package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.util.BlockDirection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.LeavesBaseBlock;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LeavesBaseBlock.class)
public class LeavesBaseBlockMixin implements BlockStateContainer {
	@Shadow protected boolean isTransparent;
	
	@Override
	@Environment(EnvType.CLIENT)
	public boolean bhapi_isSideRendered(BlockView blockView, int x, int y, int z, BlockDirection facing, BlockState state, BlockState target) {
		return this.isTransparent || BlockStateContainer.super.bhapi_isSideRendered(blockView, x, y, z, facing, state, target);
	}
}
