package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.SaplingBlock;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockMixin implements BlockStateContainer {
	@Shadow public abstract void growTree(Level arg, int i, int j, int k, Random random);
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16); // Saplings use | 8 for some states
	}
	
	@Inject(method = "onScheduledTick", at = @At("HEAD"), cancellable = true)
	private void bhapi_onScheduledTick(Level arg, int i, int j, int k, Random random, CallbackInfo info) {
		this.growTree(arg, i, j, k, random);
	}
}

