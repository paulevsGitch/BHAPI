package net.bhapi.level;

public interface ChunkSectionProvider {
	ChunkSection[] getChunkSections();
	
	default ChunkSection getChunkSection(int index) {
		return getChunkSections()[index];
	}
	
	static ChunkSectionProvider cast(Object obj){
		return (ChunkSectionProvider) obj;
	}
}
