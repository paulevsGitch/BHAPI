package net.bhapi.level;

import net.bhapi.util.ChunkSection;

public interface ChunkSectionProvider {
	ChunkSection[] getChunkSections();
	
	static ChunkSectionProvider cast(Object obj){
		return (ChunkSectionProvider) obj;
	}
}
