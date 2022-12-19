package net.bhapi.mixin.common.level;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.util.BlockUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.level.Level;
import net.minecraft.level.LevelPopulationRegion;
import net.minecraft.level.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelPopulationRegion.class)
public abstract class LevelPopulationRegionMixin implements BlockStateProvider, LevelHeightProvider {
	@Shadow private Level level;
	@Shadow private int field_166;
	@Shadow private int field_167;
	@Shadow private Chunk[][] chunks;
	
	@Environment(value=EnvType.CLIENT)
	@Shadow public abstract int method_142(int i, int j, int k, boolean bl);
	
	@ModifyConstant(method = {
		"method_142(IIIZ)I",
		"getBlockId(III)I",
		"getBlockMeta(III)I"
	}, constant = @Constant(intValue = 128))
	private int bhapi_changeMaxHeight(int value) {
		return getLevelHeight();
	}
	
	@Environment(value= EnvType.CLIENT)
	@Inject(method = "method_142", at = @At("HEAD"), cancellable = true)
	private void bhapi_getLight(int x, int y, int z, boolean flag, CallbackInfoReturnable<Integer> info) {
		if (x < -32000000 || z < -32000000 || x >= 32000000 || z > 32000000) {
			info.setReturnValue(15);
			return;
		}
		
		if (flag) {
			BlockState state = getBlockState(x, y, z);
			if (state.is(BaseBlock.STONE_SLAB) || state.is(BaseBlock.FARMLAND) || state.getBlock() instanceof StairsBlock) {
				int light = this.method_142(x, y + 1, z, false);
				int lx1 = this.method_142(x + 1, y, z, false);
				int lx2 = this.method_142(x - 1, y, z, false);
				int lz1 = this.method_142(x, y, z + 1, false);
				int lz2 = this.method_142(x, y, z - 1, false);
				
				if (lx1 > light) {
					light = lx1;
				}
				if (lx2 > light) {
					light = lx2;
				}
				if (lz1 > light) {
					light = lz1;
				}
				if (lz2 > light) {
					light = lz2;
				}
				
				info.setReturnValue(light);
				return;
			}
		}
		
		if (y < 0) {
			info.setReturnValue(0);
			return;
		}
		
		if (y >= getLevelHeight()) {
			int light = 15 - this.level.correctLightValue;
			if (light < 0) {
				light = 0;
			}
			info.setReturnValue(light);
			return;
		}
		
		int cx = (x >> 4) - this.field_166;
		int cz = (z >> 4) - this.field_167;
		
		info.setReturnValue(this.chunks[cx][cz].getLight(x & 15, y, z & 15, this.level.correctLightValue));
	}
	
	@Environment(value=EnvType.CLIENT)
	@Inject(method = "isFullOpaque", at = @At("HEAD"), cancellable = true)
	private void bhapi_isFullOpaque(int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(getBlockState(x, y, z).isFullOpaque());
	}
	
	@Unique
	@Override
	public short getLevelHeight() {
		return LevelHeightProvider.cast(this.level).getLevelHeight();
	}
	
	@Unique
	@Override
	public boolean setBlockState(int x, int y, int z, BlockState state, boolean update) {
		return false;
	}
	
	@Unique
	@Override
	public BlockState getBlockState(int x, int y, int z) {
		if (y < 0) {
			return BlockUtil.AIR_STATE;
		}
		
		if (y >= getLevelHeight()) {
			return BlockUtil.AIR_STATE;
		}
		
		int cx = (x >> 4) - this.field_166;
		int cz = (z >> 4) - this.field_167;
		if (cx < 0 || cx >= this.chunks.length || cz < 0 || cz >= this.chunks[cx].length) {
			return BlockUtil.AIR_STATE;
		}
		
		Chunk chunk = this.chunks[cx][cz];
		if (chunk == null) {
			return BlockUtil.AIR_STATE;
		}
		
		return BlockStateProvider.cast(chunk).getBlockState(x & 15, y, z & 15);
	}
	
	@Inject(method = "getBlockMeta", at = @At("HEAD"), cancellable = true)
	private void bhapi_getBlockMeta(int x, int y, int z, CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(getBlockState(x, y, z).getMeta());
	}
}
