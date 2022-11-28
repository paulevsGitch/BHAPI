package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.BlockUtil;
import net.minecraft.block.TntBlock;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TntBlock.class)
public abstract class TntBlockMixin implements BlockStateContainer {
	@Shadow public abstract void activate(Level arg, int i, int j, int k, int l);
	
	@Override
	public void onNeighbourBlockUpdate(Level level, int x, int y, int z, BlockDirection facing, BlockState state, BlockState neighbour) {
		if (!neighbour.isAir() && neighbour.emitsPower() && level.hasRedstonePower(x, y, z)) {
			this.activate(level, x, y, z, 1);
			BlockStateProvider.cast(level).setBlockState(x, y, z, BlockUtil.AIR_STATE);
		}
	}
}
