package net.bhapi.util;

import net.bhapi.block.BHAirBlock;
import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.registry.DefaultRegistries;

import java.util.HashMap;
import java.util.Map;

public class BlockUtil {
	private static final Map<Integer, BlockState> LEGACY_BLOCKS = new HashMap<>();
	public static final BHAirBlock AIR_BLOCK = new BHAirBlock();
	public static final BlockState AIR_STATE = BlockState.getDefaultState(AIR_BLOCK);
	public static final int MOD_BLOCK_ID = 255;
	
	public static void init() {
		LEGACY_BLOCKS.put(0, AIR_STATE);
		DefaultRegistries.BLOCK_REGISTRY.forEach(
			block -> LEGACY_BLOCKS.put(block.id, BlockStateContainer.cast(block).getDefaultState())
		);
	}
	
	public static BlockState getLegacyBlock(int id, int meta) {
		BlockState state = LEGACY_BLOCKS.get(id);
		if (state == null) return null;
		if (meta > 0) {
			StateProperty<?> property = state.getProperty("meta");
			if (property != null) {
				state = state.withCast(property, meta);
			}
		}
		return state;
	}
}
