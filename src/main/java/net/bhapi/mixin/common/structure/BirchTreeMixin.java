package net.bhapi.mixin.common.structure;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.minecraft.block.BaseBlock;
import net.minecraft.level.Level;
import net.minecraft.level.structure.BirchTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BirchTree.class)
public class BirchTreeMixin {
	@Inject(method = "generate", at = @At("HEAD"), cancellable = true)
	private void bhapi_generate(Level level, Random random, int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		int px, pz;
		int height = random.nextInt(3) + 5;
		
		LevelHeightProvider heightProvider = LevelHeightProvider.cast(level);
		BlockStateProvider stateProvider = BlockStateProvider.cast(level);
		
		int levelHeight = heightProvider.getLevelHeight();
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
						state = stateProvider.getBlockState(px, py, pz);
						if (state.isAir() || state.is(BaseBlock.LEAVES)) continue;
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
		
		state = stateProvider.getBlockState(x, y - 1, z);
		if (!state.is(BaseBlock.GRASS) && !state.is(BaseBlock.DIRT) || y >= levelHeight - height - 1) {
			info.setReturnValue(false);
			return;
		}
		
		stateProvider.setBlockState(x, y - 1, z, BlockState.getDefaultState(BaseBlock.DIRT));
		BlockState placing = BlockState.getDefaultState(BaseBlock.LEAVES);
		placing = placing.with(placing.getProperty("meta"), 2);
		
		/*for (py = y - 3 + height; py <= y + height; ++py) {
			int maxY = py - (y + height);
			int dxz = 1 - maxY / 2;
			for (px = x - dxz; px <= x + dxz; ++px) {
				int dx = px - x;
				for (pz = z - dxz; pz <= z + dxz; ++pz) {
					int dz = pz - z;
					if (Math.abs(dx) == dxz && Math.abs(dz) == dxz && (random.nextInt(2) == 0 || maxY == 0)) continue;
					state = stateProvider.getBlockState(px, py, pz);
					if (state.isFullOpaque()) continue;
					stateProvider.setBlockState(px, py, pz, placing);
				}
			}
		}*/
		
		for (py = y - 3 + height; py <= y + height; ++py) {
			int dy = py - (y + height);
			radius = 1 - dy / 2;
			for (px = x - radius; px <= x + radius; ++px) {
				int dx = px - x;
				for (pz = z - radius; pz <= z + radius; ++pz) {
					int dz = pz - z;
					if (Math.abs(dx) == radius && Math.abs(dz) == radius && (random.nextInt(2) == 0 || dy == 0)) continue;
					state = stateProvider.getBlockState(px, py, pz);
					if (state.isFullOpaque()) continue;
					//arg.setBlockInChunk(px, py, pz, BaseBlock.LEAVES.id, 2);
					stateProvider.setBlockState(px, py, pz, placing);
				}
			}
		}
		
		placing = BlockState.getDefaultState(BaseBlock.LOG);
		placing = placing.with(placing.getProperty("meta"), 2);
		for (py = 0; py < height; ++py) {
			state = stateProvider.getBlockState(x, y + py, z);
			if (!state.isAir() && !state.is(BaseBlock.LEAVES)) continue;
			stateProvider.setBlockState(x, y + py, z, placing);
		}
		
		info.setReturnValue(true);
	}
}
