package net.bhapi.mixin.common;

import net.minecraft.block.BaseBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseBlock.class)
public interface BaseBlockAccessor {
	@Mutable
	@Accessor("BY_ID")
	static void bhapi_setBlocks(BaseBlock[] blocks) {}
	
	@Mutable
	@Accessor("id")
	void bhapi_setID(int id);
}
