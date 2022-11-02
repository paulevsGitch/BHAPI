package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.WoolBlock;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(WoolBlock.class)
public abstract class WoolBlockMixin implements BlockStateContainer {
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
}

