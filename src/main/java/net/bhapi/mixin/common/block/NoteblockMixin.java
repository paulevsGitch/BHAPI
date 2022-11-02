package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.Noteblock;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(Noteblock.class)
public abstract class NoteblockMixin implements BlockStateContainer {
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_5);
	}
}

