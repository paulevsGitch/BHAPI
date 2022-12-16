package net.bhapi.mixin.client;

import net.minecraft.client.render.AreaRenderer;
import net.minecraft.client.render.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("field_1807")
	List<AreaRenderer> getUpdateAreas();
}
