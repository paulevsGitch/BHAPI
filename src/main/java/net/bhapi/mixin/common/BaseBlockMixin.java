package net.bhapi.mixin.common;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseBlock.class)
public class BaseBlockMixin implements BlockStateContainer {
	@Unique	private BlockState defaultState;
	
	// Reset block and all its values to default
	// Allows to register same block multiple times
	@Inject(method = "<init>(ILnet/minecraft/block/material/Material;)V", at = @At("TAIL"))
	private void bhapi_resetBlockEntries(int id, Material material, CallbackInfo info) {
		if (id == 255) {
			BaseBlock.BY_ID[255] = null;
			BaseBlock.FULL_OPAQUE[255] = BaseBlock.FULL_OPAQUE[0];
			BaseBlock.LIGHT_OPACITY[255] = BaseBlock.LIGHT_OPACITY[0];
			BaseBlock.ALLOWS_GRASS_UNDER[255] = BaseBlock.ALLOWS_GRASS_UNDER[0];
			BaseBlock.HAS_TILE_ENTITY[255] = BaseBlock.HAS_TILE_ENTITY[0];
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
}
