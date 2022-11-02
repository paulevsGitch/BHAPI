package net.bhapi.block;

import net.bhapi.util.BlockUtil;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.material.Material;

public class BHBaseBlock extends BaseBlock {
	public BHBaseBlock(Material material) {
		super(BlockUtil.MOD_BLOCK_ID, material);
	}
}
