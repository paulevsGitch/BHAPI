package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.block.BlockItemView;
import net.bhapi.client.render.texture.TextureAtlas;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.client.render.texture.UVPair;
import net.bhapi.item.BHBlockItem;
import net.bhapi.item.BHItemRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.OverlaysRenderer;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.item.MapRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.AbstractClientPlayer;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MapItem;
import net.minecraft.level.storage.MapStorage;
import net.minecraft.util.maths.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverlaysRenderer.class)
public abstract class OverlaysRendererMixin {
	@Shadow private float movementStart;
	@Shadow private float movementEnd;
	@Shadow private Minecraft minecraft;
	@Shadow private ItemStack stack;
	@Shadow private MapRenderer mapRenderer;
	@Shadow private BlockRenderer blockRenderer;
	
	@Shadow public abstract void renderHand(LivingEntity arg, ItemStack arg2);
	
	@Unique private BlockItemView bhapi_itemView = new BlockItemView();
	
	@Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderItemInHand(float delta, CallbackInfo info) {
		info.cancel();
		float r, g, b;
		float offset = this.movementStart + (this.movementEnd - this.movementStart) * delta;
		AbstractClientPlayer player = this.minecraft.player;
		float rotation = player.prevPitch + (player.pitch - player.prevPitch) * delta;
		
		GL11.glPushMatrix();
		GL11.glRotatef(rotation, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(
			player.prevYaw + (player.yaw - player.prevYaw) * delta,
			0.0F, 1.0F, 0.0F
		);
		RenderHelper.enableLighting();
		GL11.glPopMatrix();
		
		float light = this.minecraft.level.getBrightness(
			MathHelper.floor(player.x),
			MathHelper.floor(player.y),
			MathHelper.floor(player.z)
		);
		
		if (this.stack != null) {
			BaseItem item = this.stack.getType();
			int color = item.getColorMultiplier(this.stack.getDamage());
			r = (float) (color >> 16 & 0xFF) / 255.0F;
			g = (float) (color >> 8 & 0xFF) / 255.0F;
			b = (float) (color & 0xFF) / 255.0F;
			GL11.glColor4f(light * r, light * g, light * b, 1.0F);
		}
		else {
			GL11.glColor4f(light, light, light, 1.0f);
		}
		
		if (this.stack != null && this.stack.getType() instanceof MapItem) {
			GL11.glPushMatrix();
			float f8 = 0.8f;
			r = player.getHandSwing(delta);
			g = MathHelper.sin(r * (float)Math.PI);
			b = MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI);
			GL11.glTranslatef(-b * 0.4f, MathHelper.sin(MathHelper.sqrt(r) * (float) Math.PI * 2.0f) * 0.2f, -g * 0.2f);
			r = 1.0f - rotation / 45.0f + 0.1f;
			if (r < 0.0f) {
				r = 0.0f;
			}
			if (r > 1.0f) {
				r = 1.0f;
			}
			r = -MathHelper.cos(r * (float)Math.PI) * 0.5f + 0.5f;
			GL11.glTranslatef(0.0f, 0.0f * f8 - (1.0f - offset) * 1.2f - r * 0.5f + 0.04f, -0.9f * f8);
			GL11.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(r * -85.0f, 0.0f, 0.0f, 1.0f);
			GL11.glEnable(32826);
			GL11.glBindTexture(3553, this.minecraft.textureManager.getOnlineImageOrDefaultTextureId(this.minecraft.player.skinUrl, this.minecraft.player.getTextured()));
			
			for (int i = 0; i < 2; ++i) {
				int n = i * 2 - 1;
				GL11.glPushMatrix();
				GL11.glTranslatef(-0.0f, -0.6f, 1.1f * (float)n);
				GL11.glRotatef(-45 * n, 1.0f, 0.0f, 0.0f);
				GL11.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
				GL11.glRotatef(59.0f, 0.0f, 0.0f, 1.0f);
				GL11.glRotatef(-65 * n, 0.0f, 1.0f, 0.0f);
				EntityRenderer entityRenderer = EntityRenderDispatcher.INSTANCE.get(this.minecraft.player);
				PlayerRenderer playerRenderer = (PlayerRenderer)entityRenderer;
				float f9 = 1.0f;
				GL11.glScalef(f9, f9, f9);
				playerRenderer.method_345();
				GL11.glPopMatrix();
			}
			
			float f10 = player.getHandSwing(delta);
			b = MathHelper.sin(f10 * f10 * (float)Math.PI);
			float f11 = MathHelper.sin(MathHelper.sqrt(f10) * (float)Math.PI);
			GL11.glRotatef(-b * 20.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(-f11 * 20.0f, 0.0f, 0.0f, 1.0f);
			GL11.glRotatef(-f11 * 80.0f, 1.0f, 0.0f, 0.0f);
			f10 = 0.38f;
			GL11.glScalef(f10, f10, f10);
			GL11.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);
			GL11.glTranslatef(-1.0f, -1.0f, 0.0f);
			b = 0.015625f;
			GL11.glScalef(b, b, b);
			this.minecraft.textureManager.bindTexture(this.minecraft.textureManager.getTextureId("/misc/mapbg.png"));
			Tessellator tessellator = Tessellator.INSTANCE;
			GL11.glNormal3f(0.0f, 0.0f, -1.0f);
			tessellator.start();
			int n = 7;
			tessellator.vertex(0 - n, 128 + n, 0.0, 0.0, 1.0);
			tessellator.vertex(128 + n, 128 + n, 0.0, 1.0, 1.0);
			tessellator.vertex(128 + n, 0 - n, 0.0, 1.0, 0.0);
			tessellator.vertex(0 - n, 0 - n, 0.0, 0.0, 0.0);
			tessellator.draw();
			MapStorage mapStorage = BaseItem.map.getMapStorage(this.stack, this.minecraft.level);
			this.mapRenderer.loadData(this.minecraft.player, this.minecraft.textureManager, mapStorage);
			GL11.glPopMatrix();
		}
		else if (this.stack != null) {
			GL11.glPushMatrix();
			float f12 = 0.8f;
			r = player.getHandSwing(delta);
			g = MathHelper.sin(r * (float)Math.PI);
			b = MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI);
			GL11.glTranslatef(-b * 0.4f, MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI * 2.0f) * 0.2f, -g * 0.2f);
			GL11.glTranslatef(0.7f * f12, -0.65f * f12 - (1.0f - offset) * 0.6f, -0.9f * f12);
			GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
			GL11.glEnable(32826);
			r = player.getHandSwing(delta);
			g = MathHelper.sin(r * r * (float)Math.PI);
			b = MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI);
			GL11.glRotatef(-g * 20.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(-b * 20.0f, 0.0f, 0.0f, 1.0f);
			GL11.glRotatef(-b * 80.0f, 1.0f, 0.0f, 0.0f);
			r = 0.4f;
			GL11.glScalef(r, r, r);
			if (this.stack.getType().shouldSpinWhenRendering()) {
				GL11.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
			}
			this.renderHand(player, this.stack);
			GL11.glPopMatrix();
		}
		else {
			GL11.glPushMatrix();
			float f13 = 0.8f;
			r = player.getHandSwing(delta);
			g = MathHelper.sin(r * (float)Math.PI);
			b = MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI);
			GL11.glTranslatef(-b * 0.3f, MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI * 2.0f) * 0.4f, -g * 0.4f);
			GL11.glTranslatef(0.8f * f13, -0.75f * f13 - (1.0f - offset) * 0.6f, -0.9f * f13);
			GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
			GL11.glEnable(32826);
			r = player.getHandSwing(delta);
			g = MathHelper.sin(r * r * (float)Math.PI);
			b = MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI);
			GL11.glRotatef(b * 70.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(-g * 20.0f, 0.0f, 0.0f, 1.0f);
			GL11.glBindTexture(3553, this.minecraft.textureManager.getOnlineImageOrDefaultTextureId(this.minecraft.player.skinUrl, this.minecraft.player.getTextured()));
			GL11.glTranslatef(-1.0f, 3.6f, 3.5f);
			GL11.glRotatef(120.0f, 0.0f, 0.0f, 1.0f);
			GL11.glRotatef(200.0f, 1.0f, 0.0f, 0.0f);
			GL11.glRotatef(-135.0f, 0.0f, 1.0f, 0.0f);
			GL11.glScalef(1.0f, 1.0f, 1.0f);
			GL11.glTranslatef(5.6f, 0.0f, 0.0f);
			EntityRenderer entityRenderer = EntityRenderDispatcher.INSTANCE.get(this.minecraft.player);
			PlayerRenderer playerRenderer = (PlayerRenderer)entityRenderer;
			b = 1.0f;
			GL11.glScalef(b, b, b);
			playerRenderer.method_345();
			GL11.glPopMatrix();
		}
		GL11.glDisable(32826);
		RenderHelper.disableLighting();
	}
	
	@Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderHand(LivingEntity entity, ItemStack stack, CallbackInfo info) {
		info.cancel();
		GL11.glPushMatrix();
		BaseItem item = stack.getType();
		TextureAtlas atlas = Textures.getAtlas();
		atlas.bind();
		
		if (item instanceof BHBlockItem) {
			// TODO save only first after all render implementation
			BlockState state = ((BHBlockItem) item).getState();
			if (BHBlockRenderer.isImplemented(state.getRenderType(entity.level, (int) entity.x, (int) entity.y, (int) entity.z))) {
				GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
				BHBlockRenderer.setRenderer(bhapi_itemView, this.blockRenderer);
				BHBlockRenderer.renderItem(state, false, entity.getBrightnessAtEyes(1.0f));
			}
			else this.blockRenderer.renderBlockItem(((BHBlockItem) item).getState().getBlock(), stack.getDamage(), entity.getBrightnessAtEyes(1.0f));
		}
		else {
			float f;
			float u22;
			float delta;
			int count;
			
			UVPair uv;
			if (item instanceof BHItemRender) uv = BHItemRender.cast(item).getTextureForIndex(stack).getUV();
			else uv = atlas.getUV(entity.getTexture(stack));
			
			float u1 = uv.getU(0);
			float u2 = uv.getU(1);
			float v1 = uv.getV(0);
			float v2 = uv.getV(1);
			
			float f8 = 1.0f;
			float f9 = 0.0f;
			float f10 = 0.3f;
			GL11.glEnable(32826);
			GL11.glTranslatef(-f9, -f10, 0.0f);
			float f11 = 1.5f;
			GL11.glScalef(f11, f11, f11);
			GL11.glRotatef(50.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(335.0f, 0.0f, 0.0f, 1.0f);
			GL11.glTranslatef(-0.9375f, -0.0625f, 0.0f);
			
			Tessellator tessellator = Tessellator.INSTANCE;
			
			tessellator.start();
			tessellator.setNormal(0.0f, 0.0f, 1.0f);
			tessellator.vertex(0.0, 0.0, 0.0, u2, v2);
			tessellator.vertex(f8, 0.0, 0.0, u1, v2);
			tessellator.vertex(f8, 1.0, 0.0, u1, v1);
			tessellator.vertex(0.0, 1.0, 0.0, u2, v1);
			//tessellator.draw();
			
			//tessellator.start();
			tessellator.setNormal(0.0f, 0.0f, -1.0f);
			tessellator.vertex(0.0, 1.0, 0.0f - 0.0625f, u2, v1);
			tessellator.vertex(f8, 1.0, 0.0f - 0.0625f, u1, v1);
			tessellator.vertex(f8, 0.0, 0.0f - 0.0625f, u1, v2);
			tessellator.vertex(0.0, 0.0, 0.0f - 0.0625f, u2, v2);
			
			tessellator.setNormal(-1.0f, 0.0f, 0.0f);
			for (count = 0; count < uv.getWidth(); ++count) {
				delta = (float) count / uv.getWidth();
				u22 = uv.getU(0.999F - delta);
				f = f8 * delta;
				tessellator.vertex(f, 0.0, 0.0f - 0.0625f, u22, v2);
				tessellator.vertex(f, 0.0, 0.0, u22, v2);
				tessellator.vertex(f, 1.0, 0.0, u22, v1);
				tessellator.vertex(f, 1.0, 0.0f - 0.0625f, u22, v1);
			}
			
			tessellator.setNormal(0.0f, -1.0f, 0.0f);
			for (count = 0; count < uv.getHeight(); ++count) {
				delta = (float) count / uv.getHeight();
				u22 = uv.getV(0.999F - delta);
				f = f8 * delta;
				tessellator.vertex(f8, f, 0.0, u1, u22);
				tessellator.vertex(0.0, f, 0.0, u2, u22);
				tessellator.vertex(0.0, f, 0.0f - 0.0625f, u2, u22);
				tessellator.vertex(f8, f, 0.0f - 0.0625f, u1, u22);
			}
			tessellator.draw();
			
			GL11.glDisable(32826);
		}
		GL11.glPopMatrix();
	}
}
