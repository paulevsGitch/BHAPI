package net.bhapi.mixin.common;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.util.BlockUtil;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.level.Level;
import net.minecraft.util.io.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin implements BlockStateContainer {
	@Shadow public int block;
	@Unique private BlockState bhapi_blockState;
	
	@Inject(method = "<init>(Lnet/minecraft/level/Level;DDDI)V", at = @At("TAIL"))
	private void bhapi_onFallingBlockEntityInit(Level arg, double d, double e, double f, int id, CallbackInfo info) {
		bhapi_blockState = BlockUtil.getLegacyBlock(id, 0);
		if (bhapi_blockState == null) {
			this.block = 0;
		}
	}
	
	@Inject(method = "writeCustomDataToTag", at = @At("HEAD"), cancellable = true)
	private void bhapi_writeCustomDataToTag(CompoundTag tag, CallbackInfo info) {
		if (bhapi_blockState != null) {
			tag.put("state", this.bhapi_blockState.saveToNBT());
		}
		info.cancel();
	}
	
	@Inject(method = "readCustomDataFromTag", at = @At("HEAD"), cancellable = true)
	private void bhapi_readCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
		if (tag.containsKey("state")) {
			this.bhapi_blockState = BlockState.loadFromNBT(tag.getCompoundTag("state"));
		}
		else this.block = 0;
		info.cancel();
	}
	
	@Override
	public void setDefaultState(BlockState state) {
		this.bhapi_blockState = state;
	}
	
	@Override
	public BlockState getDefaultState() {
		return this.bhapi_blockState;
	}
}
