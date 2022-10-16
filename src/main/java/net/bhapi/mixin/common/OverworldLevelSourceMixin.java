package net.bhapi.mixin.common;

import net.bhapi.level.ChunkSectionProvider;
import net.bhapi.util.ChunkSection;
import net.minecraft.block.BaseBlock;
import net.minecraft.level.Level;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.source.OverworldLevelSource;
import net.minecraft.util.noise.PerlinOctaveNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

@Mixin(OverworldLevelSource.class)
public class OverworldLevelSourceMixin {
	@Shadow private Level level;
	@Shadow private PerlinOctaveNoise interpolationNoise;
	
	@Unique private final short[] bhapi_heightmap = new short[256];
	@Unique private final ForkJoinPool customPool = new ForkJoinPool(8);
	
	@Inject(method = "getChunk", at = @At("HEAD"), cancellable = true)
	private void bhapi_getChunk(int chunkX, int chunkZ, CallbackInfoReturnable<Chunk> info) {
		Chunk chunk = new Chunk(this.level, chunkX, chunkZ);
		info.setReturnValue(chunk);
		
		short height = 256;
		short delta = (short) (height - 40);
		ChunkSection[] sections = ChunkSectionProvider.cast(chunk).getChunkSections();
		
		short maxHeight = 64; // Water Level
		for (short i = 0; i < 256; i++) {
			int px = chunkX << 4 | (i & 15);
			int pz = chunkZ << 4 | (i >> 4);
			float noise = getNoise(px * 0.1, pz * 0.1);
			noise += getNoise(px * 0.5, pz * 0.5) * 0.25F;
			height = (short) (noise * delta + 32);
			bhapi_heightmap[i] = height;
			if (height > maxHeight) maxHeight = height;
		}
		
		final short count = (short) Math.ceil(maxHeight / 16F);
		customPool.submit(() -> IntStream.range(0, count).parallel().forEach(index -> {
			ChunkSection section = new ChunkSection();
			sections[index] = section;
			short secY = (short) (index << 4);
			for (short i = 0; i < 256; i++) {
				byte x = (byte) (i & 15);
				byte z = (byte) (i >> 4);
				short maxY = (short) (bhapi_heightmap[i] - secY);
				if (maxY < 0) continue;
				short dirtLevel = (short) (bhapi_heightmap[i] - 4);
				short grassLevel = (short) (bhapi_heightmap[i] - 1);
				if (maxY > 16) maxY = 16;
				for (short y = 0; y < maxY; y++) {
					short py = (short) (secY | y);
					if (py > bhapi_heightmap[i] && py < 64) section.setID(x, y, z, BaseBlock.STILL_WATER.id);
					else if (py == grassLevel) section.setID(x, y, z, BaseBlock.GRASS.id);
					else if (py > dirtLevel) section.setID(x, y, z, BaseBlock.DIRT.id);
					else section.setID(x, y, z, BaseBlock.STONE.id);
				}
			}
		}));
		
		/*for (byte x = 0; x < 16; x++) {
			int px = chunkX << 4 | x;
			for (byte z = 0; z < 16; z++) {
				int pz = chunkZ << 4 | z;
				float noise = getNoise(px * 0.1, pz * 0.1);
				noise += getNoise(px * 0.5, pz * 0.5) * 0.25F;
				height = (short) (noise * delta + 32);
				short dirtLevel = (short) (height - 3);
				short grassLevel = (short) (height - 1);
				for (short y = 0; y < dirtLevel; y++) {
					setter.setBlockFast(x, y, z, BaseBlock.STONE.id);
				}
				if (height < 62) {
					for (short y = dirtLevel; y <= grassLevel; y++) {
						setter.setBlockFast(x, y, z, BaseBlock.GRAVEL.id);
					}
					for (short y = height; y < 62; y++) {
						setter.setBlockFast(x, y, z, BaseBlock.STILL_WATER.id);
					}
				}
				else {
					for (short y = dirtLevel; y < grassLevel; y++) {
						setter.setBlockFast(x, y, z, BaseBlock.DIRT.id);
					}
					setter.setBlockFast(x, grassLevel, z, BaseBlock.GRASS.id);
				}
			}
		}*/
		
		chunk.generateHeightmap();
	}
	
	private float getNoise(double x, double z) {
		float noise = (float) interpolationNoise.sample(x, z);
		return (noise + 150.0F) / 300.0F;
	}
}
