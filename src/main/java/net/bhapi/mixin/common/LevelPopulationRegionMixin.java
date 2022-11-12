package net.bhapi.mixin.common;

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
				int n3 = this.method_142(x + 1, y, z, false);
				int n4 = this.method_142(x - 1, y, z, false);
				int n5 = this.method_142(x, y, z + 1, false);
				int n6 = this.method_142(x, y, z - 1, false);
				if (n3 > light) {
					light = n3;
				}
				if (n4 > light) {
					light = n4;
				}
				if (n5 > light) {
					light = n5;
				}
				if (n6 > light) {
					light = n6;
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
		info.setReturnValue(this.chunks[cx][cz].getLight(x & 0xF, y, z & 0xF, this.level.correctLightValue));
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
}
