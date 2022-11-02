package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin implements BlockStateContainer {
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_6);
	}
}

