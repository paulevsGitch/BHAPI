package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.properties.BlockPropertyType;
import net.bhapi.blockstate.properties.IntegerProperty;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.level.BlockView;
import net.minecraft.level.biome.BaseBiome;
import net.minecraft.level.gen.BiomeSource;
import net.minecraft.level.gen.FixedBiomeSource;

public class BlockItemView implements BlockView {
	private static final BiomeSource BIOME_SOURCE = new FixedBiomeSource(BaseBiome.PLAINS, 0.5F, 0.5F);
	private BlockState state;
	private int meta;
	
	public void setBlockState(BlockState state) {
		if (this.state == state) return;
		this.state = state;
		StateProperty<?> property = state.getProperty("meta");
		if (property != null && property.getType() == BlockPropertyType.INTEGER) {
			meta = state.getValue((IntegerProperty) property);
		}
		else meta = 0;
	}
	
	@Override
	public int getBlockId(int i, int j, int k) {
		return state == null ? 0 : state.getBlock().id;
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
}
