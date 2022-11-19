package net.bhapi.util;

import net.bhapi.block.BHAirBlock;
import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.storage.ExpandableArray;
import net.minecraft.block.BaseBlock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BlockUtil {
	private static final Map<BaseBlock, BlockInfo> INFO_MAP = new HashMap<>();
	private static final BlockInfo DEFAULT = new BlockInfo(false, true, false, 255, false, 0);
	
	private static final ExpandableArray<BlockState> LEGACY_BLOCKS = new ExpandableArray<>();
	public static final BHAirBlock AIR_BLOCK = new BHAirBlock();
	public static final BlockState AIR_STATE = BlockState.getDefaultState(AIR_BLOCK);
	public static final int MOD_BLOCK_ID = 255;
	public static BlockState brokenBlock;
	
	public static void init() {
		Arrays.stream(BaseBlock.BY_ID).filter(Objects::nonNull).forEach(block -> {
			int id = block.id;
			INFO_MAP.put(block, new BlockInfo(
				BaseBlock.TICKS_RANDOMLY[id],
				BaseBlock.FULL_OPAQUE[id],
				BaseBlock.HAS_TILE_ENTITY[id],
				BaseBlock.LIGHT_OPACITY[id],
				BaseBlock.ALLOWS_GRASS_UNDER[id],
				BaseBlock.EMITTANCE[id]
			));
		});
		LEGACY_BLOCKS.put(0, AIR_STATE);
	}
	
	public static BlockState getLegacyBlock(int id, int meta) {
		BlockState state = LEGACY_BLOCKS.get(id);
		if (state == null) return null;
		if (meta > 0) {
			StateProperty<?> property = state.getProperty("meta");
			if (property != null) {
				state = state.with(property, meta);
			}
		}
		return state;
	}
	
	public static BlockInfo getInfo(BaseBlock block) {
		if (block.id == BlockUtil.MOD_BLOCK_ID) return DEFAULT;
		return INFO_MAP.getOrDefault(block, DEFAULT);
	}
	
	public record BlockInfo(
		boolean ticksRandomly,
		boolean fullOpaque,
		boolean hasBlockEntity,
		int lightOpacity,
		boolean allowsGrassUnder,
		int emittance
	) {}
	
	static {
		Arrays.stream(BaseBlock.BY_ID).filter(Objects::nonNull).forEach(
			block -> LEGACY_BLOCKS.put(block.id, BlockStateContainer.cast(block).getDefaultState())
		);
	}
}
