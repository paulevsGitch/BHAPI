package net.bhapi.client.render.color;

import net.bhapi.blockstate.BlockState;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.FoliageColor;
import net.minecraft.client.render.block.GrassColor;
import net.minecraft.item.ItemStack;
import net.minecraft.level.gen.BiomeSource;
import net.minecraft.util.maths.MathHelper;

@Environment(EnvType.CLIENT)
public class VanillaColorProviders {
	private static final int GRASS_COLOR = GrassColor.getGrassColor(0.5, 0.5);
	
	public static final ColorProvider<BlockState> GRASS_BLOCK_COLOR = (view, x, y, z, state) -> {
		BiomeSource source = view.getBiomeSource();
		source.getBiomes(MathHelper.floor(x), MathHelper.floor(z), 1, 1);
		double temperature = source.temperatureNoises[0];
		double wetness = source.rainfallNoises[0];
		temperature = MathUtil.clamp(temperature, 0, 1);
		wetness = MathUtil.clamp(wetness, 0, 1);
		return GrassColor.getGrassColor(temperature, wetness);
	};
	
	public static final ColorProvider<BlockState> FOLIAGE_BLOCK_COLOR = (view, x, y, z, state) -> {
		int meta = state.getMeta() & 3;
		if (meta == 2) return FoliageColor.getBirchColor();
		BiomeSource source = view.getBiomeSource();
		source.getBiomes(MathHelper.floor(x), MathHelper.floor(z), 1, 1);
		double temperature = source.temperatureNoises[0];
		double wetness = source.rainfallNoises[0];
		temperature = MathUtil.clamp(temperature, 0, 1);
		wetness = MathUtil.clamp(wetness, 0, 1);
		return FoliageColor.getFoliageColor(temperature, wetness);
	};
	
	public static final ColorProvider<BlockState> SPRUCE_BLOCK_COLOR = (view, x, y, z, state) -> FoliageColor.getSpruceColor();
	public static final ColorProvider<ItemStack> GRASS_ITEM_COLOR = (view, x, y, z, stack) -> GRASS_COLOR;
}
