package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.BlockUtil;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.material.Material;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(DoorBlock.class)
public abstract class DoorBlockMixin extends BaseBlock implements BlockStateContainer {
	@Shadow public abstract void method_837(Level arg, int i, int j, int k, boolean bl);
	
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
		int meta = state.getValue(LegacyProperties.META_16);
		DoorBlock block = DoorBlock.class.cast(this);
		if ((meta & 8) != 0) {
			if (!provider.getBlockState(x, y - 1, z).is(block)) {
				provider.setBlockState(x, y, z, BlockUtil.AIR_STATE);
			}
			if (!neighbour.isAir() && neighbour.emitsPower()) {
				BlockState bottom = provider.getBlockState(x, y - 1, z);
				onNeighbourBlockUpdate(level, x, y - 1, z, facing, bottom, neighbour);
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
				if (!level.isClientSide) {
					this.drop(level, x, y, z, meta);
				}
			}
			else if (!neighbour.isAir() && neighbour.emitsPower()) {
				boolean power = level.hasRedstonePower(x, y, z) || level.hasRedstonePower(x, y + 1, z);
				this.method_837(level, x, y, z, power);
			}
		}
	}
}

