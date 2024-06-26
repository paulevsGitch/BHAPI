package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.util.BlockUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.level.BlockView;
import net.minecraft.level.biome.Biome;
import net.minecraft.level.biome.BiomeSource;
import net.minecraft.level.biome.FixedBiomeSource;

@Environment(EnvType.CLIENT)
public class BlockItemView implements BlockView, BlockStateProvider {
	private static final BiomeSource BIOME_SOURCE = new FixedBiomeSource(Biome.PLAINS, 0.5F, 0.5F);
	private BlockState state;
	private int meta;
	
	public void setBlockState(BlockState state) {
		if (this.state == state) return;
		this.state = state;
		this.meta = state.getMeta();
	}
	
	@Override
	public int getBlockID(int i, int j, int k) {
		return state == null ? 0 : state.getBlock().id;
	}
	
	@Override
	public BlockEntity getBlockEntity(int i, int j, int k) {
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
		return meta;
	}
	
	@Override
	public Material getMaterial(int i, int j, int k) {
		return state == null ? Material.AIR : state.getMaterial();
	}
	
	@Override
	public boolean isFullOpaque(int i, int j, int k) {
		return state != null && state.isFullOpaque();
	}
	
	@Override
	public boolean canSuffocate(int i, int j, int k) {
		return false;
	}
	
	@Override
	public BiomeSource getBiomeSource() {
		return BIOME_SOURCE;
	}
	
	@Override
	public boolean bhapi_setBlockState(int x, int y, int z, BlockState state, boolean update) {
		return false;
	}
	
	@Override
	public BlockState bhapi_getBlockState(int x, int y, int z) {
		return state == null ? BlockUtil.AIR_STATE : state;
	}
}
