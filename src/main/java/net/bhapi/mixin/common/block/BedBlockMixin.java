package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin implements BlockStateContainer {
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_8);
	}
}

