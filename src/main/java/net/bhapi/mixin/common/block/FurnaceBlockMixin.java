package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.FurnaceBlock;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(FurnaceBlock.class)
public abstract class FurnaceBlockMixin implements BlockStateContainer {
	@Override
	public void bhapi_appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_6);
	}
}
