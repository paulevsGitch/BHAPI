package net.bhapi.level;

import net.bhapi.blockstate.BlockState;

public interface BlockStateProvider {
	/**
	 * Set {@link BlockState} at specified position. Will return {@code true} on success.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param state {@link BlockState} to set
	 * @param update will block trigger updates or not.
	 * @return {@code true} on success
	 */
	boolean setBlockState(int x, int y, int z, BlockState state, boolean update);
	
	/**
	 * Set {@link BlockState} at specified position. Will return {@code true} on success.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param state {@link BlockState} to set
	 * @return {@code true} on success
	 */
	
	default boolean setBlockState(int x, int y, int z, BlockState state) {
		return setBlockState(x, y, z, state, true);
	}
	
	/**
	 * Get {@link BlockState} at specified position.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @return {@link BlockState}
	 */
	BlockState getBlockState(int x, int y, int z);
	
	static BlockStateProvider cast(Object obj) {
		return (BlockStateProvider) obj;
	}
}
