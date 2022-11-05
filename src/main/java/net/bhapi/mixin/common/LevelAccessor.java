package net.bhapi.mixin.common;

import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Level.class)
public interface LevelAccessor {
	@Accessor("lightingTicks")
	void setLightingTicks(int lightingTicks);
}
