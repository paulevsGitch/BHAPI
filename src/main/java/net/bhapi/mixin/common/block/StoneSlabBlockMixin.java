package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.StoneSlabBlock;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(StoneSlabBlock.class)
public abstract class StoneSlabBlockMixin implements BlockStateContainer {
	@Override
	public void bhapi_appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_4);
	}
}

