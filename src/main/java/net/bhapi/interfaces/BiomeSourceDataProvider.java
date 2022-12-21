package net.bhapi.interfaces;

import net.bhapi.storage.vanilla.VanillaBiomeSourceData;
import net.minecraft.level.gen.BiomeSource;

public interface BiomeSourceDataProvider {
	VanillaBiomeSourceData getBiomeSourceData();
	
	static BiomeSourceDataProvider cast(BiomeSource source) {
		return (BiomeSourceDataProvider) source;
	}
}
