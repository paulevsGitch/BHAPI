package net.bhapi.client.render.color;

import net.bhapi.blockstate.BlockState;
import net.bhapi.storage.MultiThreadStorage;
import net.bhapi.util.MathUtil;
import net.bhapi.util.UnsafeUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.block.FoliageColor;
import net.minecraft.client.render.block.GrassColor;
import net.minecraft.item.ItemStack;
import net.minecraft.level.biome.BiomeSource;
import net.minecraft.util.maths.MCMath;

@Environment(EnvType.CLIENT)
public class VanillaColorProviders {
	private static final int GRASS_COLOR = GrassColor.getGrassColor(0.5, 0.5);
	
	@SuppressWarnings("deprecation")
	private static final MultiThreadStorage<BiomeSource> BIOME_SOURCES = new MultiThreadStorage<>(
		() -> UnsafeUtil.copyObject(((Minecraft) FabricLoader.getInstance().getGameInstance()).level.getBiomeSource())
	);
	
	public static void resetSources() {
		BIOME_SOURCES.clear();
	}
	
	public static final ColorProvider<BlockState> GRASS_BLOCK_COLOR = (view, x, y, z, state) -> {
		BiomeSource source = BIOME_SOURCES.get();
		source.getBiome(MCMath.floor(x), MCMath.floor(z));
		double temperature = source.temperatureNoises[0];
		double wetness = source.rainfallNoises[0];
		temperature = MathUtil.clamp(temperature, 0, 1);
		wetness = MathUtil.clamp(wetness, 0, 1);
		return GrassColor.getGrassColor(temperature, wetness);
	};
	
	public static final ColorProvider<BlockState> FOLIAGE_BLOCK_COLOR = (view, x, y, z, state) -> {
		int meta = state.getMeta() & 3;
		if (meta == 2) return FoliageColor.getBirchColor();
		BiomeSource source = BIOME_SOURCES.get();
		source.getBiome(MCMath.floor(x), MCMath.floor(z));
		double temperature = source.temperatureNoises[0];
		double wetness = source.rainfallNoises[0];
		temperature = MathUtil.clamp(temperature, 0, 1);
		wetness = MathUtil.clamp(wetness, 0, 1);
		return FoliageColor.getFoliageColor(temperature, wetness);
	};
	
	public static final ColorProvider<BlockState> SPRUCE_BLOCK_COLOR = (view, x, y, z, state) -> FoliageColor.getSpruceColor();
	public static final ColorProvider<ItemStack> GRASS_ITEM_COLOR = (view, x, y, z, stack) -> GRASS_COLOR;
}
