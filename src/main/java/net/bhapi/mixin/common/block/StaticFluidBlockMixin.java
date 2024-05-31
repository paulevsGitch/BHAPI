package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.StaticFluid;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(StaticFluid.class)
public class StaticFluidBlockMixin implements BlockStateContainer {
	@Override
	public void bhapi_appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
}