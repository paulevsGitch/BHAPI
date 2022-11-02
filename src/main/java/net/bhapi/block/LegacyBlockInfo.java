package net.bhapi.block;

import net.minecraft.block.BaseBlock;

import java.util.HashMap;
import java.util.Map;

public class LegacyBlockInfo {
	private static final Map<BaseBlock, BlockInfo> INFO_MAP = new HashMap<>();
	private static final BlockInfo DEFAULT = new BlockInfo(false, true, false, 0, false, 0);
	
	public static void init() {
		for (short i = 0; i < 256; i++) {
			BaseBlock block = BaseBlock.BY_ID[i];
			if (block != null) {
				INFO_MAP.put(block, new BlockInfo(
					BaseBlock.TICKS_RANDOMLY[i],
					BaseBlock.FULL_OPAQUE[i],
					BaseBlock.HAS_TILE_ENTITY[i],
					BaseBlock.LIGHT_OPACITY[i],
					BaseBlock.ALLOWS_GRASS_UNDER[i],
					BaseBlock.EMITTANCE[i]
				));
			}
		}
	}
	
	public static BlockInfo getInfo(BaseBlock block) {
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
}
