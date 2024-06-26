package net.bhapi.blockstate;

import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.BlockUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.level.BlockView;
import net.minecraft.level.Level;

import java.util.List;
import java.util.Random;

public interface BlockStateContainer {
	/**
	 * Get default {@link BlockState} for this block.
	 */
	default BlockState bhapi_getDefaultState() {
		return null;
	}
	
	/**
	 * Set default {@link BlockState} for this block.
	 */
	default void bhapi_setDefaultState(BlockState state) {}
	
	/**
	 * Add additional properties into block. Properties should be added into input list.
	 * @param properties {@link List} of available {@link StateProperty}
	 */
	default void bhapi_appendProperties(List<StateProperty<?>> properties) {}
	
	/**
	 * Get {@link BlockSounds} for specified {@link BlockState}.
	 * @param state current {@link BlockState}
	 * @return {@link BlockSounds}
	 */
	default BlockSounds bhapi_getSounds(BlockState state) {
		return state.getBlock().sounds;
	}
	
	/**
	 * Check if specified {@link BlockState} has random ticks.
	 * Example of blocks with random ticks: saplings, crops, grass blocks.
	 * @param state current {@link BlockState}
	 * @return {@code true} if state has random ticks and {@code false} if not
	 */
	default boolean bhapi_hasRandomTicks(BlockState state) {
		return BlockUtil.getInfo(state.getBlock()).ticksRandomly();
	}
	
	/**
	 * Applied on random or scheduled ticks.
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param random {@link Random}
	 * @param state {@link BlockState}
	 */
	default void bhapi_onScheduledTick(Level level, int x, int y, int z, Random random, BlockState state) {
		state.getBlock().onScheduledTick(level, x, y, z, random);
	}
	
	/**
	 * Check if specified {@link BlockState} is full opaque block (example: stone).
	 * @param state current {@link BlockState}
	 * @return {@code true} if state is opaque and {@code false} if not
	 */
	default boolean bhapi_isFullOpaque(BlockState state) {
		return BlockUtil.getInfo(state.getBlock()).fullOpaque();
	}
	
	/**
	 * Check if specified {@link BlockState} has {@link net.minecraft.block.entity.BlockEntity} (examples: furnace, sign).
	 * @param state current {@link BlockState}
	 * @return {@code true} if state has entity and {@code false} if not
	 */
	default boolean bhapi_hasBlockEntity(BlockState state) {
		return BlockUtil.getInfo(state.getBlock()).hasBlockEntity();
	}
	
	/**
	 * Get {@link BlockState} light opacity, determines how light will be shadowed by block during transition.
	 * Transparent blocks have this value equal to zero, water = 3, leaves = 1, opaque blocks = 255.
	 * @param state current {@link BlockState}
	 * @return {@code integer} value of light opacity
	 */
	default int bhapi_getLightOpacity(BlockState state) {
		return BlockUtil.getInfo(state.getBlock()).lightOpacity();
	}
	
	/**
	 * Checks if {@link BlockState} allows grass blocks to grow under it.
	 * Opaque blocks have this value equal to {@code false}.
	 * If this value is false grass block below current state will be transformed into dirt.
	 * @param state current {@link BlockState}
	 * @return {@code true} if state allows grass growing and {@code false} if not
	 */
	default boolean bhapi_allowsGrasUnder(BlockState state) {
		return BlockUtil.getInfo(state.getBlock()).allowsGrassUnder();
	}
	
	/**
	 * Get light value of this {@link BlockState}. 0 is no light and 15 is full-brightness light.
	 * @param state current {@link BlockState}
	 * @return {@code integer} value of emittance in [0-15] range
	 */
	default int bhapi_getEmittance(BlockState state) {
		return BlockUtil.getInfo(state.getBlock()).emittance();
	}
	
	/**
	 * Get current state hardness, used in digging time calculations.
	 * @param state current {@link BlockState}
	 * @return {@code float} hardness value
	 */
	default float bhapi_getHardness(BlockState state) {
		return state.getBlock().getHardness();
	}
	
	/**
	 * Get current state hardness for specific {@link PlayerEntity}, used in digging time calculations.
	 * @param state current {@link BlockState}
	 * @param player current {@link PlayerEntity}
	 * @return {@code float} hardness value
	 */
	default float bhapi_getHardness(BlockState state, PlayerEntity player) {
		return state.getBlock().getHardness(player);
	}
	
	/**
	 * Get state blast resistance, used in digging explosions calculations.
	 * @param state current {@link BlockState}
	 * @param entity current {@link Entity} (explosion cause)
	 * @return {@code float} blast resistance value
	 */
	default float bhapi_getBlastResistance(BlockState state, Entity entity) {
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
	default void bhapi_onBlockRemoved(Level level, int x, int y, int z, BlockState oldState, BlockState newState) {
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
	default void bhapi_onBlockPlaced(Level level, int x, int y, int z, BlockState state) {
		state.getBlock().onBlockPlaced(level, x, y, z);
	}
	
	/**
	 * Called after state is placed in the world.
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param facing Facing uvID
	 * @param state {@link BlockState} that was placed
	 */
	default void bhapi_onBlockPlaced(Level level, int x, int y, int z, int facing, BlockState state) {
		state.getBlock().onBlockPlaced(level, x, y, z, facing);
	}
	
	/**
	 * Called when block neighbour is updated
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param facing Direction (from target block)
	 * @param state Self {@link BlockState}
	 * @param neighbour Neighbour {@link BlockState}
	 */
	default void bhapi_onNeighbourBlockUpdate(Level level, int x, int y, int z, BlockDirection facing, BlockState state, BlockState neighbour) {
		state.getBlock().onAdjacentBlockUpdate(level, x, y, z, neighbour.getBlock().id);
	}
	
	/**
	 * Check if specified {@link BlockState} has redstone power.
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param facing {@link BlockDirection}
	 * @param state {@link BlockState} that is checked
	 * @return {@code true} if blockstate has redstone power
	 */
	default boolean bhapi_isPowered(Level level, int x, int y, int z, BlockDirection facing, BlockState state) {
		return state.getBlock().isPowered(level, x, y, z, facing.ordinal());
	}
	
	/**
	 * Check if that state will block face rendering from target blockstate or not.
	 * @param blockView {@link BlockView} as a block getter
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param facing {@link BlockDirection} from target to this block
	 * @param state self {@link BlockState}
	 * @param target {@link BlockState} target to check rendering
	 * @return {@code true} if face should be rendered and {@code false} if not
	 */
	@Environment(EnvType.CLIENT)
	default boolean bhapi_isSideRendered(BlockView blockView, int x, int y, int z, BlockDirection facing, BlockState state, BlockState target) {
		return target.getBlock().isSideRendered(blockView, x, y, z, facing.getFacing());
	}
	
	static BlockStateContainer cast(Object obj) {
		return (BlockStateContainer) obj;
	}
}
