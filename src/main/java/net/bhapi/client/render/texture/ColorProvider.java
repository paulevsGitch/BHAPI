package net.bhapi.client.render.texture;

import net.bhapi.blockstate.BlockState;
import net.minecraft.level.BlockView;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ColorProvider {
	int getColorMultiplier(BlockView view, double x, double y, double z, @Nullable BlockState state);
}
