package net.bhapi.mixin.common;

import net.bhapi.level.LevelHeightProvider;
import net.minecraft.level.Level;
import net.minecraft.level.LevelMonsterSpawner;
import net.minecraft.util.maths.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LevelMonsterSpawner.class)
public class LevelMonsterSpawnerMixin {
	@Unique private static Level bhapi_currentLevel;
	
	@Inject(method = "spawnMonsters(Lnet/minecraft/level/Level;Ljava/util/List;)Z", at = @At("HEAD"))
	private static void bhapi_initLevel(Level level, List list, CallbackInfoReturnable<Boolean> info) {
		bhapi_currentLevel = level;
	}
	
	@Inject(method = "getPositionWithOffset(Lnet/minecraft/level/Level;II)Lnet/minecraft/util/maths/BlockPos;", at = @At("HEAD"))
	private static void bhapi_initLevel(Level level, int px, int pz, CallbackInfoReturnable<BlockPos> info) {
		bhapi_currentLevel = level;
	}
	
	@ModifyConstant(method = {
		"spawnMonsters(Lnet/minecraft/level/Level;Ljava/util/List;)Z",
		"getPositionWithOffset(Lnet/minecraft/level/Level;II)Lnet/minecraft/util/maths/BlockPos;"
	}, constant = @Constant(intValue = 128))
	private static int bhapi_changeMaxHeight(int value) {
		return LevelHeightProvider.cast(bhapi_currentLevel).getLevelHeight();
	}
}
