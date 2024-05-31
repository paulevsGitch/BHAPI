package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.minecraft.block.Block;
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
public abstract class TorchBlockMixin extends Block implements BlockStateContainer {
	@Shadow protected abstract boolean method_1674(Level arg, int i, int j, int k);
	
	protected TorchBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Override
	public void bhapi_appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_6);
	}
	
	@Override
	public boolean canPlaceAt(Level level, int x, int y, int z, int facing) {
		BlockState state = BlockStateProvider.cast(level).bhapi_getBlockState(x, y, z);
		if (state.is(this)) return true;
		if (!state.getMaterial().isReplaceable() && !state.is(Block.SNOW)) {
			return false;
		}
		BlockDirection dir = BlockDirection.getFromFacing(facing).invert();
		if (dir == BlockDirection.POS_Y) {
			if (state.is(Block.SNOW)) dir = BlockDirection.NEG_Y;
			else return false;
		}
		Vec3I pos = dir.move(new Vec3I(x, y, z));
		if (facing < 2) return method_1674(level, pos.x, pos.y, pos.z);
		else return level.canSuffocate(pos.x, pos.y, pos.z);
	}
	
	@Inject(method = "onBlockPlaced(Lnet/minecraft/level/Level;IIII)V", at = @At("HEAD"), cancellable = true)
	public void onBlockPlaced(Level level, int x, int y, int z, int facing, CallbackInfo info) {
		int meta = 6 - facing;
		if (meta < 1 || meta > 5) meta = 5;
		BlockState state = BlockState.getDefaultState(this);
		BlockStateProvider.cast(level).bhapi_setBlockState(x, y, z, state.withMeta(meta));
		info.cancel();
	}
	
	@Inject(method = "onBlockPlaced(Lnet/minecraft/level/Level;III)V", at = @At("HEAD"), cancellable = true)
	public void onBlockPlaced(Level level, int x, int y, int z, CallbackInfo info) {
		info.cancel();
	}
}
