package net.bhapi.mixin.client;

import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.storage.Vec2F;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ProjectileRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileRenderer.class)
public abstract class ProjectileRendererMixin extends EntityRenderer {
	@Shadow private int field_177;
	
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void bhapi_render(Entity arg, double x, double y, double z, float g, float h, CallbackInfo info) {
		info.cancel();
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		Textures.getAtlas().bind();
		Tessellator tessellator = Tessellator.INSTANCE;
		TextureSample sample = Textures.getVanillaItemSample(this.field_177);//BHItemRender.cast(BaseItem.snowball).getTexture(null);
		Vec2F uv1 = sample.getUV(0, 0);
		Vec2F uv2 = sample.getUV(1, 1);
		GL11.glRotatef(180.0f - this.dispatcher.angle, 0.0f, 1.0f, 0.0f);
		GL11.glRotatef(-this.dispatcher.pitchAngle, 1.0f, 0.0f, 0.0f);
		tessellator.start();
		tessellator.setNormal(0.0f, 1.0f, 0.0f);
		tessellator.vertex(-0.5f, -0.25f, 0.0, uv1.x, uv2.y);
		tessellator.vertex(0.5f, -0.25f, 0.0, uv2.x, uv2.y);
		tessellator.vertex(0.5f, 0.75f, 0.0, uv2.x, uv1.y);
		tessellator.vertex(-0.5f, 0.75f, 0.0, uv1.x, uv1.y);
		tessellator.render();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}
}
