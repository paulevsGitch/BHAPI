package net.bhapi.client.render.color;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.BlockView;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ColorProvider<T> {
	int getColorMultiplier(BlockView view, double x, double y, double z, T data);
}
