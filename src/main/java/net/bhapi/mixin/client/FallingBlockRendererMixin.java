package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.texture.Textures;
import net.minecraft.client.render.entity.FallingBlockRenderer;
import net.minecraft.entity.technical.FallingBlockEntity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockRenderer.class)
public class FallingBlockRendererMixin {
	@Inject(method = "method_770", at = @At("HEAD"), cancellable = true)
	private void bhapi_render(FallingBlockEntity entity, double x, double y, double z, float g, float delta, CallbackInfo info) {
		info.cancel();
		BlockState state = BlockStateContainer.cast(entity).bhapi_getDefaultState();
		if (state == null) return;
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		Textures.getAtlas().bind();
		BHAPIClient.getBlockRenderer().setView(entity.level);
		BHAPIClient.getBlockRenderer().renderItem(state, entity.getBrightnessAtEyes(0.5F));
		GL11.glPopMatrix();
	}
}
