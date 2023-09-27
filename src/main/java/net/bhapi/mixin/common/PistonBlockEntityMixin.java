package net.bhapi.mixin.common;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.registry.CommonRegistries;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlockEntity.class)
public abstract class PistonBlockEntityMixin extends BaseBlockEntity implements BlockStateContainer {
	@Shadow private int blockID;
	@Shadow private float progress;
	@Shadow private float maxProgress;
	@Shadow private boolean isExtending;
	@Shadow private int blockData;
	
	@Shadow protected abstract void moveBlocks(float f, float g);
	
	@Override
	public void setDefaultState(BlockState state) {
		this.blockID = state.getID();
		this.blockData = state.getMeta();
	}
	
	@Override
	public BlockState getDefaultState() {
		return CommonRegistries.BLOCKSTATES_MAP.get(this.blockID);
	}
	
	@Inject(method = "resetBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_resetBlock(CallbackInfo info) {
		info.cancel();
		if (this.progress < 1.0f) {
			this.maxProgress = 1.0f;
			this.progress = 1.0f;
			this.level.removeBlockEntity(this.x, this.y, this.z);
			this.invalidate();
			BlockStateProvider provider = BlockStateProvider.cast(level);
			if (provider.getBlockState(this.x, this.y, this.z).is(BaseBlock.MOVING_PISTON)) {
				provider.setBlockState(this.x, this.y, this.z, getDefaultState());
			}
		}
	}
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void bhapi_tick(CallbackInfo info) {
		info.cancel();
		this.progress = this.maxProgress;
		if (this.progress >= 1.0f) {
			this.moveBlocks(1.0f, 0.25f);
			this.level.removeBlockEntity(this.x, this.y, this.z);
			this.invalidate();
			BlockStateProvider provider = BlockStateProvider.cast(level);
			if (provider.getBlockState(this.x, this.y, this.z).is(BaseBlock.MOVING_PISTON)) {
				provider.setBlockState(this.x, this.y, this.z, getDefaultState());
			}
			return;
		}
		this.maxProgress += 0.5f;
		if (this.maxProgress >= 1.0f) {
			this.maxProgress = 1.0f;
		}
		if (this.isExtending) {
			this.moveBlocks(this.maxProgress, this.maxProgress - this.progress + 0.0625f);
		}
	}
}
