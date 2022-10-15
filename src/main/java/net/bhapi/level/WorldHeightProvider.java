package net.bhapi.level;

public interface WorldHeightProvider {
	/**
	 * Get maximum available world (building) height for this dimension.
	 * Can be in range 1-32768, but it's better to use values in range 128-4096.
	 */
	default short getWorldHeight() {
		return 128;
	}
	
	/**
	 * Get maximum available amount of sections for this dimension.
	 * Each section is 16 blocks tall. Vanilla world is 8 sections tall (128 blocks height).
	 */
	default short getSectionsCount() {
		return (short) Math.ceil(getWorldHeight() / 16F);
	}
	
	static WorldHeightProvider cast(Object obj) {
		return (WorldHeightProvider) obj;
	}
}
