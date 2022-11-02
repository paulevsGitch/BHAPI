package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.FarmlandBlock;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin implements BlockStateContainer {
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_8);
	}
}

