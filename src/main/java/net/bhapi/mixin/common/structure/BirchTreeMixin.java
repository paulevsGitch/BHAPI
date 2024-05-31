package net.bhapi.mixin.common.structure;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.minecraft.block.Block;
import net.minecraft.level.Level;
import net.minecraft.level.structure.BirchTreeStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BirchTreeStructure.class)
public class BirchTreeMixin {
	@Inject(method = "generate", at = @At("HEAD"), cancellable = true)
	private void bhapi_generate(Level level, Random random, int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		int px, pz;
		int height = random.nextInt(3) + 5;
		
		LevelHeightProvider heightProvider = LevelHeightProvider.cast(level);
		BlockStateProvider stateProvider = BlockStateProvider.cast(level);
		
		int levelHeight = heightProvider.bhapi_getLevelHeight();
		if (y < 1 || y + height + 1 > levelHeight) {
			info.setReturnValue(false);
			return;
		}
		
		int radius, py;
		boolean flag = true;
		BlockState state;
		for (py = y; py <= y + 1 + height; ++py) {
			radius = 1;
			if (py == y) {
				radius = 0;
			}
			if (py >= y + 1 + height - 2) {
				radius = 2;
			}
			for (px = x - radius; px <= x + radius && flag; ++px) {
				for (pz = z - radius; pz <= z + radius && flag; ++pz) {
					if (py >= 0 && py < levelHeight) {
						state = stateProvider.bhapi_getBlockState(px, py, pz);
						if (state.isAir() || state.is(Block.LEAVES)) continue;
						flag = false;
						continue;
					}
					flag = false;
				}
			}
		}
		
		if (!flag) {
			info.setReturnValue(false);
			return;
		}
		
		state = stateProvider.bhapi_getBlockState(x, y - 1, z);
		if (!state.is(Block.GRASS) && !state.is(Block.DIRT) || y >= levelHeight - height - 1) {
			info.setReturnValue(false);
			return;
		}
		
		stateProvider.bhapi_setBlockState(x, y - 1, z, BlockState.getDefaultState(Block.DIRT));
		BlockState placing = BlockState.getDefaultState(Block.LEAVES);
		placing = placing.withMeta(2);
		
		for (py = y - 3 + height; py <= y + height; ++py) {
			int dy = py - (y + height);
			radius = 1 - dy / 2;
			for (px = x - radius; px <= x + radius; ++px) {
				int dx = px - x;
				for (pz = z - radius; pz <= z + radius; ++pz) {
					int dz = pz - z;
					if (Math.abs(dx) == radius && Math.abs(dz) == radius && (random.nextInt(2) == 0 || dy == 0)) continue;
					state = stateProvider.bhapi_getBlockState(px, py, pz);
					if (state.isFullOpaque()) continue;
					stateProvider.bhapi_setBlockState(px, py, pz, placing);
				}
			}
		}
		
		placing = BlockState.getDefaultState(Block.LOG);
		placing = placing.withMeta(2);
		for (py = 0; py < height; ++py) {
			state = stateProvider.bhapi_getBlockState(x, y + py, z);
			if (!state.isAir() && !state.is(Block.LEAVES)) continue;
			stateProvider.bhapi_setBlockState(x, y + py, z, placing);
		}
		
		info.setReturnValue(true);
	}
}
