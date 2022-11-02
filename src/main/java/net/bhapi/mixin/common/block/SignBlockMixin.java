package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SignBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(SignBlock.class)
public abstract class SignBlockMixin implements BlockStateContainer {
	@Shadow private boolean field_1760;
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(this.field_1760 ? LegacyProperties.META_16 : LegacyProperties.META_5);
	}
}

