package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.level.BlockView;
import net.minecraft.level.gen.BiomeSource;

public class BlockItemView implements BlockView {
	private BlockState state;
	
	public void setBlockState(BlockState state) {
		this.state = state;
	}
	
	@Override
	public int getBlockId(int i, int j, int k) {
		return state.getBlock().id;
	}
	
	@Override
	public BaseBlockEntity getBlockEntity(int i, int j, int k) {
		return null;
	}
	
	@Override
	public float getLight(int i, int j, int k, int l) {
		return 1.0F;
	}
	
	@Override
	public float getBrightness(int i, int j, int k) {
		return 1.0F;
	}
	
	@Override
	public int getBlockMeta(int i, int j, int k) {
		return 0;
	}
	
	@Override
	public Material getMaterial(int i, int j, int k) {
		return state.getMaterial();
	}
	
	@Override
	public boolean isFullOpaque(int i, int j, int k) {
		return state.isFullOpaque();
	}
	
	@Override
	public boolean canSuffocate(int i, int j, int k) {
		return state.isFullOpaque();
	}
	
	@Override
	public BiomeSource getBiomeSource() {
		return BHAPIClient.getMinecraft().level.getBiomeSource();
	}
}
