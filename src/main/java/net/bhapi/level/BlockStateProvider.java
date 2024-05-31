package net.bhapi.level;

import net.bhapi.blockstate.BlockState;
import net.bhapi.storage.Vec3I;

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
	boolean bhapi_setBlockState(int x, int y, int z, BlockState state, boolean update);
	
	/**
	 * Set {@link BlockState} at specified position. Will return {@code true} on success.
	 * @param pos {@link Vec3I} position
	 * @param state {@link BlockState} to set
	 * @param update will block trigger updates or not.
	 * @return {@code true} on success
	 */
	default boolean bhapi_setBlockState(Vec3I pos, BlockState state, boolean update) {
		return bhapi_setBlockState(pos.x, pos.y, pos.z, state, update);
	}
	
	/**
	 * Set {@link BlockState} at specified position. Will return {@code true} on success.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param state {@link BlockState} to set
	 * @return {@code true} on success
	 */
	default boolean bhapi_setBlockState(int x, int y, int z, BlockState state) {
		return bhapi_setBlockState(x, y, z, state, true);
	}
	
	/**
	 * Set {@link BlockState} at specified position. Will return {@code true} on success.
	 * @param pos {@link Vec3I} position
	 * @param state {@link BlockState} to set
	 * @return {@code true} on success
	 */
	default boolean bhapi_setBlockState(Vec3I pos, BlockState state) {
		return bhapi_setBlockState(pos.x, pos.y, pos.z, state);
	}
	
	/**
	 * Get {@link BlockState} at specified position.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @return {@link BlockState}
	 */
	BlockState bhapi_getBlockState(int x, int y, int z);
	
	/**
	 * Get {@link BlockState} at specified position.
	 * @param pos {@link Vec3I} position
	 * @return {@link BlockState}
	 */
	default BlockState bhapi_getBlockState(Vec3I pos) {
		return bhapi_getBlockState(pos.x, pos.y, pos.z);
	}
	
	static BlockStateProvider cast(Object obj) {
		return (BlockStateProvider) obj;
	}
}
