package net.bhapi.mixin.client;

import net.bhapi.client.render.block.BlockBreakingInfo;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.storage.Vec2I;
import net.bhapi.util.MathUtil;
import net.minecraft.client.render.blockentity.BlockEntityRenderer;
import net.minecraft.client.render.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.texture.TextureManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderer.class)
public class BlockEntityRendererMixin implements BlockBreakingInfo {
	@Shadow protected BlockEntityRenderDispatcher renderDispatcher;
	@Unique private int bhapi_breaking = -1;
	@Unique private Vec2I bhapi_scale;
	
	@Inject(method = "setTexture", at = @At("HEAD"), cancellable = true)
	protected void setTexture(String string, CallbackInfo info) {
		if (bhapi_breaking != -1) {
			info.cancel();
			
			TextureManager textureManager = this.renderDispatcher.textureManager;
			
			if (bhapi_scale == null) {
				textureManager.bindTexture(textureManager.getTextureId(string));
				int width1 = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
				int height1 = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
				
				textureManager.bindTexture(Textures.getBlockBreaking(0));
				int width2 = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
				int height2 = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
				bhapi_scale = new Vec2I(MathUtil.clamp(width1 / width2, 0, 15), MathUtil.clamp(height1 / height2, 0, 15));
			}
			
			int texture = Textures.getBlockBreaking(bhapi_breaking, bhapi_scale.x, bhapi_scale.y);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			textureManager.bindTexture(texture);
		}
	}
	
	@Override
	public void setBreaking(int stage) {
		bhapi_breaking = stage;
	}
}
