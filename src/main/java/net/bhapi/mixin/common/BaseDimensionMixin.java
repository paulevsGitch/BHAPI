package net.bhapi.mixin.common;

import net.bhapi.level.LevelHeightProvider;
import net.minecraft.level.Level;
import net.minecraft.level.dimension.BaseDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseDimension.class)
public class BaseDimensionMixin implements LevelHeightProvider {
	@Shadow public Level level;
	
	@Inject(method = "canSpawnOn", at = @At("HEAD"), cancellable = true)
	private void bhapi_fixCanSpawnOn(int x, int z, CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(true);
	}
	
	// TODO remove this
	@Override
	public short getLevelHeight() {
		return 256;
	}
}
