package net.bhapi.mixin.client;

import net.minecraft.client.render.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Tessellator.class)
public interface TesselatorAccessor {
	@Accessor("drawing")
	boolean isDrawing();
}
