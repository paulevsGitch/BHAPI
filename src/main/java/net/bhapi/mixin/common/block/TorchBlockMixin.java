package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.material.Material;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TorchBlock.class)
public abstract class TorchBlockMixin extends BaseBlock implements BlockStateContainer {
	@Shadow protected abstract boolean method_1674(Level arg, int i, int j, int k);
	
	protected TorchBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_6);
	}
	
	@Override
	public boolean canPlaceAt(Level level, int x, int y, int z, int facing) {
		BlockState state = BlockStateProvider.cast(level).getBlockState(x, y, z);
		if (state.is(this)) return true;
		if (!state.getMaterial().isReplaceable() && !state.is(BaseBlock.SNOW)) {
			return false;
		}
		BlockDirection dir = BlockDirection.getFromFacing(facing).invert();
		if (dir == BlockDirection.POS_Y) {
			if (state.is(BaseBlock.SNOW)) dir = BlockDirection.NEG_Y;
			else return false;
		}
		Vec3I pos = dir.move(new Vec3I(x, y, z));
		if (facing == 5) return method_1674(level, pos.x, pos.y, pos.z);
		else return level.canSuffocate(pos.x, pos.y, pos.z);
	}
	
	@Inject(method = "onBlockPlaced(Lnet/minecraft/level/Level;IIII)V", at = @At("HEAD"), cancellable = true)
	public void onBlockPlaced(Level level, int x, int y, int z, int facing, CallbackInfo info) {
		int meta = 6 - facing;
		if (meta < 1 || meta > 5) meta = 5;
		BlockState state = BlockState.getDefaultState(this);
		state = state.with(state.getProperty("meta"), meta);
		BlockStateProvider.cast(level).setBlockState(x, y, z, state);
		info.cancel();
	}
	
	@Inject(method = "onBlockPlaced(Lnet/minecraft/level/Level;III)V", at = @At("HEAD"), cancellable = true)
	public void onBlockPlaced(Level level, int x, int y, int z, CallbackInfo info) {
		info.cancel();
	}
}
