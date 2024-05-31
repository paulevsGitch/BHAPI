package net.bhapi.mixin.common.structure;

import net.minecraft.level.Level;
import net.minecraft.level.structure.DungeonStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

// Not required (yet)
@Mixin(DungeonStructure.class)
public class DungeonMixin {
	@Inject(method = "generate", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/level/Level;setBlock(IIII)Z",
		ordinal = 4
	), locals = LocalCapture.CAPTURE_FAILHARD)
	private void bhapi_fixChestEntity(Level arg, Random random, int i, int j, int k, CallbackInfoReturnable<Boolean> info, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15) {
	
	}
}
