package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Shadow public Random random;
	@Unique private Random bhapi_clientRandom = new Random();
	
	@Inject(method = "shuffleSpawnPoint", at = @At("HEAD"), cancellable = true)
	private void bhapi_fixShuffleSpawnPoint(CallbackInfo info) {
		info.cancel();
	}
	
	@Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
	public void randomDisplayTick(int x, int y, int z, CallbackInfo info) {
		info.cancel();
		for (int count = 0; count < 1000; ++count) {
			int px = x + this.bhapi_clientRandom.nextInt(32) - 16;
			int py = y + this.bhapi_clientRandom.nextInt(32) - 16;
			int pz = z + this.bhapi_clientRandom.nextInt(32) - 16;
			BlockState state = BlockStateProvider.cast(this).getBlockState(px, py, pz);
			if (!state.hasRandomTicks()) continue;
			state.getBlock().randomDisplayTick(Level.class.cast(this), px, py, pz, bhapi_clientRandom);
		}
	}
}
