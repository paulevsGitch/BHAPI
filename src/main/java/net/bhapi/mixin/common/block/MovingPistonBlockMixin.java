package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.registry.CommonRegistries;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.MovingPistonBlock;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.technical.PistonDataValues;
import net.minecraft.level.BlockView;
import net.minecraft.level.Level;
import net.minecraft.util.maths.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MovingPistonBlock.class)
public abstract class MovingPistonBlockMixin extends BaseBlock implements BlockStateContainer {
	protected MovingPistonBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	@Shadow protected abstract PistonBlockEntity method_1535(BlockView arg, int i, int j, int k);
	
	@Inject(method = "updateBoundingBox", at = @At("HEAD"), cancellable = true)
	private void bhapi_updateBoundingBox(BlockView view, int x, int y, int z, CallbackInfo info) {
		info.cancel();
		PistonBlockEntity entity = this.method_1535(view, x, y, z);
		if (entity != null) {
			BlockState state = BlockStateContainer.cast(entity).getDefaultState();
			if (state == null || state.is(this)) {
				return;
			}
			BaseBlock block = state.getBlock();
			block.updateBoundingBox(view, x, y, z);
			float delta = entity.getProgress(0.0f);
			if (entity.isExtending()) {
				delta = 1.0f - delta;
			}
			int facing = entity.getFacing();
			this.minX = block.minX - PistonDataValues.OFFSET_X[facing] * delta;
			this.minY = block.minY - PistonDataValues.OFFSET_Y[facing] * delta;
			this.minZ = block.minZ - PistonDataValues.OFFSET_Z[facing] * delta;
			this.maxX = block.maxX - PistonDataValues.OFFSET_X[facing] * delta;
			this.maxY = block.maxY - PistonDataValues.OFFSET_Y[facing] * delta;
			this.maxZ = block.maxZ - PistonDataValues.OFFSET_Z[facing] * delta;
		}
	}
	
	@Inject(method = "getExtendedBox", at = @At("HEAD"), cancellable = true)
	private void bhapi_getExtendedBox(Level level, int x, int y, int z, int rawID, float delta, int facing, CallbackInfoReturnable<Box> info) {
		BlockState state = CommonRegistries.BLOCKSTATES_MAP.get(rawID);
		if (state == null || state.isAir() || state.is(this)) {
			info.setReturnValue(null);
			return;
		}
		
		Box box = state.getBlock().getCollisionShape(level, x, y, z);
		if (box == null) {
			info.setReturnValue(null);
			return;
		}
		
		box.minX -= PistonDataValues.OFFSET_X[facing] * delta;
		box.maxX -= PistonDataValues.OFFSET_X[facing] * delta;
		box.minY -= PistonDataValues.OFFSET_Y[facing] * delta;
		box.maxY -= PistonDataValues.OFFSET_Y[facing] * delta;
		box.minZ -= PistonDataValues.OFFSET_Z[facing] * delta;
		box.maxZ -= PistonDataValues.OFFSET_Z[facing] * delta;
		
		info.setReturnValue(box);
	}
}
