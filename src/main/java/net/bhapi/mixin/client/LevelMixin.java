package net.bhapi.mixin.client;

import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Shadow public abstract int getBlockId(int i, int j, int k);
	
	@Shadow public abstract int getLight(int i, int j, int k);
	
	@Shadow public abstract int getLight(LightType arg, int i, int j, int k);
	
	@Inject(method = "shuffleSpawnPoint", at = @At("HEAD"), cancellable = true)
	private void bhapi_fixShuffleSpawnPoint(CallbackInfo info) {
		info.cancel();
	}
}
