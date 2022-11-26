package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.util.BlockDirection;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.material.Material;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin extends BaseBlock implements BlockStateContainer {
	protected DispenserBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Shadow public abstract int getTickrate();
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_6);
	}
	
	@Override
	public void onNeighbourBlockUpdate(Level level, int x, int y, int z, BlockDirection facing, BlockState state, BlockState neighbour) {
		if (!neighbour.isAir() && neighbour.emitsPower()) {
			if (level.hasRedstonePower(x, y, z) || level.hasRedstonePower(x, y + 1, z)) {
				level.scheduleTick(x, y, z, this.id, this.getTickrate());
			}
		}
	}
}

