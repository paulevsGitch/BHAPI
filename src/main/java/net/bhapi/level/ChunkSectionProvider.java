package net.bhapi.level;

public interface ChunkSectionProvider {
	ChunkSection[] getChunkSections();
	
	static ChunkSectionProvider cast(Object obj){
		return (ChunkSectionProvider) obj;
	}
}
