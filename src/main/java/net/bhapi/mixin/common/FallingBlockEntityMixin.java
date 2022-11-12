package net.bhapi.mixin.common;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.io.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin implements BlockStateContainer {
	@Unique private BlockState bhapi_blockState;
	
	@Inject(method = "writeCustomDataToTag", at = @At("HEAD"), cancellable = true)
	private void bhapi_writeCustomDataToTag(CompoundTag tag, CallbackInfo info) {
		tag.put("state", this.bhapi_blockState.saveToNBT());
		info.cancel();
	}
	
	@Inject(method = "readCustomDataFromTag", at = @At("HEAD"), cancellable = true)
	private void bhapi_readCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
		this.bhapi_blockState = BlockState.loadFromNBT(tag.getCompoundTag("state"));
		info.cancel();
	}
	
	@Override
	public void setDefaultState(BlockState state) {
		this.bhapi_blockState = state;
	}
}
