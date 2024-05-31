package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.storage.Vec2F;
import net.minecraft.block.Block;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRenderMixin {
	@Shadow protected EntityRenderDispatcher dispatcher;
	
	@Inject(method = "render(Lnet/minecraft/entity/Entity;DDDF)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_render(Entity arg, double d, double e, double f, float g, CallbackInfo info) {
		info.cancel();
		GL11.glDisable(GL11.GL_LIGHTING);
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float)d, (float)e, (float)f);
		
		float f6 = arg.width * 1.4f;
		GL11.glScalef(f6, f6, f6);
		
		Textures.getAtlas().bind();
		Tessellator tessellator = Tessellator.INSTANCE;
		
		float dx = 0.5f;
		float height = arg.height / f6;
		float dy = (float) (arg.y - arg.boundingBox.minY);
		
		GL11.glRotatef(-this.dispatcher.angle, 0.0f, 1.0f, 0.0f);
		GL11.glTranslatef(0.0f, 0.0f, -0.3f + (float) ((int) height) * 0.02f);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		float dz = 0.0f;
		
		tessellator.start();
		
		int index = 0;
		while (height > 0.0f) {
			BlockState state = BlockState.getDefaultState(Block.FIRE);
			TextureSample sample = state.getTextureForIndex(arg.level, 0, 0, 0, index, 0);
			
			boolean flip = ((index >> 1) & 1) == 0;
			Vec2F uv1 = sample.getUV(flip ? 1 : 0, 0);
			Vec2F uv2 = sample.getUV(flip ? 0 : 1, 1);
			
			tessellator.vertex(dx, 0.0f - dy, dz, uv2.x, uv2.y);
			tessellator.vertex(-dx, 0.0f - dy, dz, uv1.x, uv2.y);
			tessellator.vertex(-dx, 1.4f - dy, dz, uv1.x, uv1.y);
			tessellator.vertex(dx, 1.4f - dy, dz, uv2.x, uv1.y);
			
			height -= 0.45f;
			dy -= 0.45f;
			dx *= 0.9f;
			dz += 0.03f;
			index++;
		}
		
		tessellator.render();
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
	}
}
