package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.util.BlockDirection;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.material.Material;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(TrapdoorBlock.class)
public abstract class TrapdoorBlockMixin extends BaseBlock implements BlockStateContainer {
	@Shadow public abstract void method_1059(Level arg, int i, int j, int k, boolean bl);
	
	protected TrapdoorBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_8);
	}
	
	@Override
	public void onNeighbourBlockUpdate(Level level, int x, int y, int z, BlockDirection facing, BlockState state, BlockState neighbour) {
		if (level.isClientSide) {
			return;
		}
		int meta = state.getMeta();
		int dx = x;
		int dz = z;
		
		if ((meta & 3) == 0) ++dz;
		if ((meta & 3) == 1) --dz;
		if ((meta & 3) == 2) ++dx;
		if ((meta & 3) == 3) --dx;
		
		if (!level.canSuffocate(dx, y, dz)) {
			level.setBlock(x, y, z, 0);
			this.drop(level, x, y, z, meta);
		}
		
		if (!neighbour.isAir() && neighbour.emitsPower()) {
			boolean power = level.hasRedstonePower(x, y, z);
			this.method_1059(level, x, y, z, power);
		}
	}
}
