package net.bhapi.mixin.common;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.ChunkSection;
import net.bhapi.level.ChunkSectionProvider;
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
		
		ChunkSection[] sections = ChunkSectionProvider.cast(chunk).getChunkSections();
		
		short height;
		short maxHeight = 64; // Water Level
		for (short i = 0; i < 256; i++) {
			int px = chunkX << 4 | (i & 15);
			int pz = chunkZ << 4 | (i >> 4);
			float noise = getNoise(px * 0.4, pz * 0.4);
			noise += getNoise(px * 2, pz * 2) * 0.25F;
			height = (short) (noise * 200 + 16);
			bhapi_heightmap[i] = height;
			if (height > maxHeight) maxHeight = height;
		}
		
		maxHeight = (short) ((maxHeight + 16) >> 4);
		if (maxHeight >= sections.length) maxHeight = (short) (sections.length - 1);
		final short count = maxHeight;
		
		BlockState water = BlockState.getDefaultState(BaseBlock.STILL_WATER);
		BlockState stone = BlockState.getDefaultState(BaseBlock.STONE);
		BlockState grass = BlockState.getDefaultState(BaseBlock.GRASS);
		BlockState dirt = BlockState.getDefaultState(BaseBlock.DIRT);
		
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
					if (py > bhapi_heightmap[i] && py < 64) section.setBlockState(x, y, z, water);
					else if (py == grassLevel) section.setBlockState(x, y, z, grass);
					else if (py > dirtLevel) section.setBlockState(x, y, z, dirt);
					else section.setBlockState(x, y, z, stone);
				}
			}
		}));
		
		chunk.generateHeightmap();
	}
	
	private float getNoise(double x, double z) {
		float noise = (float) interpolationNoise.sample(x, z);
		return (noise + 150.0F) / 300.0F;
	}
}
