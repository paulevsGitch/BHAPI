package net.bhapi.mixin.client;

import net.bhapi.level.LevelHeightProvider;
import net.minecraft.level.dimension.Dimension;
import net.minecraft.level.dimension.OverworldDimension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OverworldDimension.class)
public abstract class OverworldDimensionMixin extends Dimension implements LevelHeightProvider {
	// TODO move into weather API
	/*@Override
	public float getCloudHeight() {
		return getLevelHeight() + 1;
	}*/
}
