package net.bhapi.mixin.common;

import net.bhapi.storage.vanilla.BlockMarker;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Stats.class)
public class StatsMixin {
	@Inject(method = "setupBlockStats", at = @At("HEAD"))
	private static void bhapi_onSetupBlockStats(CallbackInfo info) {
		BlockMarker.setVanillaInitiated();
	}
}
