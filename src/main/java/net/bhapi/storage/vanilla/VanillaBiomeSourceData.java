package net.bhapi.storage.vanilla;

import net.minecraft.level.biome.Biome;
import net.minecraft.util.noise.SimplexOctaveNoise;

import java.util.Random;

public class VanillaBiomeSourceData {
	public final SimplexOctaveNoise temperatureNoise;
	public final SimplexOctaveNoise rainfallNoise;
	public final SimplexOctaveNoise detailNoise;
	public double[] temperatureNoises;
	public double[] rainfallNoises;
	public double[] detailNoises;
	public Biome[] biomes;
	
	public VanillaBiomeSourceData(long seed) {
		this.temperatureNoise = new SimplexOctaveNoise(new Random(seed * 9871L), 4);
		this.rainfallNoise = new SimplexOctaveNoise(new Random(seed * 39811L), 4);
		this.detailNoise = new SimplexOctaveNoise(new Random(seed * 543321L), 2);
	}
}
