package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.BlockUtil;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.material.Material;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DoorBlock.class)
public abstract class DoorBlockMixin extends BaseBlock implements BlockStateContainer {
	@Shadow public abstract void updateDoor(Level arg, int i, int j, int k, boolean bl);
	
	protected DoorBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	@Override
	public void onNeighbourBlockUpdate(Level level, int x, int y, int z, BlockDirection facing, BlockState state, BlockState neighbour) {
		BlockStateProvider provider = BlockStateProvider.cast(level);
		DoorBlock block = DoorBlock.class.cast(this);
		int meta = state.getMeta();
		
		if ((meta & 8) != 0) {
			if (!provider.getBlockState(x, y - 1, z).is(block)) {
				provider.setBlockState(x, y, z, BlockUtil.AIR_STATE);
			}
			if (!neighbour.isAir() && neighbour.emitsPower()) {
				BlockState bottom = provider.getBlockState(x, y - 1, z);
				onNeighbourBlockUpdate(level, x, y - 1, z, BlockDirection.POS_Y, bottom, neighbour);
			}
		}
		else {
			boolean drop = false;
			if (!provider.getBlockState(x, y + 1, z).is(block)) {
				provider.setBlockState(x, y, z, BlockUtil.AIR_STATE);
				drop = true;
			}
			
			if (!level.canSuffocate(x, y - 1, z)) {
				provider.setBlockState(x, y, z, BlockUtil.AIR_STATE);
				drop = true;
				if (!provider.getBlockState(x, y + 1, z).is(block)) {
					provider.setBlockState(x, y + 1, z, BlockUtil.AIR_STATE);
				}
			}
			
			if (drop) {
				if (!level.isRemote) {
					this.drop(level, x, y, z, meta);
				}
			}
			else if (!neighbour.isAir() && neighbour.emitsPower()) {
				boolean power = level.hasRedstonePower(x, y, z) || level.hasRedstonePower(x, y + 1, z);
				this.updateDoor(level, x, y, z, power);
			}
		}
	}
	
	@Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
	private void bhapi_canPlaceAt(Level arg, int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		if (y >= LevelHeightProvider.cast(arg).getLevelHeight()) {
			info.setReturnValue(false);
		}
		info.setReturnValue(
			arg.canSuffocate(x, y - 1, z) &&
			super.canPlaceAt(arg, x, y, z) &&
			super.canPlaceAt(arg, x, y + 1, z)
		);
	}
	
	@Inject(method = "updateDoor", at = @At("HEAD"), cancellable = true)
	public void bhapi_updateDoor(Level level, int x, int y, int z, boolean power, CallbackInfo info) {
		info.cancel();
		BlockStateProvider provider = BlockStateProvider.cast(level);
		BlockState state = provider.getBlockState(x, y, z);
		int meta = state.getMeta();
		if ((meta & 8) != 0) {
			if (provider.getBlockState(x, y - 1, z).is(this)) {
				this.updateDoor(level, x, y - 1, z, power);
			}
			return;
		}
		boolean metaPower = (meta & 4) > 0;
		if (metaPower == power) return;
		if (provider.getBlockState(x, y + 1, z).is(this)) {
			provider.setBlockState(x, y + 1, z, state.withMeta((meta ^ 4) + 8));
		}
		provider.setBlockState(x, y, z, state.withMeta(meta ^ 4));
		level.updateArea(x, y - 1, z, x, y, z);
		level.playLevelEvent(null, 1003, x, y, z, 0);
	}
}

