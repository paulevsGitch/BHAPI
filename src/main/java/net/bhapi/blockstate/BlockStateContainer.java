package net.bhapi.blockstate;

import net.bhapi.block.LegacyBlockInfo;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.BlockSounds;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.level.Level;

import java.util.List;

public interface BlockStateContainer {
	BlockState getDefaultState();
	void setDefaultState(BlockState state);
	
	/**
	 * Add additional properties into block. Properties should be added into input list.
	 * @param properties {@link List} of available {@link StateProperty}
	 */
	default void appendProperties(List<StateProperty<?>> properties) {}
	
	/**
	 * Get {@link BlockSounds} for specified {@link BlockState}.
	 * @param state current {@link BlockState}
	 * @return {@link BlockSounds}
	 */
	default BlockSounds getSounds(BlockState state) {
		return state.getBlock().sounds;
	}
	
	/**
	 * Check if specified {@link BlockState} has random ticks.
	 * Example of blocks with random ticks: saplings, crops, grass blocks.
	 * @param state current {@link BlockState}
	 * @return {@code true} if state has random ticks and {@code false} if not
	 */
	default boolean hasRandomTicks(BlockState state) {
		return LegacyBlockInfo.getInfo(state.getBlock()).ticksRandomly();
	}
	
	/**
	 * Check if specified {@link BlockState} if full opaque block (example: stone).
	 * @param state current {@link BlockState}
	 * @return {@code true} if state is opaque and {@code false} if not
	 */
	default boolean isFullOpaque(BlockState state) {
		return LegacyBlockInfo.getInfo(state.getBlock()).fullOpaque();
	}
	
	/**
	 * Check if specified {@link BlockState} has {@link net.minecraft.block.entity.BaseBlockEntity} (examples: furnace, sign).
	 * @param state current {@link BlockState}
	 * @return {@code true} if state has entity and {@code false} if not
	 */
	default boolean hasBlockEntity(BlockState state) {
		return LegacyBlockInfo.getInfo(state.getBlock()).hasBlockEntity();
	}
	
	/**
	 * Get {@link BlockState} light opacity, determines how light will be shadowed by block during transition.
	 * Transparent blocks have this value equal to zero, water = 3, leaves = 1, opaque blocks = 255.
	 * @param state current {@link BlockState}
	 * @return {@code integer} value of light opacity
	 */
	default int getLightOpacity(BlockState state) {
		return LegacyBlockInfo.getInfo(state.getBlock()).lightOpacity();
	}
	
	/**
	 * Checks if {@link BlockState} allows grass blocks to grow under it.
	 * Opaque blocks have this value equal to {@code false}.
	 * If this value is false grass block below current state will be transformed into dirt.
	 * @param state current {@link BlockState}
	 * @return {@code true} if state allows grass growing and {@code false} if not
	 */
	default boolean allowsGrasUnder(BlockState state) {
		return LegacyBlockInfo.getInfo(state.getBlock()).allowsGrassUnder();
	}
	
	/**
	 * Get light value of this {@link BlockState}. 0 is no light and 15 is full-brightness light.
	 * @param state current {@link BlockState}
	 * @return {@code integer} value of emittance in [0-15] range
	 */
	default int getEmittance(BlockState state) {
		return LegacyBlockInfo.getInfo(state.getBlock()).emittance();
	}
	
	/**
	 * Get current state hardness, used in digging time calculations.
	 * @param state current {@link BlockState}
	 * @return {@code float} hardness value
	 */
	default float getHardness(BlockState state) {
		return state.getBlock().getHardness();
	}
	
	/**
	 * Get current state hardness for specific {@link PlayerBase}, used in digging time calculations.
	 * @param state current {@link BlockState}
	 * @param player current {@link PlayerBase}
	 * @return {@code float} hardness value
	 */
	default float getHardness(BlockState state, PlayerBase player) {
		return state.getBlock().getHardness(player);
	}
	
	/**
	 * Get state blast resistance, used in digging explosions calculations.
	 * @param state current {@link BlockState}
	 * @param entity current {@link BaseEntity} (explosion cause)
	 * @return {@code float} blast resistance value
	 */
	default float getBlastResistance(BlockState state, BaseEntity entity) {
		return state.getBlock().getBlastResistance(entity);
	}
	
	/**
	 * Called after state is removed from the world.
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param oldState {@link BlockState} that will be removed
	 * @param newState {@link BlockState} that will replace old state
	 */
	default void onBlockRemoved(Level level, int x, int y, int z, BlockState oldState, BlockState newState) {
		oldState.getBlock().onBlockRemoved(level, x, y, z);
	}
	
	/**
	 * Called after state is placed in the world.
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param state {@link BlockState} that was placed
	 */
	default void onBlockPlaced(Level level, int x, int y, int z, BlockState state) {
		state.getBlock().onBlockPlaced(level, x, y, z);
	}
	
	static BlockStateContainer cast(BaseBlock block) {
		return (BlockStateContainer) block;
	}
}
