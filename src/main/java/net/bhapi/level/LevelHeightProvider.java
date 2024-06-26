package net.bhapi.level;

public interface LevelHeightProvider {
	/**
	 * Get maximum available building height for this dimension.
	 * Can be in range 1-32768, but it's better to use values in range 128-4096.
	 */
	default short bhapi_getLevelHeight() {
		return 128;
	}
	
	/**
	 * Get maximum available amount of sections for this dimension.
	 * Each section is 16 blocks tall. Vanilla world is 8 sections tall (128 blocks height).
	 */
	default short bhapi_getSectionsCount() {
		return (short) Math.ceil(bhapi_getLevelHeight() / 16F);
	}
	
	static LevelHeightProvider cast(Object obj) {
		return (LevelHeightProvider) obj;
	}
}
