package net.bhapi.blockstate;

import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.BlockSounds;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.player.PlayerBase;

import java.util.List;

public interface BlockStateContainer {
	BlockState getDefaultState();
	void setDefaultState(BlockState state);
	default void appendProperties(List<StateProperty> properties) {}
	default BlockSounds getSounds(BlockState state) { return state.getBlock().sounds; }
	default boolean hasRandomTicks(BlockState state) { return false; }
	default boolean isFullOpaque(BlockState state) { return true; }
	default boolean hasTileEntity(BlockState state) { return false; }
	default int getLightOpacity(BlockState state) { return 0; }
	default boolean allowsGrasUnder(BlockState state) { return false; }
	default int getEmittance(BlockState state) { return 0; }
	default float getHardness(BlockState state) { return state.getBlock().getHardness(); }
	default float getHardness(BlockState state, PlayerBase player) { return state.getBlock().getHardness(player); }
	default float getBlastResistance(BlockState state, BaseEntity entity) { return state.getBlock().getBlastResistance(entity); }
}
