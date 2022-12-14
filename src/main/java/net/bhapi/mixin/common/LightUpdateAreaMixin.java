package net.bhapi.mixin.common;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.level.LightUpdateArea;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightUpdateArea.class)
public class LightUpdateAreaMixin {
	@Shadow public int x1;
	@Shadow public int x2;
	@Shadow public int y1;
	@Shadow public int y2;
	@Shadow public int z1;
	@Shadow public int z2;
	@Shadow @Final public LightType lightType;
	@Unique private Level bhapi_currentLevel;
	
	/*@Inject(method = "process(Lnet/minecraft/level/Level;)V", at = @At("HEAD"))
	private void bhapi_process(Level level, CallbackInfo info) {
		bhapi_currentLevel = level;
	}*/
	
	@ModifyConstant(method = "process(Lnet/minecraft/level/Level;)V", constant = @Constant(intValue = 128))
	private int bhapi_changeMaxHeight(int value) {
		return LevelHeightProvider.cast(bhapi_currentLevel).getLevelHeight() - 1;
	}
	
	@Inject(method = "process", at = @At("HEAD"), cancellable = true)
	private void bhapi_process(Level level, CallbackInfo info) {
		info.cancel();
		
		bhapi_currentLevel = level;
		int sideX = this.x2 - this.x1 + 1;
		int sideY = this.y2 - this.y1 + 1;
		int sideZ = this.z2 - this.z1 + 1;
		
		int area = sideX * sideY * sideZ;
		if (area > 32768) {
			BHAPI.warn("Light too large, skipping!");
			return;
		}
		
		short height = LevelHeightProvider.cast(level).getLevelHeight();
		if (this.y2 >= height) this.y2 = height - 1;
		if (this.y1 < 0) this.y1 = 0;
		
		int l1, l2, l3, l4, l5, l6;
		for (int x = this.x1; x <= this.x2; ++x) {
			for (int z = this.z1; z <= this.z2; ++z) {
				boolean isLoaded = level.isAreaLoaded(x, 0, z, 1);
				if (isLoaded && level.getChunkFromCache(x >> 4, z >> 4).isClient()) isLoaded = false;
				if (!isLoaded) continue;
				
				for (int y = this.y1; y <= this.y2; ++y) {
					int light = level.getLight(this.lightType, x, y, z);
					BlockState state = BlockStateProvider.cast(level).getBlockState(x, y, z);
					int resultLight = 0;
					int opacity = state.getLightOpacity();
					
					if (opacity == 0) opacity = 1;
					
					int lightValue = 0;
					if (this.lightType == LightType.SKY && level.isAboveGround(x, y, z)) {
						lightValue = 15;
					}
					else if (this.lightType == LightType.BLOCK) {
						lightValue = state.getEmittance();
					}
					
					if (opacity < 15 || lightValue != 0) {
						l1 = level.getLight(this.lightType, x - 1, y, z);
						l2 = level.getLight(this.lightType, x + 1, y, z);
						l3 = level.getLight(this.lightType, x, y - 1, z);
						l4 = level.getLight(this.lightType, x, y + 1, z);
						l5 = level.getLight(this.lightType, x, y, z - 1);
						l6 = level.getLight(this.lightType, x, y, z + 1);
						resultLight = l1;
						if (l2 > resultLight) resultLight = l2;
						if (l3 > resultLight) resultLight = l3;
						if (l4 > resultLight) resultLight = l4;
						if (l5 > resultLight) resultLight = l5;
						if (l6 > resultLight) resultLight = l6;
						if ((resultLight -= opacity) < 0) resultLight = 0;
						if (lightValue > resultLight) resultLight = lightValue;
					}
					
					if (light == resultLight) continue;
					
					level.setLight(this.lightType, x, y, z, resultLight);
					l1 = resultLight - 1;
					
					if (l1 < 0) l1 = 0;
					
					level.updateLightIfNecessary(this.lightType, x - 1, y, z, l1);
					level.updateLightIfNecessary(this.lightType, x, y - 1, z, l1);
					level.updateLightIfNecessary(this.lightType, x, y, z - 1, l1);
					
					if (x + 1 >= this.x2) level.updateLightIfNecessary(this.lightType, x + 1, y, z, l1);
					if (y + 1 >= this.y2) level.updateLightIfNecessary(this.lightType, x, y + 1, z, l1);
					
					if (z + 1 < this.z2) continue;
					level.updateLightIfNecessary(this.lightType, x, y, z + 1, l1);
				}
			}
		}
	}
}
