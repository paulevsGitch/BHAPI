package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.level.LevelHeightProvider;
import net.minecraft.block.PistonBlock;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PistonBlock.class)
public abstract class PistonBlockMixin implements BlockStateContainer {
	@Unique private static Level bhapi_level;
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	@ModifyConstant(method = {
		"pushByPiston",
		"canMoveBlock(Lnet/minecraft/level/Level;IIII)Z"
	}, constant = @Constant(intValue = 127))
	private static int bhapi_changeMaxBlockHeight(int value) {
		return getLevelHeight() - 1;
	}
	
	@Inject(method = "pushByPiston", at = @At("HEAD"))
	private void bhapi_pushByPiston(Level level, int i, int j, int k, int l, CallbackInfoReturnable<Boolean> info) {
		bhapi_level = level;
	}
	
	@Inject(method = "canMoveBlock(Lnet/minecraft/level/Level;IIII)Z", at = @At("HEAD"))
	private static void bhapi_canMoveBlock(Level level, int i, int j, int k, int l, CallbackInfoReturnable<Boolean> info) {
		bhapi_level = level;
	}
	
	@Unique
	private static short getLevelHeight() {
		return LevelHeightProvider.cast(bhapi_level).getLevelHeight();
	}
}

