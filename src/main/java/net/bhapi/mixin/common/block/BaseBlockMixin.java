package net.bhapi.mixin.common.block;

import net.bhapi.block.CustomDropProvider;
import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.client.render.block.CustomBlockRender;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.util.BlockUtil;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BaseBlock.class)
public abstract class BaseBlockMixin implements BlockStateContainer, CustomBlockRender {
	@Shadow protected abstract void drop(Level arg, int i, int j, int k, ItemStack arg2);
	
	@Unique	private BlockState defaultState;
	
	// Reset block and all its values to default
	// Allows to register same block multiple times
	@Inject(method = "<init>(ILnet/minecraft/block/material/Material;)V", at = @At("TAIL"))
	private void bhapi_resetBlockEntries(int id, Material material, CallbackInfo info) {
		if (id == BlockUtil.MOD_BLOCK_ID) {
			BaseBlock.BY_ID[id] = null;
			BaseBlock.FULL_OPAQUE[id] = BaseBlock.FULL_OPAQUE[0];
			BaseBlock.LIGHT_OPACITY[id] = BaseBlock.LIGHT_OPACITY[0];
			BaseBlock.ALLOWS_GRASS_UNDER[id] = BaseBlock.ALLOWS_GRASS_UNDER[0];
			BaseBlock.HAS_TILE_ENTITY[id] = BaseBlock.HAS_TILE_ENTITY[0];
		}
	}
	
	@Unique
	@Override
	public BlockState getDefaultState() {
		if (defaultState == null) {
			setDefaultState(BlockState.getDefaultState(BaseBlock.class.cast(this)));
		}
		return defaultState;
	}
	
	@Unique
	@Override
	public void setDefaultState(BlockState state) {
		defaultState = state;
	}
	
	@Inject(method = "canPlaceAt(Lnet/minecraft/level/Level;III)Z", at = @At("HEAD"), cancellable = true)
	private void bhapi_canPlaceAt(Level level, int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockStateProvider.cast(level).getBlockState(x, y, z);
		info.setReturnValue(state.getBlock().material.isReplaceable());
	}
	
	@Inject(method = "drop(Lnet/minecraft/level/Level;IIIIF)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_drop(Level level, int x, int y, int z, int l, float f, CallbackInfo info) {
		if (this instanceof CustomDropProvider) {
			info.cancel();
			if (!level.isClientSide) {
				List<ItemStack> drop = new ArrayList<>();
				CustomDropProvider.cast(this).getCustomDrop(level, x, y, z, drop);
				drop.forEach(stack -> this.drop(level, x, y, z, stack));
			}
		}
	}
}
