package net.bhapi.block;

import net.bhapi.util.BlockUtil;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;

import java.util.List;

public class BHBaseBlock extends BaseBlock implements CustomDropProvider {
	public BHBaseBlock(Material material) {
		super(BlockUtil.MOD_BLOCK_ID, material);
	}
	
	@Override
	public void getCustomDrop(Level level, int x, int y, int z, List<ItemStack> drop) {
		drop.add(new ItemStack(this));
	}
}
