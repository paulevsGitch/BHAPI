package net.bhapi.level;

import net.minecraft.level.chunk.Chunk;

public interface ChunkHeightProvider {
	short getHeightmapData(int x, int z);
	
	static ChunkHeightProvider cast(Chunk chunk) {
		return (ChunkHeightProvider) chunk;
	}
}
