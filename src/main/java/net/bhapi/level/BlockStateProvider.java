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
	boolean setBlockState(int x, int y, int z, BlockState state, boolean update);
	
	/**
	 * Set {@link BlockState} at specified position. Will return {@code true} on success.
	 * @param pos {@link Vec3I} position
	 * @param state {@link BlockState} to set
	 * @param update will block trigger updates or not.
	 * @return {@code true} on success
	 */
	default boolean setBlockState(Vec3I pos, BlockState state, boolean update) {
		return setBlockState(pos.x, pos.y, pos.z, state, update);
	}
	
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
	 * Set {@link BlockState} at specified position. Will return {@code true} on success.
	 * @param pos {@link Vec3I} position
	 * @param state {@link BlockState} to set
	 * @return {@code true} on success
	 */
	default boolean setBlockState(Vec3I pos, BlockState state) {
		return setBlockState(pos.x, pos.y, pos.z, state);
	}
	
	/**
	 * Get {@link BlockState} at specified position.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @return {@link BlockState}
	 */
	BlockState getBlockState(int x, int y, int z);
	
	/**
	 * Get {@link BlockState} at specified position.
	 * @param pos {@link Vec3I} position
	 * @return {@link BlockState}
	 */
	default BlockState getBlockState(Vec3I pos) {
		return getBlockState(pos.x, pos.y, pos.z);
	}
	
	static BlockStateProvider cast(Object obj) {
		return (BlockStateProvider) obj;
	}
}
