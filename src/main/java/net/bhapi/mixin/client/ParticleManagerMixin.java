package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.interfaces.SimpleBlockStateContainer;
import net.bhapi.registry.CommonRegistries;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.particle.DiggingParticle;
import net.minecraft.entity.BaseParticle;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
	@Shadow protected Level level;
	@Shadow private Random rand;
	
	@Shadow public abstract void addParticle(BaseParticle arg);
	
	@Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
	private void bhapi_addBlockBreakParticles(int x, int y, int z, int rawID, int meta, CallbackInfo info) {
		info.cancel();
		BlockState state = CommonRegistries.BLOCKSTATES_MAP.get(rawID);
		if (state == null) return;
		for (int dx = 0; dx < 4; ++dx) {
			for (int dy = 0; dy < 4; ++dy) {
				for (int dz = 0; dz < 4; ++dz) {
					double px = x + (dx + 0.5) / 4.0;
					double py = y + (dy + 0.5) / 4.0;
					double pz = z + (dz + 0.5) / 4.0;
					int side = this.rand.nextInt(6);
					DiggingParticle particle = new DiggingParticle(
						this.level,
						px, py, pz,
						px - x - 0.5,
						py - y - 0.5,
						pz - z - 0.5,
						state.getBlock(),
						side, meta
					).applyColor(x, y, z);
					SimpleBlockStateContainer.cast(particle).setBlockState(state);
					this.addParticle(particle);
				}
			}
		}
	}
}
