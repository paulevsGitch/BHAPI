package net.bhapi.mixin.common;

import net.bhapi.level.LevelHeightProvider;
import net.minecraft.level.Level;
import net.minecraft.level.LightUpdateArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightUpdateArea.class)
public class LightUpdateAreaMixin {
	@Unique private Level bhapi_currentLevel;
	
	@Inject(method = "process(Lnet/minecraft/level/Level;)V", at = @At("HEAD"))
	private void bhapi_process(Level level, CallbackInfo info) {
		bhapi_currentLevel = level;
	}
	
	@ModifyConstant(method = "process(Lnet/minecraft/level/Level;)V", constant = @Constant(intValue = 128))
	private int bhapi_changeMaxHeight(int value) {
		return LevelHeightProvider.cast(bhapi_currentLevel).getLevelHeight() - 1;
	}
}
